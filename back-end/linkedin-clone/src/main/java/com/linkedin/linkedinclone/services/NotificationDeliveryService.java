package com.linkedin.linkedinclone.services;

import com.linkedin.linkedinclone.model.Job;
import com.linkedin.linkedinclone.model.User;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Collection;

@Service
@RequiredArgsConstructor
public class NotificationDeliveryService {

    private final JavaMailSender mailSender;

    @Value("${app.notifications.email.enabled:false}")
    private boolean emailEnabled;

    @Value("${app.notifications.email.from:no-reply@proconnect.local}")
    private String emailFrom;

    @Value("${app.notifications.sms.enabled:false}")
    private boolean smsEnabled;

    @Value("${twilio.account-sid:}")
    private String twilioAccountSid;

    @Value("${twilio.auth-token:}")
    private String twilioAuthToken;

    @Value("${twilio.from-number:}")
    private String twilioFromNumber;

    public void sendConnectionRequestNotification(User sender, User receiver) {
        String senderName = fullName(sender);
        String subject = senderName + " sent you a connection request";
        String body = "Hi " + safe(receiver.getName()) + ",\n\n"
                + senderName + " wants to connect with you on ProConnect.\n"
                + "Open the app and check your notifications to accept or view the profile.\n\n"
                + "ProConnect";

        sendEmail(receiver, subject, body);
        sendSms(receiver, senderName + " sent you a connection request on ProConnect.");
    }

    public void sendJobPostedNotifications(User recruiter, Job job, Collection<User> recipients) {
        String recruiterName = fullName(recruiter);
        String company = safe(firstNonBlank(job.getCompanyName(), recruiter.getCurrentCompany(), "the company"));
        String title = safe(firstNonBlank(job.getTitle(), "New job"));
        String location = safe(firstNonBlank(job.getLocation(), "Location not specified"));

        for (User recipient : recipients) {
            String subject = "New job posted: " + title;
            String body = "Hi " + safe(recipient.getName()) + ",\n\n"
                    + recruiterName + " posted a new job on ProConnect.\n\n"
                    + "Role: " + title + "\n"
                    + "Company: " + company + "\n"
                    + "Location: " + location + "\n\n"
                    + "Open the Jobs page in the app to view and apply.\n\n"
                    + "ProConnect";

            sendEmail(recipient, subject, body);
            sendSms(recipient, "New job posted on ProConnect: " + title + " at " + company + ".");
        }
    }

    private void sendEmail(User recipient, String subject, String body) {
        if (!emailEnabled || recipient == null || isBlank(recipient.getUsername())) {
            return;
        }
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(emailFrom);
            message.setTo(recipient.getUsername());
            message.setSubject(subject);
            message.setText(body);
            mailSender.send(message);
        } catch (Exception ex) {
            System.out.println("Email notification failed for " + recipient.getUsername() + ": " + ex.getMessage());
        }
    }

    private void sendSms(User recipient, String message) {
        if (!smsEnabled || recipient == null || isBlank(recipient.getPhoneNumber())
                || isBlank(twilioAccountSid) || isBlank(twilioAuthToken) || isBlank(twilioFromNumber)) {
            return;
        }
        try {
            String url = "https://api.twilio.com/2010-04-01/Accounts/" + twilioAccountSid + "/Messages.json";
            String auth = twilioAccountSid + ":" + twilioAuthToken;
            String encodedAuth = Base64.getEncoder().encodeToString(auth.getBytes(StandardCharsets.UTF_8));

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
            headers.set("Authorization", "Basic " + encodedAuth);

            MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
            form.add("From", twilioFromNumber);
            form.add("To", recipient.getPhoneNumber());
            form.add("Body", message);

            new RestTemplate().postForEntity(url, new HttpEntity<>(form, headers), String.class);
        } catch (Exception ex) {
            System.out.println("SMS notification failed for " + recipient.getPhoneNumber() + ": " + ex.getMessage());
        }
    }

    private String fullName(User user) {
        if (user == null) return "Someone";
        return (safe(user.getName()) + " " + safe(user.getSurname())).trim();
    }

    private String safe(String value) {
        return value == null ? "" : value.trim();
    }

    private String firstNonBlank(String... values) {
        for (String value : values) {
            if (!isBlank(value)) return value;
        }
        return "";
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
