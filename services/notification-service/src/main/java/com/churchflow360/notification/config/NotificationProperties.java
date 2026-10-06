package com.churchflow360.notification.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "notification")
@Data
public class NotificationProperties {

    private Email email = new Email();
    private Sms   sms   = new Sms();

    @Data
    public static class Email {
        private String  fromAddress = "noreply@rehoboth360.org";
        private String  fromName    = "Rehoboth Pentecostal Church";
        private boolean enabled     = true;
    }

    @Data
    public static class Sms {
        private boolean enabled = true;
        private Twilio  twilio  = new Twilio();

        @Data
        public static class Twilio {
            private String accountSid;
            private String authToken;
            private String fromNumber;
        }
    }
}
