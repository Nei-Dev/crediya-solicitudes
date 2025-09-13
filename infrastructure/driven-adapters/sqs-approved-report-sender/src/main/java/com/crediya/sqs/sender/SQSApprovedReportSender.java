package com.crediya.sqs.sender;

import com.crediya.model.creditapplication.CreditApplication;
import com.crediya.model.creditapplication.gateways.MessageApprovedCreditService;
import com.crediya.sqs.sender.config.SQSApprovedReportSenderProperties;
import com.crediya.sqs.sender.dto.CreditApproved;
import com.google.gson.Gson;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import software.amazon.awssdk.services.sqs.SqsAsyncClient;
import software.amazon.awssdk.services.sqs.model.SendMessageRequest;
import software.amazon.awssdk.services.sqs.model.SendMessageResponse;

@Slf4j
@Service
public class SQSApprovedReportSender implements MessageApprovedCreditService {
    
    private final SQSApprovedReportSenderProperties properties;
    private final SqsAsyncClient client;
    
    private final Gson gson = new Gson();
    
    public SQSApprovedReportSender(SQSApprovedReportSenderProperties properties, @Qualifier("configApprovedReportSqs") SqsAsyncClient client) {
        this.properties = properties;
        this.client = client;
    }
    
    @Override
    public Mono<String> sendApprovedCreditApplication(CreditApplication creditApplication) {
        return Mono.fromCallable(() -> new CreditApproved(creditApplication.getId(), creditApplication.getAmount()))
            .map(gson::toJson)
            .flatMap(this::send);
    }
    
    private Mono<String> send(String message) {
        return Mono.fromCallable(() -> buildRequest(message))
                .flatMap(request -> Mono.fromFuture(client.sendMessage(request)))
                .doOnNext(response -> log.debug("Message sent {}", response.messageId()))
                .map(SendMessageResponse::messageId);
    }

    private SendMessageRequest buildRequest(String message) {
        return SendMessageRequest.builder()
                .queueUrl(properties.queueUrl())
                .messageBody(message)
                .build();
    }
}
