-- 1. ENUM 타입
CREATE TYPE task_type AS ENUM ('TODO', 'SCHEDULE', 'HABIT');
CREATE TYPE repeat_type AS ENUM ('NONE', 'DAILY', 'WEEKLY', 'MONTHLY');
CREATE TYPE growth_type AS ENUM ('FOCUS', 'VITALITY', 'PERSISTENCE', 'CREATIVITY');
CREATE TYPE oauth_provider AS ENUM ('KAKAO', 'APPLE', 'GOOGLE');

-- 2. 사용자 테이블
CREATE TABLE users
(
    id         BIGSERIAL PRIMARY KEY,
    email      VARCHAR(255) NOT NULL UNIQUE,
    nickname   VARCHAR(50)  NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- 3. 사용자 계정 연동 정보 테이블
CREATE TABLE user_social_accounts
(
    id               BIGSERIAL PRIMARY KEY,
    user_id          BIGINT         NOT NULL,
    provider         oauth_provider NOT NULL,
    provider_user_id VARCHAR(255)   NOT NULL, -- 소셜 서비스(카카오, 애플)에서 넘겨주는 사용자의 고유 ID 값
    created_at       TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
    -- 한 유저가 동일한 플랫폼으로 중복 연동하는 것을 방지
    CONSTRAINT unique_user_provider UNIQUE (user_id, provider),
    -- 특정 소셜 아이디로 로그인할 때 빠른 조회
    CONSTRAINT unique_provider_user_id UNIQUE (provider, provider_user_id)
);

-- 4. 정령 상태 테이블
CREATE TABLE spirit
(
    id              BIGSERIAL PRIMARY KEY,
    user_id         BIGINT      NOT NULL UNIQUE,
    spirit_name     VARCHAR(50) NOT NULL     DEFAULT '아기 정령',
    stage           INT         NOT NULL     DEFAULT 1,
    exp             INT         NOT NULL     DEFAULT 0,
    focus_exp       INT         NOT NULL     DEFAULT 0,
    vitality_exp    INT         NOT NULL     DEFAULT 0,
    persistence_exp INT         NOT NULL     DEFAULT 0,
    creativity_exp  INT         NOT NULL     DEFAULT 0,
    updated_at      TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
);

-- 5. 통합 To-do/일정/할일 테이블
CREATE TABLE tasks
(
    id           BIGSERIAL PRIMARY KEY,
    user_id      BIGINT       NOT NULL,
    type         task_type    NOT NULL,
    title        VARCHAR(255) NOT NULL,
    category     VARCHAR(50),
    task_time    TIME,
    repeat_rule  repeat_rule  NOT NULL    DEFAULT 'NONE',
    power        INT          NOT NULL    DEFAULT 10,
    power_type   growth_type  NOT NULL,
    is_completed BOOLEAN      NOT NULL    DEFAULT FALSE,
    task_date    DATE         NOT NULL,
    completed_at TIMESTAMP WITH TIME ZONE,
    created_at   TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at   TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
);

-- 6. 일일 성취 기록 및 리포트 테이블
CREATE TABLE daily_records
(
    id              BIGSERIAL PRIMARY KEY,
    user_id         BIGINT        NOT NULL,
    record_date     DATE          NOT NULL,
    total_power     INT           NOT NULL   DEFAULT 0,
    completion_rate NUMERIC(5, 2) NOT NULL   DEFAULT 0.00,
    interpretation  TEXT,
    created_at      TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
    CONSTRAINT unique_user_date UNIQUE (user_id, record_date)
);

-- 7. 인덱스
CREATE INDEX idx_social_accounts_search ON user_social_accounts (provider, provider_user_id);
CREATE INDEX idx_tasks_user_date ON tasks (user_id, task_date);
CREATE INDEX idx_daily_records_user_date ON daily_records (user_id, record_date);