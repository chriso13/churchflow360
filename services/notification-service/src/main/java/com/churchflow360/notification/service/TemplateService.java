package com.churchflow360.notification.service;

import org.springframework.stereotype.Service;

/**
 * Builds notification message bodies.
 * For production, replace with Thymeleaf/FreeMarker HTML templates.
 */
@Service
public class TemplateService {

    private static final String FOOTER =
            "\n\n---\nRehoboth Pentecostal Church\n" +
            "123 Faith Avenue, Kissimmee, FL 34741\n" +
            "Phone: (407) 555-0100 | finance@360church.org";

    public String welcomeEmail(String memberName) {
        return "Dear " + memberName + ",\n\n" +
               "Welcome to 360 Church Flow — the digital home of Rehoboth Pentecostal Church!\n\n" +
               "Your account is now active. You can:\n" +
               "  • View and register for upcoming events\n" +
               "  • Submit and intercede for prayer requests\n" +
               "  • Manage your tithe records\n" +
               "  • Connect with your small group\n\n" +
               "We are blessed to have you in our congregation." + FOOTER;
    }

    public String welcomeSms(String memberName) {
        return "Welcome to 360 Church Flow, " + memberName +
               "! Your account is active. God bless you. - Rehoboth Church";
    }

    public String prayerRequestEmail(String recipientName, String requesterName,
                                      String title, String requestText) {
        return "Dear " + recipientName + ",\n\n" +
               "A new prayer request has been submitted for your intercession:\n\n" +
               "From: " + requesterName + "\n" +
               "Subject: " + title + "\n\n" +
               requestText + "\n\n" +
               "Please lift this up in prayer. Your intercession is valued." + FOOTER;
    }

    public String prayerRequestSms(String requesterName, String title) {
        return "New prayer request from " + requesterName +
               ": \"" + (title.length() > 60 ? title.substring(0,57)+"..." : title) +
               "\" - Please intercede. Rehoboth Church";
    }

    public String pastoralMeetingEmail(String memberName, String pastorName,
                                        String date, String time, String topic) {
        return "Dear " + memberName + ",\n\n" +
               "Your pastoral meeting has been scheduled:\n\n" +
               "  Pastor: " + pastorName + "\n" +
               "  Date:   " + date + "\n" +
               "  Time:   " + time + "\n" +
               "  Topic:  " + topic + "\n\n" +
               "Please confirm your attendance by contacting the church office." + FOOTER;
    }

    public String pastoralMeetingSms(String memberName, String date, String time) {
        return "Pastoral meeting scheduled for " + memberName +
               " on " + date + " at " + time + ". Rehoboth Church";
    }

    public String announcementEmail(String recipientName, String title, String content) {
        return "Dear " + recipientName + ",\n\n" +
               "ANNOUNCEMENT: " + title + "\n\n" +
               content + FOOTER;
    }

    public String announcementSms(String title) {
        return "Church Announcement: " +
               (title.length() > 100 ? title.substring(0,97)+"..." : title) +
               " - Rehoboth Church";
    }

    public String titheReceiptEmail(String memberName, String amount,
                                     String month, String year, String refNumber) {
        return "Dear " + memberName + ",\n\n" +
               "Thank you for your faithful giving!\n\n" +
               "TITHE RECEIPT\n" +
               "--------------\n" +
               "Amount:    $" + amount + "\n" +
               "Month:     " + month + " " + year + "\n" +
               "Reference: " + refNumber + "\n\n" +
               "\"Bring the whole tithe into the storehouse.\" — Malachi 3:10\n\n" +
               "God bless your generosity." + FOOTER;
    }

    public String titheReceiptSms(String memberName, String amount, String month) {
        return "Tithe receipt: $" + amount + " for " + month +
               " recorded. Thank you, " + memberName + "! Rehoboth Church";
    }
}
