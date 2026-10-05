package com.library.management.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.sns.SnsClient;
import software.amazon.awssdk.services.sns.model.PublishRequest;
import software.amazon.awssdk.services.sns.model.PublishResponse;

import java.math.BigDecimal;

@Service
public class SnsNotificationService {

    private static final Logger log = LoggerFactory.getLogger(SnsNotificationService.class);

    private final SnsClient snsClient;

    @Value("${aws.sns.topic-arn:arn:aws:sns:us-east-1:123456789012:library-notifications}")
    private String topicArn;

    @Autowired
    public SnsNotificationService(SnsClient snsClient) {
        this.snsClient = snsClient;
    }

    public void sendOverdueNotification(String memberEmail, String bookTitle, String dueDate) {
        String subject = "Overdue Book Notice - Library Management System";
        String message = String.format("Dear Member (%s),\n\n" +
                "The book '%s' was due on %s and is now overdue. Please return it as soon as possible to avoid further fines.\n\n" +
                "Thank you,\nLibrary Management", memberEmail, bookTitle, dueDate);

        publishNotification(subject, message);
    }

    public void sendFineNotice(String memberEmail, BigDecimal amount, String reason) {
        String subject = "Fine Notice - Library Management System";
        String message = String.format("Dear Member (%s),\n\n" +
                "A fine of $%.2f has been assessed to your account. Reason: %s.\n" +
                "Please settle your fine at your earliest convenience.\n\n" +
                "Thank you,\nLibrary Management", memberEmail, amount, reason);

        publishNotification(subject, message);
    }

    public void sendDeploymentStatusUpdate(String statusDetails) {
        String subject = "Deployment Status Update - Library Management System";
        String message = String.format("Library Management System Deployment Update:\n\n%s\n\n" +
                "Environment: AWS Cloud Services\nAuthor: Abraham Grace F", statusDetails);

        publishNotification(subject, message);
    }

    public boolean publishNotification(String subject, String message) {
        try {
            PublishRequest request = PublishRequest.builder()
                    .topicArn(topicArn)
                    .subject(subject)
                    .message(message)
                    .build();

            PublishResponse response = snsClient.publish(request);
            log.info("Successfully sent SNS notification [MessageId: {}] with subject: {}", response.messageId(), subject);
            return true;
        } catch (Exception e) {
            log.warn("Failed to publish SNS notification [Subject: {}]: {}. Logging message as fallback.\nMessage Body:\n{}",
                    subject, e.getMessage(), message);
            return false;
        }
    }
}
