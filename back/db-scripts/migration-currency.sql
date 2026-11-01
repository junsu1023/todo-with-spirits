-- ============================================================
--  재화 원장 마이그레이션
--  ddl.sql의 "5-1. 재화 테이블" 섹션만 떼어낸 증분 스크립트입니다.
--  이미 ddl.sql 전체를 반영한 DB라면 실행할 필요 없습니다.
-- ============================================================

CREATE TABLE IF NOT EXISTS currency_wallets
(
    user_id    BIGINT PRIMARY KEY REFERENCES users (id) ON DELETE CASCADE,
    balance    BIGINT NOT NULL DEFAULT 0,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS currency_transactions
(
    id                    BIGSERIAL PRIMARY KEY,
    user_id               BIGINT      NOT NULL,
    amount                BIGINT      NOT NULL,
    reason                VARCHAR(30) NOT NULL,
    task_id               BIGINT,
    task_completion_date  DATE,
    created_at            TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at            TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_currency_transactions_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
    CONSTRAINT fk_currency_transactions_task FOREIGN KEY (task_id) REFERENCES tasks (id) ON DELETE SET NULL,
    CONSTRAINT uq_currency_tx_task_completion UNIQUE (task_id, task_completion_date)
);

CREATE INDEX IF NOT EXISTS idx_currency_transactions_user ON currency_transactions (user_id, created_at DESC);
