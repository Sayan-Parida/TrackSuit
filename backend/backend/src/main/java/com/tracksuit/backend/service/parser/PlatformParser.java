package com.tracksuit.backend.service.parser;

import com.tracksuit.backend.dto.ParsedEmailDTO;

/**
 * Interface for platform-specific email parsers.
 * Each implementation handles parsing for a specific e-commerce platform.
 */
public interface PlatformParser {

    /**
     * Check if this parser can handle the given email.
     */
    boolean canParse(String subject, String body);

    /**
     * Parse the email and extract order details.
     */
    ParsedEmailDTO parse(String subject, String body);

    /**
     * Get the platform name this parser handles.
     */
    String getPlatformName();
}
