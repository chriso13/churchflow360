package com.churchflow360.notification.service.sms;

import com.churchflow360.notification.config.NotificationProperties;
import com.twilio.Twilio;
import com.twilio.exception.ApiException;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Twilio SMS Service.
 *
 * Sends SMS to any phone number worldwide via Twilio Programmable Messaging.
 *
 * Required env vars in IntelliJ Run Configuration:
 *   TWILIO_ACCOUNT_SID  = ACxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx
 *   TWILIO_AUTH_TOKEN   = your_auth_token
 *   TWILIO_FROM_NUMBER  = +15005550006  (your Twilio phone number)
 *
 * Trial account: can only send to verified numbers (up to 5).
 * Paid account:  can send to any number worldwide.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class TwilioSmsService {

    private final NotificationProperties props;

    @PostConstruct
    public void init() {
        NotificationProperties.Sms.Twilio config = props.getSms().getTwilio();
        if (config.getAccountSid() != null && !config.getAccountSid().isBlank()
                && config.getAuthToken() != null && !config.getAuthToken().isBlank()) {
            Twilio.init(config.getAccountSid(), config.getAuthToken());
            log.info("Twilio SMS service initialized — from={}", config.getFromNumber());
        } else {
            log.warn("Twilio credentials not configured — SMS will be skipped");
        }
    }

    /**
     * Send SMS via Twilio.
     *
     * @param toPhone   recipient in E.164 format (+16412331066) or plain digits
     * @param body      SMS text (160 chars = 1 segment)
     * @return SmsResult — never throws
     */
    public SmsResult sendSms(String toPhone, String body) {
        NotificationProperties.Sms.Twilio config = props.getSms().getTwilio();

        if (!props.getSms().isEnabled()) {
            log.info("SMS disabled — skipping to {}", toPhone);
            return SmsResult.skipped("SMS notifications are disabled");
        }

        if (config.getAccountSid() == null || config.getAccountSid().isBlank()) {
            log.warn("Twilio not configured — skipping SMS to {}", toPhone);
            return SmsResult.skipped("Twilio credentials not configured");
        }

        if (toPhone == null || toPhone.isBlank()) {
            return SmsResult.failed("Recipient phone number is required");
        }

        // Normalize to E.164
        String normalized = toPhone.trim().replaceAll("[\\s\\-().]+", "");
        if (!normalized.startsWith("+")) {
            normalized = "+" + normalized;
        }

        // Cap at 160 chars to stay within single SMS segment
        String messageBody = body != null && body.length() > 160
                ? body.substring(0, 157) + "..."
                : body;

        try {
            Message message = Message.creator(
                    new PhoneNumber(normalized),
                    new PhoneNumber(config.getFromNumber()),
                    messageBody
            ).create();

            log.info("SMS sent via Twilio → {} sid={} status={}",
                    normalized, message.getSid(), message.getStatus());
            return SmsResult.success(message.getSid());

        } catch (ApiException e) {
            log.error("Twilio API error to {}: [{}] {}", normalized, e.getCode(), e.getMessage());
            return SmsResult.failed("Twilio error " + e.getCode() + ": " + e.getMessage());
        } catch (Exception e) {
            log.error("Twilio SMS failed to {}: {}", normalized, e.getMessage());
            return SmsResult.failed(e.getMessage());
        }
    }

    // ── Result ────────────────────────────────────────────────────────────────

    public static final class SmsResult {
        private final boolean sent;
        private final boolean skipped;
        private final String  message;

        private SmsResult(boolean sent, boolean skipped, String message) {
            this.sent    = sent;
            this.skipped = skipped;
            this.message = message;
        }

        public static SmsResult success(String msg)    { return new SmsResult(true,  false, msg); }
        public static SmsResult failed(String error)   { return new SmsResult(false, false, error); }
        public static SmsResult skipped(String reason) { return new SmsResult(false, true,  reason); }

        public boolean isSent()    { return sent; }
        public boolean isSkipped() { return skipped; }
        public boolean isFailed()  { return !sent && !skipped; }
        public String  getMessage(){ return message; }
    }
}
