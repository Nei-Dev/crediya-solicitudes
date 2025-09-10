package com.crediya.sqs.sender;

import com.crediya.model.creditapplication.DebtCapacityCredit;
import com.crediya.model.creditapplication.gateways.MessageDebtCapacityService;
import com.crediya.sqs.sender.config.SQSDebtCapacitySenderProperties;
import com.google.gson.Gson;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import software.amazon.awssdk.services.sqs.SqsAsyncClient;
import software.amazon.awssdk.services.sqs.model.SendMessageRequest;
import software.amazon.awssdk.services.sqs.model.SendMessageResponse;

@Service
@Log4j2
@RequiredArgsConstructor
public class SQSDebtCapacitySender implements MessageDebtCapacityService {
    private final SQSDebtCapacitySenderProperties properties;
    private final SqsAsyncClient client;
    
    private final Gson gson = new Gson();
    
    @Override
    public Mono<String> sendChangeStateCreditApplication(DebtCapacityCredit debtCapacityCredit) {
        return Mono.fromCallable(() -> gson.toJson(debtCapacityCredit))
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
