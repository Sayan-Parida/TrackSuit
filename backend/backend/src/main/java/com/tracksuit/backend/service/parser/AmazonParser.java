package com.tracksuit.backend.service.parser;

import com.tracksuit.backend.dto.ParsedEmailDTO;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class AmazonParser implements PlatformParser {

    // Patterns for Amazon order emails
    private static final Pattern SUBJECT_PATTERN = Pattern.compile(
            "(?i)(your\\s+amazon.*order|order\\s+confirmed|shipped|delivered|out\\s+for\\s+delivery)",
            Pattern.CASE_INSENSITIVE
    );

    private static final Pattern PRODUCT_NAME_PATTERN = Pattern.compile(
            "(?i)(?:ordered|item|product)[:\\s]+([^\\n]{5,80})",
            Pattern.CASE_INSENSITIVE
    );

    private static final Pattern STATUS_PATTERN = Pattern.compile(
            "(?i)(shipped|delivered|out for delivery|in transit|order confirmed|dispatched)",
            Pattern.CASE_INSENSITIVE
    );

    private static final Pattern DATE_PATTERN = Pattern.compile(
            "(?i)(?:deliver(?:y|ed)?\\s+(?:by|on|date|expected)?[:\\s]*)([A-Za-z]+\\s+\\d{1,2},?\\s*\\d{4}|\\d{1,2}[\\s-][A-Za-z]+[\\s-]\\d{4}|\\d{4}-\\d{2}-\\d{2})",
            Pattern.CASE_INSENSITIVE
    );

    private static final DateTimeFormatter[] DATE_FORMATS = {
            DateTimeFormatter.ofPattern("MMMM d, yyyy", Locale.ENGLISH),
            DateTimeFormatter.ofPattern("MMMM d yyyy", Locale.ENGLISH),
            DateTimeFormatter.ofPattern("d MMMM yyyy", Locale.ENGLISH),
            DateTimeFormatter.ofPattern("d-MMMM-yyyy", Locale.ENGLISH),
            DateTimeFormatter.ofPattern("yyyy-MM-dd", Locale.ENGLISH),
            DateTimeFormatter.ofPattern("MMM d, yyyy", Locale.ENGLISH),
            DateTimeFormatter.ofPattern("dd MMM yyyy", Locale.ENGLISH),
    };

    @Override
    public boolean canParse(String subject, String body) {
        String combined = (subject + " " + body).toLowerCase();
        return combined.contains("amazon") && SUBJECT_PATTERN.matcher(subject).find();
    }

    @Override
    public ParsedEmailDTO parse(String subject, String body) {
        String combined = subject + "\n" + body;

        String productName = extractGroup(PRODUCT_NAME_PATTERN, combined, "Unknown Product");
        String status = extractGroup(STATUS_PATTERN, combined, "Processing");
        LocalDate expectedDate = extractDate(combined);

        ParsedEmailDTO dto = new ParsedEmailDTO();
        dto.setPlatform("Amazon");
        dto.setProductName(productName.trim());
        dto.setStatus(capitalizeFirst(status));
        dto.setExpectedDate(expectedDate);
        dto.setParsed(true);
        return dto;
    }

    @Override
    public String getPlatformName() {
        return "Amazon";
    }

    private String extractGroup(Pattern pattern, String text, String defaultValue) {
        Matcher matcher = pattern.matcher(text);
        if (matcher.find()) {
            return matcher.group(1).trim();
        }
        return defaultValue;
    }

    private LocalDate extractDate(String text) {
        Matcher matcher = DATE_PATTERN.matcher(text);
        if (matcher.find()) {
            String dateStr = matcher.group(1).trim();
            for (DateTimeFormatter fmt : DATE_FORMATS) {
                try {
                    return LocalDate.parse(dateStr, fmt);
                } catch (Exception ignored) {
                }
            }
        }
        return null;
    }

    private String capitalizeFirst(String s) {
        if (s == null || s.isEmpty()) return s;
        return s.substring(0, 1).toUpperCase() + s.substring(1).toLowerCase();
    }
}
