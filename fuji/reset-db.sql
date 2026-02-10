-- Drop and recreate database
DROP DATABASE IF EXISTS `fuji_db`;
CREATE DATABASE `fuji_db` CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE `fuji_db`;

-- Now run init.sql
SOURCE init.sql;
