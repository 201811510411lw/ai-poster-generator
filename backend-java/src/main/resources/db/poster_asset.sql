USE ai_poster_generator;

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
