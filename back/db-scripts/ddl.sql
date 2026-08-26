-- ============================================================
--  1. ENUM 타입
-- ============================================================

-- CREATE TYPE task_type AS ENUM ('SCHEDULE', 'ROUTINE');
-- CREATE TYPE growth_type AS ENUM ('FOCUS', 'ENERGY', 'CONSISTENCY', 'CREATIVITY');
-- CREATE TYPE repeat_type AS ENUM ('NONE', 'DAILY', 'WEEKLY', 'MONTHLY');
-- CREATE TYPE category_type AS ENUM ('NONE', 'WORK_STUDY', 'HEALTH', 'LIFE', 'RELATIONSHIP', 'GROWTH', 'HOBBY', 'REST', 'FINANCE');
-- CREATE TYPE delete_scope AS ENUM ('ONLY_THIS', 'ALL_FUTURE');
-- CREATE TYPE plan_type AS ENUM ('FREE', 'MONTHLY', 'YEARLY');
-- CREATE TYPE provider AS ENUM ('KAKAO', 'APPLE', 'GOOGLE');
-- CREATE TYPE user_role AS ENUM ('USER', 'ADMIN');
-- CREATE TYPE interaction_type AS ENUM ('PET', 'FEED', 'PLAY');
-- CREATE TYPE achievement_type AS ENUM ('FIRST_CATEGORY_COMPLETE', 'CATEGORY_HALF_COMPLETE', 'CATEGORY_ALL_COMPLETE',
-- 'TOTAL_COMPLTE_COUNT', 'HIDDEN_ACHIEVEMENT', 'STREAK', 'SPIRIT_LEVEL', 'USER_LEVEL', 'SPIRIT_MASTER', 'CATEGORY_MASTER','BADGE_COLLECTOR');
-- CREATE TYPE card_type AS ENUM ('TODAY', 'MONTHLY');
-- CREATE TYPE card_status AS ENUM ('PROCESSING', 'READY', 'FAILED');

-- ============================================================
--  2. 사용자 관련 테이블
-- ============================================================
-- 사용자 테이블
CREATE TABLE users
(
    id                       BIGSERIAL PRIMARY KEY,
    email                    VARCHAR(255),                            -- null: social login
    password                 VARCHAR(255),                            -- null: social login
    nickname                 VARCHAR(50) NOT NULL,
    fullname                 VARCHAR(50),                             -- 본명
    birthday                 DATE,                                    -- 생년월일
    gender                   VARCHAR(10),                             -- gender ENUM
    profile_image_url        TEXT,
    email_verification_status VARCHAR(30) NOT NULL    DEFAULT 'UNVERIFIED', -- email_verification_status ENUM (UNVERIFIED, VERIFIED, NEEDS_REVERIFICATION)
    role                     VARCHAR(20) NOT NULL     DEFAULT 'USER', -- user_role ENUM
    is_premium               BOOLEAN     NOT NULL     DEFAULT FALSE,
    representative_spirit_id BIGINT,                                  -- spirits FK (순환 참조. ALTER로 후처리)
    deleted_at               TIMESTAMP WITH TIME ZONE,                -- soft delete
    created_at               TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at               TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_users_email UNIQUE (email)
);

-- 소셜 계정 연동 테이블
CREATE TABLE user_social_accounts
(
    id               BIGSERIAL PRIMARY KEY,
    user_id          BIGINT       NOT NULL,
    provider         VARCHAR(20)  NOT NULL, -- provider ENUM
    provider_user_id VARCHAR(255) NOT NULL,
    created_at       TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_social_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
    CONSTRAINT uq_user_provider UNIQUE (user_id, provider),
    CONSTRAINT uq_provider_user_id UNIQUE (provider, provider_user_id)
);

-- 리프레시 토큰 테이블
CREATE TABLE refresh_tokens
(
    id         BIGSERIAL PRIMARY KEY,
    user_id    BIGINT                   NOT NULL,
    token      VARCHAR(512)             NOT NULL UNIQUE,
    is_revoked BOOLEAN                  NOT NULL DEFAULT FALSE,
    expires_at TIMESTAMP WITH TIME ZONE NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE          DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_refresh_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
);

-- 이메일 인증 토큰 테이블
CREATE TABLE email_verification_tokens
(
    id          BIGSERIAL PRIMARY KEY,
    user_id     BIGINT                   NOT NULL,
    email       VARCHAR(255)             NOT NULL, -- 인증 대상 이메일 (발급 시점 스냅샷)
    token       VARCHAR(255)             NOT NULL UNIQUE,
    verified_at TIMESTAMP WITH TIME ZONE,           -- null: 미사용
    expires_at  TIMESTAMP WITH TIME ZONE NOT NULL,
    created_at  TIMESTAMP WITH TIME ZONE          DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_email_verification_tokens_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
);

