package com.crediya.sqs.listener;

public record ResultCreditEvaluation(
	Long idCreditApplication,
	String result
) {}
