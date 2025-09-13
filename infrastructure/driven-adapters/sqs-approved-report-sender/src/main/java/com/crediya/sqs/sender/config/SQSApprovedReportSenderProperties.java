package com.crediya.sqs.sender.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "adapter.sqs.approved-report")
public record SQSApprovedReportSenderProperties(
     String region,
     String queueUrl,
     String endpoint){
}