CREATE INDEX idx_email_verification_tokens_user ON email_verification_tokens (user_id);

-- 구독/프리미엄 테이블
CREATE TABLE subscriptions
(
    id         BIGSERIAL PRIMARY KEY,
    user_id    BIGINT      NOT NULL UNIQUE,
    plan_type  VARCHAR(50) NOT NULL     DEFAULT 'FREE', -- plan_type ENUM
    started_at TIMESTAMP WITH TIME ZONE,
    expires_at TIMESTAMP WITH TIME ZONE,
    auto_renew BOOLEAN     NOT NULL     DEFAULT FALSE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_subscriptions_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
);

-- ============================================================
--  3. 정령 관련 테이블
-- ============================================================
-- 정령 테이블
CREATE TABLE spirits
(
    id              BIGSERIAL PRIMARY KEY,
    user_id         BIGINT      NOT NULL,
    spirit_name     VARCHAR(50) NOT NULL     DEFAULT '아기 정령',
    stage           INT         NOT NULL     DEFAULT 1,
    exp             INT         NOT NULL     DEFAULT 0,
    focus_exp       INT         NOT NULL     DEFAULT 0,
    energy_exp      INT         NOT NULL     DEFAULT 0,
    consistency_exp INT         NOT NULL     DEFAULT 0,
    creativity_exp  INT         NOT NULL     DEFAULT 0,
    image_url       TEXT,
    created_at      TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_spirits_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
);

ALTER TABLE users
    ADD CONSTRAINT fk_users_rep_spirit
        FOREIGN KEY (representative_spirit_id) REFERENCES spirits (id) ON DELETE SET NULL;

-- 정령 진화 이력 테이블
CREATE TABLE spirit_stage_history
(
    id         BIGSERIAL PRIMARY KEY,
    spirit_id  BIGINT NOT NULL,
    user_id    BIGINT NOT NULL,
    from_stage INT    NOT NULL,
    to_stage   INT    NOT NULL,
    evolved_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_stage_history_spirit FOREIGN KEY (spirit_id) REFERENCES spirits (id) ON DELETE CASCADE,
    CONSTRAINT fk_stage_history_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
);

--  정령 상호작용 로그 테이블 (일일 횟수 제한 체크용)
CREATE TABLE spirit_interactions
(
    id               BIGSERIAL PRIMARY KEY,
    spirit_id        BIGINT      NOT NULL,
    user_id          BIGINT      NOT NULL,
    interaction_type VARCHAR(20) NOT NULL, -- interaction_type ENUM
    interacted_at    TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_interactions_spirit FOREIGN KEY (spirit_id) REFERENCES spirits (id) ON DELETE CASCADE,
    CONSTRAINT fk_interactions_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
);

-- ============================================================
--  4. To-do, schedule, habit 관련 테이블
-- ============================================================
-- To-do, schedule, habit 테이블
CREATE TABLE tasks
(
    id                   BIGSERIAL PRIMARY KEY,
    user_id              BIGINT       NOT NULL,
    task_type            VARCHAR(20)  NOT NULL,                   -- task_type ENUM
    title                VARCHAR(255) NOT NULL,
    memo                 TEXT,
    category             VARCHAR(20)  NOT NULL    DEFAULT 'NONE', -- category_type ENUM
    start_date           DATE         NOT NULL,
    start_time           TIME,                                    -- 종일 일정이면 null
    end_date             DATE,
    end_time             TIME,
    repeat_type          VARCHAR(20)  NOT NULL    DEFAULT 'NONE', -- repeat_type ENUM
    repeat_end_date      DATE,
    growth_type          VARCHAR(20)  NOT NULL,                   -- growth_type ENUM
    growth_value         INT          NOT NULL    DEFAULT 10,     -- 서버가 계산하여 저장 (클라이언트 미전달)
    is_completed         BOOLEAN      NOT NULL    DEFAULT FALSE,
    is_important         BOOLEAN      NOT NULL    DEFAULT FALSE,
    is_all_day           BOOLEAN      NOT NULL    DEFAULT FALSE,
    is_public            BOOLEAN      NOT NULL    DEFAULT FALSE,
    exclude_holiday      BOOLEAN      NOT NULL    DEFAULT FALSE,
    notification_minutes INT,
    notification_at      TIMESTAMP WITH TIME ZONE,                -- 알림 발송 기준 시각 (23:59 - notification_minutes)
    completed_at         TIMESTAMP WITH TIME ZONE,
    created_at           TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at           TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_tasks_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
);

