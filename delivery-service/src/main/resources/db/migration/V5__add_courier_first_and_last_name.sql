ALTER TABLE couriers
    RENAME COLUMN name TO first_name;

ALTER TABLE couriers
    ADD COLUMN last_name VARCHAR(255);