-- Repeated statements to simulate a larger file
SELECT * FROM employees WHERE id = 1;
SELECT * FROM employees WHERE id = 2;
SELECT * FROM employees WHERE id = 3;
SELECT * FROM employees WHERE id = 4;
SELECT * FROM employees WHERE id = 5;
SELECT * FROM employees WHERE id = 6;
SELECT * FROM employees WHERE id = 7;
SELECT * FROM employees WHERE id = 8;
SELECT * FROM employees WHERE id = 9;
SELECT * FROM employees WHERE id = 10;
-- ... imagine 1000 more of these ...
INSERT INTO audit_log (event_time, description) VALUES (CURRENT_TIMESTAMP, 'Scan completed');
