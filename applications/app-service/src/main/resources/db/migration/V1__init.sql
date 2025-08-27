CREATE TABLE estado
(
    id_estado   BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    nombre      VARCHAR(50) NOT NULL UNIQUE,
    descripcion VARCHAR(255)
);

CREATE TABLE tipo_prestamo
(
    id_tipo_prestamo      BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    nombre                VARCHAR(50) NOT NULL UNIQUE,
    monto_minimo          NUMERIC     NOT NULL,
    monto_maximo          NUMERIC     NOT NULL,
    tasa_interes          NUMERIC     NOT NULL,
    validacion_automatica BOOLEAN     NOT NULL DEFAULT FALSE
);

CREATE TABLE solicitud
(
    id_solicitud     BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    monto            NUMERIC                                            NOT NULL,
    plazo            INTEGER                                            NOT NULL,
    email            VARCHAR(150)                                       NOT NULL,
    id_tipo_prestamo BIGINT REFERENCES tipo_prestamo (id_tipo_prestamo) NOT NULL,
    id_estado        BIGINT REFERENCES estado (id_estado)               NOT NULL
);