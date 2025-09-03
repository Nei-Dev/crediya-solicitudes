CREATE TABLE state
(
    id_state    BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    name        VARCHAR(50) NOT NULL UNIQUE,
    description VARCHAR(255)
);

CREATE TABLE credit_type
(
    id_credit_type  BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    name            VARCHAR(50) NOT NULL UNIQUE,
    minimum_amount  NUMERIC     NOT NULL,
    maximum_amount  NUMERIC     NOT NULL,
    interest_rate   NUMERIC     NOT NULL,
    auto_validation BOOLEAN     NOT NULL DEFAULT FALSE
);

CREATE TABLE application
(
    id_application BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    amount         NUMERIC                                        NOT NULL,
    term           INTEGER                                        NOT NULL,
    email          VARCHAR(150)                                   NOT NULL,
    id_credit_type BIGINT REFERENCES credit_type (id_credit_type) NOT NULL,
    id_state       BIGINT REFERENCES state (id_state)             NOT NULL
);