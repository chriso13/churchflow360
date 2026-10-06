package com.churchflow360.notification.service.email;

import com.churchflow360.notification.config.NotificationProperties;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class BrevoEmailService {

    private final JavaMailSender mailSender;
    private final NotificationProperties props;

    public EmailResult sendEmail(String toEmail, String toName,
                                 String subject, String body) {
        if (!props.getEmail().isEnabled()) {
            log.info("Email disabled — skipping to {}", toEmail);
            return EmailResult.skipped("Email notifications are disabled");
        }
        if (toEmail == null || toEmail.isBlank()) {
            return EmailResult.failed("Recipient email address is required");
        }
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(props.getEmail().getFromAddress(), props.getEmail().getFromName());
            helper.setTo(toEmail);
            helper.setSubject(subject != null ? subject : "(no subject)");
            helper.setText(body, false);
            mailSender.send(message);
            log.info("Email sent via Brevo → {} <{}>", toName, toEmail);
            return EmailResult.ok();
        } catch (Exception e) {
            log.error("Brevo email failed to <{}>: {}", toEmail, e.getMessage());
            return EmailResult.failed(e.getMessage());
        }
    }

    public EmailResult sendHtmlEmail(String toEmail, String toName,
                                     String subject, String htmlBody) {
        if (!props.getEmail().isEnabled()) {
            return EmailResult.skipped("Email notifications are disabled");
        }
        if (toEmail == null || toEmail.isBlank()) {
            return EmailResult.failed("Recipient email address is required");
        }
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(props.getEmail().getFromAddress(), props.getEmail().getFromName());
            helper.setTo(toEmail);
            helper.setSubject(subject != null ? subject : "(no subject)");
            helper.setText(htmlBody, true);
            mailSender.send(message);
            log.info("HTML email sent via Brevo → {} <{}>", toName, toEmail);
            return EmailResult.ok();
        } catch (Exception e) {
            log.error("Brevo HTML email failed to <{}>: {}", toEmail, e.getMessage());
            return EmailResult.failed(e.getMessage());
        }
    }

    // ── Result — use sent/skipped/errorMsg to avoid accessor name conflict ────
    public static final class EmailResult {
        private final boolean sent;
        private final boolean skipped;
        private final String  errorMsg;

        private EmailResult(boolean sent, boolean skipped, String errorMsg) {
            this.sent     = sent;
            this.skipped  = skipped;
            this.errorMsg = errorMsg;
        }

        public static EmailResult ok()                  { return new EmailResult(true,  false, null); }
        public static EmailResult failed(String error)  { return new EmailResult(false, false, error); }
        public static EmailResult skipped(String reason){ return new EmailResult(false, true,  reason); }

        public boolean isSent()    { return sent; }
        public boolean isSkipped() { return skipped; }
        public boolean isFailed()  { return !sent && !skipped; }
        public String  getError()  { return errorMsg; }
    }
}
