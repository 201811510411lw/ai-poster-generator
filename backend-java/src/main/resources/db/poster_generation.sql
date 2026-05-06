USE ai_poster_generator;

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
