package com.crediya.sqs.sender.dto.creditapplication;

import com.crediya.model.creditapplication.Installment;

import java.util.List;

public record StatusUpdatedPayload(
	String name,
	String email,
	String amount,
	boolean isApproved,
	List<Installment> paymentPlan
) {}
