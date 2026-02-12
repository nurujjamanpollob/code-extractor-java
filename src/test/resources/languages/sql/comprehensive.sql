-- Data Definition Language (DDL)
CREATE TABLE employees (
    id INT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    department_id INT,
    salary DECIMAL(10, 2),
    hire_date DATE
);

ALTER TABLE employees ADD COLUMN email VARCHAR(255);

DROP TABLE IF EXISTS temp_data;

/* Data Manipulation Language (DML) 
   with various join types and subqueries */
INSERT INTO employees (id, name, department_id, salary, hire_date)
VALUES (1, 'John Doe', 101, 50000.00, '2023-01-01');

UPDATE employees 
SET salary = salary * 1.10 
WHERE department_id = (SELECT id FROM departments WHERE name = 'Engineering');

DELETE FROM employees WHERE hire_date < '2020-01-01';

-- Common Table Expressions (CTE) and Window Functions
WITH DeptAvg AS (
    SELECT 
        department_id, 
        AVG(salary) OVER(PARTITION BY department_id) as average_salary
    FROM employees
)
SELECT 
    e.name, 
    e.salary, 
    da.average_salary
FROM employees e
JOIN DeptAvg da ON e.department_id = da.department_id
WHERE e.salary > da.average_salary;

-- Data Control Language (DCL)
GRANT SELECT, INSERT ON employees TO 'hr_user';
REVOKE DELETE ON employees FROM 'temp_user';

-- Transaction Control Language (TCL)
BEGIN;
UPDATE employees SET salary = salary + 1000;
COMMIT;

BEGIN;
DELETE FROM employees WHERE id = 999;
ROLLBACK;

-- Stored Procedure and Function
CREATE PROCEDURE GetEmployeeById(IN emp_id INT)
BEGIN
    SELECT * FROM employees WHERE id = emp_id;
END;

CREATE FUNCTION CalculateBonus(emp_id INT) RETURNS DECIMAL(10, 2)
DETERMINISTIC
BEGIN
    DECLARE bonus DECIMAL(10, 2);
    SELECT salary * 0.1 INTO bonus FROM employees WHERE id = emp_id;
    RETURN bonus;
END;

-- Strings with escaped characters and comments
SELECT 'It''s a SQL test' as msg, "Double ""quotes"" test" as msg2
FROM dual; -- Single line comment at end of line

/* 
   Multi-line comment
   containing SQL keywords: SELECT, INSERT, DELETE
*/
SELECT * FROM employees;
