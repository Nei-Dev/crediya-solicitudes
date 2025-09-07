package com.crediya.sqs.sender.dto.creditapplication;

public record StatusUpdatedPayload(
	Long idCreditApplication,
	String state
) {
}
