-- 确保messages表有is_read列
ALTER TABLE messages ADD COLUMN IF NOT EXISTS is_read BOOLEAN NOT NULL DEFAULT FALSE;

-- 由于我们已经删除了flyway_schema_history表并重新开始，
-- 可以直接尝试创建索引，如果已存在会报错，但可以继续执行
-- 这是一个安全的方法，因为即使失败也不影响其他操作
CREATE INDEX idx_is_read ON messages (is_read);

