package com.crediya.sqs.sender.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "adapter.sqs.update-state")
public record SQSUpdateStateSenderProperties(
     String region,
     String queueUrl,
     String endpoint
){}
