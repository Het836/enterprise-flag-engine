CREATE TABLE audit_log (
    id BIGSERIAL PRIMARY KEY,
    flag_key VARCHAR(255) NOT NULL,
    action_type VARCHAR(50) NOT NULL,
    changed_by VARCHAR(255) NOT NULL,
    old_value TEXT,
    new_value TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Change audit_logs to audit_log here:
CREATE INDEX idx_audit_log_flag_key ON audit_log(flag_key);