package com.crediya.sqs.sender.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "adapter.sqs.debt-capacity")
public record SQSDebtCapacitySenderProperties(
     String region,
     String queueUrl,
     String endpoint
){}
