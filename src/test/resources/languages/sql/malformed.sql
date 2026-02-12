-- This is a malformed SQL file
SELECT * FROM
-- Missing table name

INSERT INTO (id, name) VALUES (1, 'Test');
-- Missing table name

CREATE TABLE {
    invalid syntax
};

DROP TABLE ;

!!!! NOT SQL AT ALL !!!!

SELECT 1;
