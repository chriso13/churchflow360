package com.churchflow360.attendanceservice.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QrCodeResponse {
    private String qrToken;
    private String qrImageBase64;   // PNG image as base64 string
    private LocalDateTime expiresAt;
    private String serviceEventTitle;
    private String serviceEventId;
}