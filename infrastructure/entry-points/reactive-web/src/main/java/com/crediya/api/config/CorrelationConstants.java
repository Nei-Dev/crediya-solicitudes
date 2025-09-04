package com.crediya.api.config;

import lombok.experimental.UtilityClass;

@UtilityClass
public final class CorrelationConstants {
    public static final String CORRELATION_ID_KEY = "correlationId";
    public static final String X_CORRELATION_ID = "X-Correlation-ID";
    public static final int CORRELATION_ID_LENGTH = 10;
}