-- 매주 반복 요일 (WEEKLY)
CREATE TABLE IF NOT EXISTS task_repeat_days_of_week
(
    task_id     BIGINT      NOT NULL,
    day_of_week VARCHAR(10) NOT NULL,
    CONSTRAINT fk_rdow_task FOREIGN KEY (task_id) REFERENCES tasks (id) ON DELETE CASCADE,
    CONSTRAINT pk_task_repeat_days_of_week PRIMARY KEY (task_id, day_of_week)
);

-- 매월 반복 일 (MONTHLY)
CREATE TABLE IF NOT EXISTS task_repeat_days_of_month
(
    task_id      BIGINT NOT NULL,
    day_of_month INT    NOT NULL,
    CONSTRAINT fk_rdom_task FOREIGN KEY (task_id) REFERENCES tasks (id) ON DELETE CASCADE,
    CONSTRAINT pk_task_repeat_days_of_month PRIMARY KEY (task_id, day_of_month)
);

-- 루틴 날짜별 완료 기록 (ROUTINE 전용)
CREATE TABLE routine_completions
(
    id              BIGSERIAL PRIMARY KEY,
    task_id         BIGINT                   NOT NULL,
    completion_date DATE                     NOT NULL,
    completed_at    TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_routine_completions_task FOREIGN KEY (task_id) REFERENCES tasks (id) ON DELETE CASCADE,
    CONSTRAINT uq_routine_completions UNIQUE (task_id, completion_date)
);

-- 일일 기록 테이블
CREATE TABLE daily_records
(
    id                  BIGSERIAL PRIMARY KEY,
    user_id             BIGINT        NOT NULL,
    record_date         DATE          NOT NULL,
    total_count         INT           NOT NULL   DEFAULT 0,
    completed_count     INT           NOT NULL   DEFAULT 0,
    completion_rate     NUMERIC(5, 2) NOT NULL   DEFAULT 0.00,
    earned_growth_power INT           NOT NULL   DEFAULT 0,
    interpretation      TEXT, -- 기록 화면 코멘트 (서버/AI 생성)
    created_at          TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at          TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_daily_records_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
    CONSTRAINT uq_daily_records_user_date UNIQUE (user_id, record_date)
);

-- 월간 기록 테이블
CREATE TABLE monthly_records
(
    id                       BIGSERIAL PRIMARY KEY,
    user_id                  BIGINT        NOT NULL,
    year                     INT           NOT NULL,
    month                    INT           NOT NULL,
    total_completed_count    INT           NOT NULL   DEFAULT 0,
    completion_rate          NUMERIC(5, 2) NOT NULL   DEFAULT 0.00,
    accumulated_growth_power INT           NOT NULL   DEFAULT 0,
    top_growth_type          VARCHAR(20), -- growth_type ENUM
    created_at               TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at               TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_monthly_records_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
    CONSTRAINT uq_monthly_records_user_ym UNIQUE (user_id, year, month),
    CONSTRAINT chk_monthly_records_year CHECK (year BETWEEN 2000 AND 2100
        ),
    CONSTRAINT chk_monthly_records_month CHECK (month BETWEEN 1 AND 12)
);

