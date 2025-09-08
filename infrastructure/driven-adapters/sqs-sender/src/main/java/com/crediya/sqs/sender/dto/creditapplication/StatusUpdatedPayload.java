package com.crediya.sqs.sender.dto.creditapplication;

public record StatusUpdatedPayload(
	String name,
	String amount,
	boolean isApproved
) {
}
