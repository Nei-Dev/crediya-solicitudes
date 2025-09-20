package com.crediya.sqs.listener.helper;

import com.crediya.sqs.listener.config.SQSCreditEvaluationProperties;
import jakarta.annotation.PreDestroy;
import lombok.Builder;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;
import reactor.util.retry.Retry;
import software.amazon.awssdk.services.sqs.SqsAsyncClient;
import software.amazon.awssdk.services.sqs.model.DeleteMessageRequest;
import software.amazon.awssdk.services.sqs.model.Message;
import software.amazon.awssdk.services.sqs.model.ReceiveMessageRequest;

import java.time.Duration;
import java.util.function.Function;

@Slf4j
@Builder
public class SQSCreditEvaluationListener {
    private final SqsAsyncClient client;
    private final SQSCreditEvaluationProperties properties;
    private final Function<Message, Mono<Void>> processor;
    private String operation;
    
    private static final String OPERATION_NAME = "SQSCreditEvaluationListener";

    private Scheduler scheduler;
    
    public SQSCreditEvaluationListener start() {
        this.operation = "MessageFrom:" + properties.queueUrl();
        this.scheduler = Schedulers.newBoundedElastic(
            properties.numberOfThreads(),
            Integer.MAX_VALUE,
            "sqs-listener"
        );
        
        for (var i = 0; i < properties.numberOfThreads(); i++) {
            listenRetryRepeat().subscribeOn(scheduler)
                .subscribe();
        }
        return this;
    }

    private Flux<Void> listenRetryRepeat() {
        return listen()
            .retryWhen(Retry.backoff(Long.MAX_VALUE, Duration.ofSeconds(5))
                .maxBackoff(Duration.ofMinutes(1))
                .doBeforeRetry(rs -> log.warn("Retrying after error: {}", rs.failure().getMessage()))
            )
            .doOnError(e -> log.error("Error listening sqs queue", e))
            .repeat();
    }

    private Flux<Void> listen() {
        return getMessages()
                .flatMap(message -> processor.apply(message)
                        .name(OPERATION_NAME)
                        .tag("operation", operation)
                        .then(confirm(message)))
                .onErrorContinue((e, o) -> log.error("Error listening sqs message", e));
    }

    private Mono<Void> confirm(Message message) {
        return Mono.fromCallable(() -> getDeleteMessageRequest(message.receiptHandle()))
                .flatMap(request -> Mono.fromFuture(client.deleteMessage(request)))
                .then();
    }

    private Flux<Message> getMessages() {
        return Mono.fromCallable(this::getReceiveMessageRequest)
                .flatMap(request -> Mono.fromFuture(client.receiveMessage(request)))
                .doOnNext(response -> {
                    if (response.hasMessages()) log.debug("{} received messages from sqs", response.messages().size());
                })
                .flatMapMany(response -> Flux.fromIterable(response.messages()));
    }

    private ReceiveMessageRequest getReceiveMessageRequest() {
        return ReceiveMessageRequest.builder()
                .queueUrl(properties.queueUrl())
                .maxNumberOfMessages(properties.maxNumberOfMessages())
                .waitTimeSeconds(properties.waitTimeSeconds())
                .visibilityTimeout(properties.visibilityTimeoutSeconds())
                .build();
    }

    private DeleteMessageRequest getDeleteMessageRequest(String receiptHandle) {
        return DeleteMessageRequest.builder()
                .queueUrl(properties.queueUrl())
                .receiptHandle(receiptHandle)
                .build();
    }
    
    @PreDestroy
    public void shutdown() {
        if (scheduler != null) {
            log.info("Shutting down SQS listener scheduler...");
            scheduler.dispose();
        }
    }
}
