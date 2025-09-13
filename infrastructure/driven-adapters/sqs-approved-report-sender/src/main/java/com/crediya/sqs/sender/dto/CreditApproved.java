package com.crediya.sqs.sender.dto;

import java.math.BigDecimal;

public record CreditApproved(
	Long idCreditApplication,
	BigDecimal amount
) {}
