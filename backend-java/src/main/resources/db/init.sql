CREATE DATABASE IF NOT EXISTS ai_poster_generator
  DEFAULT CHARACTER SET utf8mb4
  DEFAULT COLLATE utf8mb4_unicode_ci;

USE ai_poster_generator;

CREATE TABLE IF NOT EXISTS sys_user (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  username VARCHAR(64) NOT NULL UNIQUE,
  password_hash VARCHAR(255) NOT NULL,
  nickname VARCHAR(64),
  avatar VARCHAR(255),
  role VARCHAR(32) DEFAULT 'USER',
  status TINYINT DEFAULT 1,
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- 默认测试账号：admin / admin123
-- BCrypt hash generated for password: admin123
INSERT INTO sys_user (username, password_hash, nickname, role, status)
VALUES (
  'admin',
  '$2a$10$CXoy1jW5FSv2ar5S8X8CbevtRdzW4D.o9p0sXwR4hCHFkWicXn4/S',
  '管理员',
  'ADMIN',
  1
)
ON DUPLICATE KEY UPDATE username = username;
