package com.tracksuit.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ParsedEmailDTO {
    private String platform;
    private String productName;
    private String status;
    private LocalDate expectedDate;
    private boolean parsed;

    public static ParsedEmailDTO unparsed() {
        ParsedEmailDTO dto = new ParsedEmailDTO();
        dto.setParsed(false);
        return dto;
    }
}
