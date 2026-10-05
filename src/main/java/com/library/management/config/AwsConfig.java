package com.library.management.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AnonymousCredentialsProvider;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.sns.SnsClient;

@Configuration
public class AwsConfig {

    private static final Logger log = LoggerFactory.getLogger(AwsConfig.class);

    @Value("${aws.region:us-east-1}")
    private String awsRegion;

    @Bean
    public S3Client s3Client() {
        try {
            log.info("Initializing AWS S3Client with region: {}", awsRegion);
            return S3Client.builder()
                    .region(Region.of(awsRegion))
                    .credentialsProvider(DefaultCredentialsProvider.create())
                    .build();
        } catch (Exception e) {
            log.warn("Failed to initialize standard AWS S3Client ({}), falling back to anonymous credentials provider", e.getMessage());
            return S3Client.builder()
                    .region(Region.of(awsRegion))
                    .credentialsProvider(AnonymousCredentialsProvider.create())
                    .build();
        }
    }

    @Bean
    public SnsClient snsClient() {
        try {
            log.info("Initializing AWS SnsClient with region: {}", awsRegion);
            return SnsClient.builder()
                    .region(Region.of(awsRegion))
                    .credentialsProvider(DefaultCredentialsProvider.create())
                    .build();
        } catch (Exception e) {
            log.warn("Failed to initialize standard AWS SnsClient ({}), falling back to anonymous credentials provider", e.getMessage());
            return SnsClient.builder()
                    .region(Region.of(awsRegion))
                    .credentialsProvider(AnonymousCredentialsProvider.create())
                    .build();
        }
    }
}
