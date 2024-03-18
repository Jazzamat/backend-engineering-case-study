
CREATE DATABASE dreambase DEFAULT CHARACTER SET utf8 COLLATE utf8_unicode_ci;
CREATE USER 'dreamgames'@'%' IDENTIFIED BY 'password';
GRANT ALL PRIVILEGES ON *.* TO 'dreamgames'@'%' WITH GRANT OPTION;
FLUSH PRIVILEGES;

USE dreambase;

-- CREATE TABLE IF NOT EXISTS user_seq (
--   id BIGINT NOT NULL AUTO_INCREMENT,
--   stub CHAR(1) NOT NULL DEFAULT '',
--   PRIMARY KEY (id),
--   UNIQUE KEY (stub)
-- );

-- CREATE TABLE IF NOT EXISTS tournament_seq (
--   id BIGINT NOT NULL AUTO_INCREMENT,
--   stub CHAR(1) NOT NULL DEFAULT '',
--   PRIMARY KEY (id),
--   UNIQUE KEY (stub)
-- );

-- CREATE TABLE IF NOT EXISTS tournament_entry_seq (
--   id BIGINT NOT NULL AUTO_INCREMENT,
--   stub CHAR(1) NOT NULL DEFAULT '',
--   PRIMARY KEY (id),
--   UNIQUE KEY (stub)
-- );

-- CREATE TABLE IF NOT EXISTS tournament_group_seq (
--   id BIGINT NOT NULL AUTO_INCREMENT,
--   stub CHAR(1) NOT NULL DEFAULT '',
--   PRIMARY KEY (id),
--   UNIQUE KEY (stub)
-- );
