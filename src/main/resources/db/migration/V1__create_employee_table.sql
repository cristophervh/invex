CREATE TABLE employee (
    id               BIGINT       NOT NULL AUTO_INCREMENT,
    first_name       VARCHAR(50)  NOT NULL,
    middle_name      VARCHAR(50),
    last_name        VARCHAR(50)  NOT NULL,
    second_last_name VARCHAR(50),
    age              INT          NOT NULL,
    gender           VARCHAR(10)  NOT NULL,
    birth_date       DATE         NOT NULL,
    position         VARCHAR(80)  NOT NULL,
    created_at       TIMESTAMP(6) NOT NULL,
    active           BOOLEAN      NOT NULL DEFAULT TRUE,
    version          BIGINT       NOT NULL DEFAULT 0,
    CONSTRAINT pk_employee PRIMARY KEY (id)
);

CREATE INDEX idx_employee_identity ON employee (last_name, first_name, birth_date);
