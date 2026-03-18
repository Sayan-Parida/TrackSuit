package com.tracksuit.backend.controller;

import com.tracksuit.backend.dto.ApiResponse;
import com.tracksuit.backend.model.Order;
import com.tracksuit.backend.service.GmailService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/gmail")
public class GmailController {

    private static final Logger log = LoggerFactory.getLogger(GmailController.class);

    private final GmailService gmailService;

    @Value("${google.oauth2.client-id}")
    private String clientId;

    @Value("${google.oauth2.redirect-uri}")
    private String redirectUri;

    @Value("${google.oauth2.scope}")
    private String scope;

    public GmailController(GmailService gmailService) {
        this.gmailService = gmailService;
    }

    /**
     * GET /api/v1/gmail/connect
     * Redirects the browser to Google's OAuth consent screen.
     */
    @GetMapping("/connect")
    public void connect(HttpServletResponse response) throws IOException {
        String authUrl = "https://accounts.google.com/o/oauth2/v2/auth"
                + "?client_id=" + URLEncoder.encode(clientId, StandardCharsets.UTF_8)
                + "&redirect_uri=" + URLEncoder.encode(redirectUri, StandardCharsets.UTF_8)
                + "&response_type=code"
                + "&scope=" + URLEncoder.encode(scope, StandardCharsets.UTF_8)
                + "&access_type=offline"
                + "&prompt=consent";

        log.info("=== OAuth Connect ===");
        log.info("Redirecting to Google OAuth URL:");
        log.info("  Client ID: {}...{}", clientId.substring(0, 10), clientId.substring(clientId.length() - 5));
        log.info("  Redirect URI: {}", redirectUri);
        log.info("  Scope: {}", scope);
        log.debug("  Full Auth URL: {}", authUrl);

        response.sendRedirect(authUrl);
    }

    /**
     * GET /api/v1/gmail/callback?code=...
     * Google redirects here after user grants consent.
     * Exchanges the authorization code for an access token.
     */
    @GetMapping("/callback")
    public ResponseEntity<ApiResponse<Map<String, String>>> callback(
            @RequestParam(value = "code", required = false) String code,
            @RequestParam(value = "error", required = false) String error) {

        log.info("=== OAuth Callback ===");

        // Handle error from Google
        if (error != null) {
            log.error("OAuth error from Google: {}", error);
            return ResponseEntity.badRequest().body(
                    ApiResponse.error("Google OAuth error: " + error));
        }

        // Validate code
        if (code == null || code.isBlank()) {
            log.error("No authorization code received in callback");
            return ResponseEntity.badRequest().body(
                    ApiResponse.error("No authorization code received"));
        }

        log.info("Authorization code received: {}...", code.substring(0, Math.min(10, code.length())));

        try {
            // Exchange authorization code for access token
            String accessToken = gmailService.exchangeCodeForToken(code);

            log.info("=== OAuth SUCCESS ===");
            log.info("Access token received: {}...", accessToken.substring(0, Math.min(15, accessToken.length())));

            Map<String, String> data = Map.of(
                    "status", "connected",
                    "tokenPreview", accessToken.substring(0, Math.min(15, accessToken.length())) + "..."
            );

            return ResponseEntity.ok(ApiResponse.success(
                    "Gmail connected successfully! Access token obtained.", data));

        } catch (Exception e) {
            log.error("=== OAuth FAILED ===");
            log.error("Token exchange failed: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().body(
                    ApiResponse.error("Token exchange failed: " + e.getMessage()));
        }
    }

    /**
     * GET /api/v1/gmail/fetch
     * Uses the stored access token to fetch and parse emails from Gmail.
     */
    @GetMapping("/fetch")
    public ResponseEntity<ApiResponse<List<Order>>> fetchEmails() {
        log.info("=== Fetch Emails ===");

        String accessToken = gmailService.getStoredAccessToken();
        if (accessToken == null) {
            log.warn("No access token stored. User must connect first via /api/v1/gmail/connect");
            return ResponseEntity.badRequest().body(
                    ApiResponse.error("Not connected to Gmail. Visit /api/v1/gmail/connect first."));
        }

        log.info("Using stored access token: {}...", accessToken.substring(0, Math.min(15, accessToken.length())));

        try {
            List<Order> orders = gmailService.fetchAndParseEmails(accessToken);
            log.info("=== Fetch SUCCESS === Found {} orders", orders.size());
            return ResponseEntity.ok(ApiResponse.success(
                    "Fetched " + orders.size() + " orders from Gmail", orders));
        } catch (Exception e) {
            log.error("=== Fetch FAILED === {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().body(
                    ApiResponse.error("Failed to fetch emails: " + e.getMessage()));
        }
    }

    /**
     * GET /api/v1/gmail/status
     * Check if the user has connected their Gmail (has a stored token).
     */
    @GetMapping("/status")
    public ResponseEntity<ApiResponse<Map<String, Object>>> status() {
        String token = gmailService.getStoredAccessToken();
        boolean connected = token != null;
        log.info("Gmail connection status: {}", connected ? "CONNECTED" : "NOT CONNECTED");
        return ResponseEntity.ok(ApiResponse.success(
                connected ? "Gmail is connected" : "Gmail is not connected",
                Map.of("connected", connected)));
    }
}
