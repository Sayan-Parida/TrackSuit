package com.tracksuit.backend.service;

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.gmail.Gmail;
import com.google.api.services.gmail.model.ListMessagesResponse;
import com.google.api.services.gmail.model.Message;
import com.google.api.services.gmail.model.MessagePart;
import com.google.api.services.gmail.model.MessagePartHeader;
import com.tracksuit.backend.dto.OrderDTO;
import com.tracksuit.backend.dto.ParsedEmailDTO;
import com.tracksuit.backend.exception.GmailException;
import com.tracksuit.backend.model.EmailRaw;
import com.tracksuit.backend.model.Order;
import com.tracksuit.backend.repository.EmailRawRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

@Service
public class GmailService {

    private static final Logger log = LoggerFactory.getLogger(GmailService.class);
    private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();
    private static final String TOKEN_ENDPOINT = "https://oauth2.googleapis.com/token";

    // In-memory token storage (good enough for dev/testing)
    private final AtomicReference<String> storedAccessToken = new AtomicReference<>();

    private final EmailRawRepository emailRawRepository;
    private final EmailParserService emailParserService;
    private final OrderService orderService;
    private final RestClient restClient;

    @Value("${google.oauth2.client-id}")
    private String clientId;

    @Value("${google.oauth2.client-secret}")
    private String clientSecret;

    @Value("${google.oauth2.redirect-uri}")
    private String redirectUri;

    public GmailService(EmailRawRepository emailRawRepository,
                        EmailParserService emailParserService,
                        OrderService orderService) {
        this.emailRawRepository = emailRawRepository;
        this.emailParserService = emailParserService;
        this.orderService = orderService;
        this.restClient = RestClient.create();
    }

    // ==========================================
    // Token Exchange & Storage
    // ==========================================

    /**
     * Exchange an authorization code for an access token by POSTing to Google's token endpoint.
     * Stores the token in-memory for later use.
     */
    @SuppressWarnings("unchecked")
    public String exchangeCodeForToken(String authorizationCode) {
        log.info("=== Exchanging authorization code for access token ===");
        log.debug("  Token endpoint: {}", TOKEN_ENDPOINT);
        log.debug("  Client ID: {}...{}", clientId.substring(0, 10), clientId.substring(clientId.length() - 5));
        log.debug("  Redirect URI: {}", redirectUri);

        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        formData.add("code", authorizationCode);
        formData.add("client_id", clientId);
        formData.add("client_secret", clientSecret);
        formData.add("redirect_uri", redirectUri);
        formData.add("grant_type", "authorization_code");

        try {
            Map<String, Object> response = restClient.post()
                    .uri(TOKEN_ENDPOINT)
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .body(formData)
                    .retrieve()
                    .body(Map.class);

            if (response == null) {
                throw new GmailException("Empty response from Google token endpoint");
            }

            log.debug("Token response keys: {}", response.keySet());

            if (response.containsKey("error")) {
                String error = (String) response.get("error");
                String errorDesc = (String) response.getOrDefault("error_description", "No description");
                log.error("Token exchange error: {} - {}", error, errorDesc);
                throw new GmailException("Google token error: " + error + " - " + errorDesc);
            }

            String accessToken = (String) response.get("access_token");
            if (accessToken == null || accessToken.isBlank()) {
                throw new GmailException("No access_token in Google response. Keys: " + response.keySet());
            }

            // Store the token
            storedAccessToken.set(accessToken);
            log.info("=== Access token obtained and stored successfully ===");
            log.info("  Token preview: {}...", accessToken.substring(0, Math.min(15, accessToken.length())));

            // Log refresh token if present
            if (response.containsKey("refresh_token")) {
                log.info("  Refresh token also received (not stored in this version)");
            }

            return accessToken;

        } catch (GmailException e) {
            throw e;
        } catch (Exception e) {
            log.error("Token exchange request failed: {}", e.getMessage(), e);
            throw new GmailException("Failed to exchange code for token: " + e.getMessage(), e);
        }
    }

    /**
     * Get the stored access token (may be null if user hasn't connected yet).
     */
    public String getStoredAccessToken() {
        return storedAccessToken.get();
    }

    // ==========================================
    // Gmail Email Fetching
    // ==========================================

