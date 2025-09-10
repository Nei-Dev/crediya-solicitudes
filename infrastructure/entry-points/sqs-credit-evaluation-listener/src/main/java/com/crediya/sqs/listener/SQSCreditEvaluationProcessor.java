package com.crediya.sqs.listener;

import com.crediya.model.creditapplication.StateCreditApplication;
import com.crediya.model.creditapplication.ports.IUpdateStateCreditApplicationUseCase;
import com.google.gson.Gson;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import software.amazon.awssdk.services.sqs.model.Message;

import java.util.function.Function;

@Slf4j
@Service
@RequiredArgsConstructor
public class SQSCreditEvaluationProcessor implements Function<Message, Mono<Void>> {
    
    private final IUpdateStateCreditApplicationUseCase updateStateCreditApplicationUseCase;
    private final Gson gson = new Gson();

    @Override
    public Mono<Void> apply(Message message) {
        
        ResultCreditEvaluation result = gson.fromJson(message.body(), ResultCreditEvaluation.class);
        return Mono.fromCallable(() -> updateStateCreditApplicationUseCase.execute(
            result.idCreditApplication(),
            StateCreditApplication.valueOf(result.result())
        )).then();
        
    }
}
