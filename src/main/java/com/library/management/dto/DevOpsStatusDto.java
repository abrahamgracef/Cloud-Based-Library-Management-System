package com.library.management.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DevOpsStatusDto {
    private String awsRegion;
    private String ec2Instance;
    private String ec2PublicIp;
    private String serverPort;
    private String javaVersion;
    private String activeProfile;
    private String databaseEngine;
    private String databaseStatus;
    private String s3Bucket;
    private String snsTopic;
    private String cloudWatchLogGroup;
    private String githubRepo;
    private String gitBranch;
    private long uptimeSeconds;
    private String uptimeFormatted;
    private String memoryUsage;
    private String pipelineStatus;
}