    /**
     * Fetch recent emails from Gmail using the provided OAuth2 access token,
     * parse them, and save orders to DB.
     */
    public List<Order> fetchAndParseEmails(String accessToken) {
        if (accessToken == null || accessToken.isBlank()) {
            throw new GmailException("Access token is missing or invalid");
        }

        try {
            Gmail gmailClient = buildGmailService(accessToken);
            List<Order> savedOrders = new ArrayList<>();

            // Fetch recent order-related emails
            String query = "subject:(order OR shipped OR delivered OR dispatch) newer_than:30d";
            log.info("Fetching emails with query: {}", query);

            ListMessagesResponse response = gmailClient.users().messages()
                    .list("me")
                    .setQ(query)
                    .setMaxResults(20L)
                    .execute();

            List<Message> messages = response.getMessages();
            if (messages == null || messages.isEmpty()) {
                log.info("No order emails found");
                return savedOrders;
            }

            log.info("Found {} potential order emails", messages.size());

            for (Message msgRef : messages) {
                try {
                    Message fullMessage = gmailClient.users().messages()
                            .get("me", msgRef.getId())
                            .setFormat("full")
                            .execute();

                    String messageId = fullMessage.getId();

                    // Skip already-processed emails
                    if (emailRawRepository.existsByMessageId(messageId)) {
                        log.debug("Skipping already-processed email: {}", messageId);
                        continue;
                    }

                    String subject = extractHeader(fullMessage, "Subject");
                    String body = extractBody(fullMessage);

                    log.debug("Processing email: {} - {}", messageId, subject);

                    // Save raw email
                    EmailRaw raw = new EmailRaw();
                    raw.setMessageId(messageId);
                    raw.setSubject(subject != null ? subject : "(no subject)");
                    raw.setBody(body);
                    raw.setParsedFlag(false);
                    raw.setReceivedAt(LocalDateTime.now());
                    emailRawRepository.save(raw);

                    // Parse email
                    ParsedEmailDTO parsed = emailParserService.parse(
                            subject != null ? subject : "",
                            body != null ? body : ""
                    );

                    if (parsed.isParsed()) {
                        OrderDTO orderDTO = new OrderDTO();
                        orderDTO.setPlatform(parsed.getPlatform());
                        orderDTO.setProductName(parsed.getProductName());
                        orderDTO.setStatus(parsed.getStatus());
                        orderDTO.setExpectedDate(parsed.getExpectedDate());

                        Order saved = orderService.saveOrder(orderDTO);
                        if (saved != null) {
                            savedOrders.add(saved);
                            log.info("Saved order: {} - {}", saved.getPlatform(), saved.getProductName());
                        }

                        // Mark as parsed
                        raw.setParsedFlag(true);
                        emailRawRepository.save(raw);
                    }

                } catch (Exception e) {
                    log.error("Error processing email {}: {}", msgRef.getId(), e.getMessage());
                }
            }

            log.info("Parsed and saved {} new orders", savedOrders.size());
            return savedOrders;

        } catch (GmailException e) {
            throw e;
        } catch (Exception e) {
            throw new GmailException("Failed to fetch emails from Gmail", e);
        }
    }

    // ==========================================
    // Helper Methods
    // ==========================================

    private Gmail buildGmailService(String accessToken) throws Exception {
        HttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();
        HttpRequestInitializer requestInitializer = request ->
                request.getHeaders().setAuthorization("Bearer " + accessToken);

        return new Gmail.Builder(httpTransport, JSON_FACTORY, requestInitializer)
                .setApplicationName("TrackSuit")
                .build();
    }

    private String extractHeader(Message message, String headerName) {
        if (message.getPayload() == null || message.getPayload().getHeaders() == null) {
            return null;
        }
        return message.getPayload().getHeaders().stream()
                .filter(h -> headerName.equalsIgnoreCase(h.getName()))
                .map(MessagePartHeader::getValue)
                .findFirst()
                .orElse(null);
    }

    private String extractBody(Message message) {
        try {
            MessagePart payload = message.getPayload();
            if (payload == null) return null;

            // Simple text body
            if (payload.getBody() != null && payload.getBody().getData() != null) {
                return new String(Base64.getUrlDecoder().decode(payload.getBody().getData()));
            }

            // Multipart — look for text/plain
            if (payload.getParts() != null) {
                for (MessagePart part : payload.getParts()) {
                    if ("text/plain".equals(part.getMimeType()) &&
                            part.getBody() != null && part.getBody().getData() != null) {
                        return new String(Base64.getUrlDecoder().decode(part.getBody().getData()));
                    }
                }
                // Fallback to text/html
                for (MessagePart part : payload.getParts()) {
                    if ("text/html".equals(part.getMimeType()) &&
                            part.getBody() != null && part.getBody().getData() != null) {
                        return new String(Base64.getUrlDecoder().decode(part.getBody().getData()));
                    }
                }
            }
        } catch (Exception e) {
            log.warn("Error extracting email body: {}", e.getMessage());
        }
        return null;
    }
}
