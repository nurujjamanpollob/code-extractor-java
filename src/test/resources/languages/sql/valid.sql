-- Valid SQL
CREATE TABLE users (
    id INT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(50) NOT NULL,
    email VARCHAR(100) UNIQUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

INSERT INTO users (username, email) VALUES ('admin', 'admin@example.com');

SELECT u.id, u.username, p.profile_name
FROM users u
JOIN profiles p ON u.id = p.user_id
WHERE u.created_at > '2023-01-01'
ORDER BY u.username ASC;

UPDATE users SET email = 'new@example.com' WHERE id = 1;

DELETE FROM users WHERE id = 10;