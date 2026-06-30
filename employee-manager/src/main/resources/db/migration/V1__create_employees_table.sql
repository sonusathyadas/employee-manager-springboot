-- V1: Create the initial employees table
CREATE TABLE employees (
    id          BIGSERIAL PRIMARY KEY,
    first_name  VARCHAR(100)    NOT NULL,
    last_name   VARCHAR(100)    NOT NULL,
    email       VARCHAR(255)    NOT NULL,
    phone       VARCHAR(20),
    department  VARCHAR(100)    NOT NULL,
    job_title   VARCHAR(100)    NOT NULL,
    salary      NUMERIC(15, 2)  NOT NULL,
    hire_date   DATE            NOT NULL,
    status      VARCHAR(20)     NOT NULL DEFAULT 'ACTIVE',
    created_at  TIMESTAMP       NOT NULL,
    updated_at  TIMESTAMP       NOT NULL,
    CONSTRAINT uq_employees_email UNIQUE (email)
);