-- 공휴일 테이블
CREATE TABLE holiday
(
    id           INT GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
    holiday_date DATE         NOT NULL UNIQUE,
    name         VARCHAR(255) NOT NULL,
    created_at   TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_holiday_date ON holiday (holiday_date);

-- ============================================================
--  5. 공통 정보 or 설정 관련 테이블
-- ============================================================
-- 업적 마스터 테이블
CREATE TABLE achievement_config
(
    id           BIGSERIAL PRIMARY KEY,
    code         VARCHAR(100) NOT NULL UNIQUE,
    title        VARCHAR(255) NOT NULL,
    description  TEXT         NOT NULL,
    icon_url     TEXT,
    target_value INT          NOT NULL,
    created_at   TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- 사용자 업적 달성 테이블
CREATE TABLE user_achievements
(
    id                    BIGSERIAL PRIMARY KEY,
    user_id               BIGINT  NOT NULL,
    achievement_config_id BIGINT  NOT NULL,
    current_value         INT     NOT NULL         DEFAULT 0,
    is_unlocked           BOOLEAN NOT NULL         DEFAULT FALSE,
    unlocked_at           TIMESTAMP WITH TIME ZONE,
    created_at            TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at            TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_user_achievements_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
    CONSTRAINT fk_user_achievements_definition FOREIGN KEY (achievement_config_id) REFERENCES achievement_config (id) ON DELETE CASCADE,
    CONSTRAINT uq_user_achievement UNIQUE (user_id, achievement_config_id)
);

-- ============================================================
--  14. 공유 카드 테이블  (비동기 생성 → 202 Accepted 패턴)
-- ============================================================
CREATE TABLE share_cards
(
    id           BIGSERIAL PRIMARY KEY,
    user_id      BIGINT      NOT NULL,
    card_type    VARCHAR(20) NOT NULL,
    card_status  VARCHAR(20) NOT NULL     DEFAULT 'PROCESSING',
    template_id  VARCHAR(100),
    target_date  DATE, -- card_type = TODAY
    target_year  INT,  -- card_type = MONTHLY
    target_month INT,  -- card_type = MONTHLY
    download_url TEXT, -- READY 상태가 되면 채워짐 (Presigned URL)
    expires_at   TIMESTAMP WITH TIME ZONE,
    created_at   TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at   TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_share_cards_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
);

-- ============================================================
--  15. 앱 설정 테이블
-- ============================================================
CREATE TABLE user_settings
(
    id                  BIGSERIAL PRIMARY KEY,
    user_id             BIGINT      NOT NULL UNIQUE,
    dark_mode           BOOLEAN     NOT NULL     DEFAULT FALSE,
    language            VARCHAR(10) NOT NULL     DEFAULT 'ko',
    auto_backup_enabled BOOLEAN     NOT NULL     DEFAULT FALSE,
    created_at          TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at          TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_user_settings_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
);

-- ============================================================
--  16. 알림 설정 테이블
-- ============================================================
CREATE TABLE notification_settings
(
    id                          BIGSERIAL PRIMARY KEY,
    user_id                     BIGINT  NOT NULL UNIQUE,
    routine_reminder_enabled    BOOLEAN NOT NULL         DEFAULT TRUE,
    routine_reminder_time       TIME, -- NULL이면 앱 기본값 사용
    spirit_notification_enabled BOOLEAN NOT NULL         DEFAULT TRUE,
    created_at                  TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at                  TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_notification_settings_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
);

-- ============================================================
--  17. 인덱스
-- ============================================================

-- 소셜 로그인 조회 (provider + provider_user_id)
CREATE INDEX idx_social_accounts_lookup ON user_social_accounts (provider, provider_user_id);

-- 리프레시 토큰 조회 및 유효 토큰 필터
CREATE INDEX idx_refresh_tokens_user ON refresh_tokens (user_id);
CREATE INDEX idx_refresh_tokens_active ON refresh_tokens (token) WHERE NOT is_revoked;

-- 정령 목록 / 인터랙션 일별 집계
CREATE INDEX idx_spirits_user ON spirits (user_id);
CREATE INDEX idx_stage_history_spirit ON spirit_stage_history (spirit_id);
-- CREATE INDEX idx_interactions_user_date ON spirit_interactions (user_id, (interacted_at::date));
-- CREATE INDEX idx_interactions_user_date ON spirit_interactions (user_id, CAST(interacted_at AS DATE));

-- 태스크 조회 (캘린더, 목록)
CREATE INDEX idx_tasks_user_date ON tasks (user_id, start_date);
-- 스케줄 전용 날짜 범위 인덱스 (기존 인덱스명 유지 시)
CREATE INDEX idx_tasks_user_start_date ON tasks (user_id, start_date);
-- 루틴 종료일 조건 최적화용 복합 인덱스 추가
CREATE INDEX idx_tasks_calendar_routine ON tasks (user_id, task_type, start_date, repeat_end_date);
CREATE INDEX idx_tasks_user_type ON tasks (user_id, task_type);
CREATE INDEX idx_tasks_user_category ON tasks (user_id, category);
-- 알림 발송 대상 조회 (알림 서비스용)
CREATE INDEX idx_tasks_notification_at ON tasks (notification_at) WHERE notification_at IS NOT NULL;

-- 기록
CREATE INDEX idx_daily_records_user_date ON daily_records (user_id, record_date);
CREATE INDEX idx_monthly_records_user_ym ON monthly_records (user_id, year, month);

-- 공유 카드 상태 조회
CREATE INDEX idx_share_cards_user_status ON share_cards (user_id, card_status);

-- 업적 진행 조회
CREATE INDEX idx_user_achievements_user ON user_achievements (user_id);