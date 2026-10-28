-- ============================================================
--  장식(decorations) · 정령 외형(spirit_variants) 마이그레이션
--  ddl.sql의 "3-1"(spirit_variants 부분)·"5-2" 섹션만 떼어낸 증분 스크립트입니다.
--  이미 ddl.sql 전체를 반영한 DB라면 실행할 필요 없습니다.
-- ============================================================

CREATE TABLE IF NOT EXISTS spirit_variants
(
    id                BIGSERIAL PRIMARY KEY,
    code              VARCHAR(30)  NOT NULL UNIQUE,
    variant_name      VARCHAR(50)  NOT NULL,
    unlock_metric     VARCHAR(30)  NOT NULL,
    unlock_threshold  INT          NOT NULL DEFAULT 0,
    weight            INT          NOT NULL DEFAULT 1,
    sort_order        INT          NOT NULL DEFAULT 0,
    asset_key         VARCHAR(100) NOT NULL,
    image_url         TEXT,
    image_file_name   VARCHAR(150)
);

CREATE TABLE IF NOT EXISTS user_spirit_variant_unlocks
(
    id          BIGSERIAL PRIMARY KEY,
    user_id     BIGINT NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    variant_id  BIGINT NOT NULL REFERENCES spirit_variants (id),
    unlocked_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_user_variant_unlock UNIQUE (user_id, variant_id)
);

ALTER TABLE spirits
    ADD COLUMN IF NOT EXISTS variant_id BIGINT REFERENCES spirit_variants (id);

CREATE TABLE IF NOT EXISTS decorations
(
    id               BIGSERIAL PRIMARY KEY,
    code             VARCHAR(30)  NOT NULL UNIQUE,
    decoration_name  VARCHAR(50)  NOT NULL,
    price            BIGINT       NOT NULL,
    asset_key        VARCHAR(100) NOT NULL,
    image_url        TEXT,
    image_file_name  VARCHAR(150)
);

CREATE TABLE IF NOT EXISTS user_decorations
(
    id            BIGSERIAL PRIMARY KEY,
    user_id       BIGINT  NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    decoration_id BIGINT  NOT NULL REFERENCES decorations (id),
    purchased_at  TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    is_installed  BOOLEAN NOT NULL DEFAULT FALSE,
    CONSTRAINT uq_user_decoration UNIQUE (user_id, decoration_id)
);
