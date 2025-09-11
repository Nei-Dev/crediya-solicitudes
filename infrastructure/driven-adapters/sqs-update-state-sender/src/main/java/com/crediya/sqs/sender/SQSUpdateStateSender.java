package com.crediya.sqs.sender;

import com.crediya.model.creditapplication.CreditApplication;
import com.crediya.model.creditapplication.Installment;
import com.crediya.model.creditapplication.StateCreditApplication;
import com.crediya.model.creditapplication.gateways.MessageChangeStatusService;
import com.crediya.sqs.sender.config.SQSUpdateStateSenderProperties;
import com.crediya.sqs.sender.dto.creditapplication.StatusUpdatedPayload;
import com.google.gson.Gson;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import software.amazon.awssdk.services.sqs.SqsAsyncClient;
import software.amazon.awssdk.services.sqs.model.SendMessageRequest;
import software.amazon.awssdk.services.sqs.model.SendMessageResponse;

import java.util.List;
import java.util.Locale;

import static java.text.NumberFormat.getCurrencyInstance;

@Service
@Slf4j
@RequiredArgsConstructor
public class SQSUpdateStateSender implements MessageChangeStatusService {
    private final SQSUpdateStateSenderProperties properties;
    private final SqsAsyncClient client;
    
    private final Gson gson = new Gson();
    
    @Override
    public Mono<String> sendChangeStateCreditApplication(CreditApplication creditApplication, List<Installment> paymentPlan) {
        return Mono.fromCallable(() -> new StatusUpdatedPayload(
                creditApplication.getClientName(),
                getCurrencyInstance(Locale.US).format(creditApplication.getAmount()),
                creditApplication.getState().equals(StateCreditApplication.APPROVED),
                paymentPlan
            ))
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
