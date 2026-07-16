CREATE TABLE environments (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE,
    api_key VARCHAR(255) NOT NULL UNIQUE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE feature_flags (
    id BIGSERIAL PRIMARY KEY,
    flag_key VARCHAR(100) NOT NULL UNIQUE, -- Unified naming
    description TEXT,
    type VARCHAR(20) NOT NULL DEFAULT 'BOOLEAN',
    is_enabled BOOLEAN NOT NULL DEFAULT FALSE,
    targeting_rules JSONB NOT NULL DEFAULT '[]'::jsonb,
    environment_id BIGINT REFERENCES environments(id) ON DELETE CASCADE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_flags_environment ON feature_flags(environment_id);

-- CRITICAL FIX: Insert a seed environment so ID 1 exists!
INSERT INTO environments (id, name, api_key)
VALUES (1, 'Development', 'dev-api-key-123XYZ')
ON CONFLICT (id) DO NOTHING;