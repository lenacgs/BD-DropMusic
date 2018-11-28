CREATE USER 'DBUser'@'localhost' IDENTIFIED BY 'password';
CREATE DATABASE DropMusicDatabase;
USE DropMusicDatabase;
GRANT ALL PRIVILEGES ON DropMusicDatabase.* TO 'DBUser'@'localhost' WITH GRANT OPTION;