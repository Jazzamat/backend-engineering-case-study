
CREATE DATABASE dreambase DEFAULT CHARACTER SET utf8 COLLATE utf8_unicode_ci;
CREATE USER 'dreamgames'@'%' IDENTIFIED BY 'password';
GRANT ALL PRIVILEGES ON *.* TO 'dreamgames'@'%' WITH GRANT OPTION;
FLUSH PRIVILEGES;
USE dreambase;