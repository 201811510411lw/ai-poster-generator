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

CREATE TABLE IF NOT EXISTS poster_asset (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_id BIGINT NOT NULL,
  asset_type VARCHAR(32) NOT NULL,
  filename VARCHAR(255) NOT NULL,
  original_filename VARCHAR(255) NOT NULL,
  content_type VARCHAR(64),
  file_size BIGINT,
  width INT,
  height INT,
  storage_path VARCHAR(512) NOT NULL,
  access_url VARCHAR(512),
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  INDEX idx_poster_asset_user_created (user_id, created_at),
  CONSTRAINT fk_poster_asset_user FOREIGN KEY (user_id) REFERENCES sys_user(id)
);

CREATE TABLE IF NOT EXISTS poster_generation_task (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_id BIGINT NOT NULL,
  material_type VARCHAR(32) NOT NULL,
  width INT NOT NULL,
  height INT NOT NULL,
  main_color VARCHAR(32),
  sub_color VARCHAR(32),
  brand_description VARCHAR(1024),
  style_description VARCHAR(1024),
  title VARCHAR(255),
  subtitle VARCHAR(255),
  activity_description VARCHAR(1024),
  design_requirement VARCHAR(2048),
  output_format VARCHAR(16) NOT NULL DEFAULT 'png',
  prompt_text TEXT,
  status VARCHAR(32) NOT NULL DEFAULT 'PENDING',
  result_filename VARCHAR(255),
  result_storage_path VARCHAR(512),
  result_image_url VARCHAR(512),
  error_message VARCHAR(1024),
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  INDEX idx_poster_generation_task_user_created (user_id, created_at),
  INDEX idx_poster_generation_task_status (status),
  CONSTRAINT fk_poster_generation_task_user FOREIGN KEY (user_id) REFERENCES sys_user(id)
);

CREATE TABLE IF NOT EXISTS poster_generation_asset (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  task_id BIGINT NOT NULL,
  asset_id BIGINT NOT NULL,
  asset_role VARCHAR(32) NOT NULL,
  sort_order INT NOT NULL DEFAULT 0,
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  INDEX idx_poster_generation_asset_task (task_id, sort_order),
  INDEX idx_poster_generation_asset_asset (asset_id),
  CONSTRAINT fk_poster_generation_asset_task FOREIGN KEY (task_id) REFERENCES poster_generation_task(id) ON DELETE CASCADE,
  CONSTRAINT fk_poster_generation_asset_asset FOREIGN KEY (asset_id) REFERENCES poster_asset(id) ON DELETE CASCADE
);
