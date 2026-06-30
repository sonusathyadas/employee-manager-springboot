-- V2: Add the reporting_manager column to the employees table.
-- Required after adding the reportingManager field to the Employee entity.
ALTER TABLE employees
    ADD COLUMN reporting_manager VARCHAR(200);
