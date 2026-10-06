package com.churchflow360.attendanceservice.service;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

/**
 * Generates QR code images (PNG) encoded as Base64 strings.
 *
 * <p>The QR payload is the service event's UUID-based token. When a member
 * scans the QR code, the token is sent to POST /attendance/checkin/qr
 * along with the member ID for validation and check-in.
 *
 * <p>QR images are NOT stored — they are generated on demand and returned
 * as Base64 PNG. The token itself is stored in the database with an expiry.
 */
@Service
@Slf4j
public class QrCodeService {

    @Value("${churchflow360.attendance.qr.size:300}")
    private int qrSize;

    /**
     * Generates a QR code PNG image encoded as a Base64 string.
     *
     * @param content  the text to encode (the QR token UUID string)
     * @return Base64-encoded PNG image string (ready for img src="data:image/png;base64,...")
     */
    public String generateQrCodeBase64(String content) {
        try {
            Map<EncodeHintType, Object> hints = new HashMap<>();
            hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.H);
            hints.put(EncodeHintType.MARGIN, 2);
            hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");

            QRCodeWriter writer = new QRCodeWriter();
            BitMatrix matrix = writer.encode(content, BarcodeFormat.QR_CODE, qrSize, qrSize, hints);

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            MatrixToImageWriter.writeToStream(matrix, "PNG", outputStream);

            byte[] pngBytes = outputStream.toByteArray();
            return Base64.getEncoder().encodeToString(pngBytes);

        } catch (WriterException | IOException e) {
            log.error("Failed to generate QR code for content: {}", content, e);
            throw new RuntimeException("QR code generation failed: " + e.getMessage());
        }
    }

    /**
     * Generates the raw PNG bytes of a QR code (for direct HTTP response).
     */
    public byte[] generateQrCodeBytes(String content) {
        try {
            Map<EncodeHintType, Object> hints = new HashMap<>();
            hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.H);
            hints.put(EncodeHintType.MARGIN, 2);

            QRCodeWriter writer = new QRCodeWriter();
            BitMatrix matrix = writer.encode(content, BarcodeFormat.QR_CODE, qrSize, qrSize, hints);

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            MatrixToImageWriter.writeToStream(matrix, "PNG", outputStream);
            return outputStream.toByteArray();

        } catch (WriterException | IOException e) {
            log.error("Failed to generate QR code bytes", e);
            throw new RuntimeException("QR code generation failed: " + e.getMessage());
        }
    }
}