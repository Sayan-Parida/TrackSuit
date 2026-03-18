package com.tracksuit.backend.service;

import com.tracksuit.backend.dto.ParsedEmailDTO;
import com.tracksuit.backend.service.parser.PlatformParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Service that delegates email parsing to the appropriate platform parser.
 * New parsers are auto-discovered via Spring component scanning.
 */
@Service
public class EmailParserService {

    private static final Logger log = LoggerFactory.getLogger(EmailParserService.class);

    private final List<PlatformParser> parsers;

    public EmailParserService(List<PlatformParser> parsers) {
        this.parsers = parsers;
        log.info("Loaded {} platform parsers: {}", parsers.size(),
                parsers.stream().map(PlatformParser::getPlatformName).toList());
    }

    /**
     * Attempt to parse an email using registered platform parsers.
     * Returns the result from the first parser that can handle the email.
     */
    public ParsedEmailDTO parse(String subject, String body) {
        for (PlatformParser parser : parsers) {
            if (parser.canParse(subject, body)) {
                log.debug("Matched parser: {} for subject: {}", parser.getPlatformName(), subject);
                return parser.parse(subject, body);
            }
        }
        log.debug("No parser matched for subject: {}", subject);
        return ParsedEmailDTO.unparsed();
    }
}
