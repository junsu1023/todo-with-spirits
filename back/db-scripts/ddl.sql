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
-- CREATE TYPE notification_category AS ENUM ('SYSTEM', 'EVENT', 'SPIRIT');
-- CREATE TYPE card_type AS ENUM ('TODAY', 'MONTHLY');
-- CREATE TYPE card_status AS ENUM ('PROCESSING', 'READY', 'FAILED');
-- CREATE TYPE spirit_status AS ENUM ('GROWING', 'ADULT', 'DEPARTED');
-- CREATE TYPE spirit_temperament AS ENUM ('CURIOUS', 'EASYGOING', 'ACTIVE', 'SOCIABLE', 'CAUTIOUS', 'METICULOUS', 'PLAYFUL', 'INDEPENDENT');
-- CREATE TYPE spirit_action_place AS ENUM ('BOOKSHELF', 'WORKBENCH', 'FRONT_YARD', 'TEA_TABLE', 'BED');

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

-- 이메일 인증 코드 테이블 (6자리 숫자, 5분 유효). 회원가입 이전에 발급되므로 특정 유저에 종속되지 않음
CREATE TABLE email_verification_codes
(
    id          BIGSERIAL PRIMARY KEY,
    email       VARCHAR(255)             NOT NULL, -- 인증 대상 이메일
    code        VARCHAR(6)               NOT NULL, -- 6자리 숫자 코드 (여러 이메일이 동시에 같은 코드를 받을 수 있어 UNIQUE 아님)
    verified_at TIMESTAMP WITH TIME ZONE,           -- null: 미사용
    expires_at  TIMESTAMP WITH TIME ZONE NOT NULL,
    created_at  TIMESTAMP WITH TIME ZONE          DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_email_verification_codes_email ON email_verification_codes (email);

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

-- ------------------------------------------------------------
--  3-1. 정령 육성(알 선택 ~ 성장 ~ 성체) 시스템
-- ------------------------------------------------------------

-- 알 마스터 (초기 튜토리얼 이후 3~5개 중 1개 선택)
-- 콘텐츠(이름/설명/이미지)는 기획 확정 전까지 TBD. 최소 1개는 튜토리얼 기본알로 시드 필요
CREATE TABLE spirit_egg_options
(
    id          BIGSERIAL PRIMARY KEY,
    code        VARCHAR(30)  NOT NULL UNIQUE,
    egg_name    VARCHAR(50)  NOT NULL,
    description TEXT,
    image_url   TEXT,
    is_tutorial BOOLEAN      NOT NULL DEFAULT FALSE, -- 최초 가입 튜토리얼에서 고정 제공되는 알인지
    is_active   BOOLEAN      NOT NULL DEFAULT TRUE,
    sort_order  INT          NOT NULL DEFAULT 0
);

-- 유저에게 제시된 알 선택지 (재조회 시 같은 3~5개가 유지되도록 offer를 고정)
CREATE TABLE spirit_egg_offers
(
    id            BIGSERIAL PRIMARY KEY,
    user_id       BIGINT  NOT NULL,
    egg_option_id BIGINT  NOT NULL,
    is_selected   BOOLEAN NOT NULL DEFAULT FALSE,
    created_at    TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_egg_offers_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
    CONSTRAINT fk_egg_offers_option FOREIGN KEY (egg_option_id) REFERENCES spirit_egg_options (id)
);

CREATE INDEX idx_egg_offers_user ON spirit_egg_offers (user_id, created_at DESC);

-- 정령별 선천기질 (2~3개 부여, 화면에는 수치가 아닌 태그로만 노출)
CREATE TABLE spirit_temperaments
(
    id          BIGSERIAL PRIMARY KEY,
    spirit_id   BIGINT      NOT NULL,
    temperament VARCHAR(20) NOT NULL, -- spirit_temperament ENUM
    created_at  TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_spirit_temperaments_spirit FOREIGN KEY (spirit_id) REFERENCES spirits (id) ON DELETE CASCADE,
    CONSTRAINT uq_spirit_temperament UNIQUE (spirit_id, temperament)
);

-- 행동장소 마스터 (5곳 고정)
CREATE TABLE spirit_action_places
(
    id                     BIGSERIAL PRIMARY KEY,
    code                   VARCHAR(30) NOT NULL UNIQUE, -- spirit_action_place ENUM
    place_name             VARCHAR(50) NOT NULL,
    representative_action  VARCHAR(50) NOT NULL,
    sort_order             INT         NOT NULL DEFAULT 0
);

-- 장소별 행동예시 (오브젝트 설치 여부에 따라 노출되는 행동 구분)
CREATE TABLE spirit_place_actions
(
    id              BIGSERIAL PRIMARY KEY,
    place_id        BIGINT      NOT NULL,
    action_text     VARCHAR(50) NOT NULL,
    requires_object BOOLEAN     NOT NULL DEFAULT FALSE, -- 해당 장소의 오브젝트(가구)가 설치되어 있어야 노출되는 행동인지
    sort_order      INT         NOT NULL DEFAULT 0,
    CONSTRAINT fk_spirit_place_actions_place FOREIGN KEY (place_id) REFERENCES spirit_action_places (id) ON DELETE CASCADE
);

-- 카테고리별 정령 표현 문구 (TODO 완료 시 category 기준으로 노출)
CREATE TABLE category_spirit_actions
(
    id          BIGSERIAL PRIMARY KEY,
    category    VARCHAR(20)  NOT NULL, -- category_type ENUM 재사용
    action_text VARCHAR(100) NOT NULL,
    sort_order  INT          NOT NULL DEFAULT 0
);

CREATE INDEX idx_category_spirit_actions_category ON category_spirit_actions (category);

-- 정령의 집/숲 오브젝트(가구) 마스터
CREATE TABLE spirit_home_objects
(
    id          BIGSERIAL PRIMARY KEY,
    code        VARCHAR(30) NOT NULL UNIQUE,
    object_name VARCHAR(50) NOT NULL,
    place_id    BIGINT      NOT NULL,
    icon_url    TEXT,
    CONSTRAINT fk_spirit_home_objects_place FOREIGN KEY (place_id) REFERENCES spirit_action_places (id)
);

-- 유저가 설치한 오브젝트 (정령 세대가 바뀌어도 유지되는 "집"/"숲" 자산 = 남은 장식)
CREATE TABLE user_spirit_home_objects
(
    id           BIGSERIAL PRIMARY KEY,
    user_id      BIGINT  NOT NULL,
    object_id    BIGINT  NOT NULL,
    installed_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_user_home_objects_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
    CONSTRAINT fk_user_home_objects_object FOREIGN KEY (object_id) REFERENCES spirit_home_objects (id),
    CONSTRAINT uq_user_home_object UNIQUE (user_id, object_id)
);

-- 정령이 실제로 수행한 장소/행동 로그(spirit_action_logs)는 tasks 테이블을 참조하므로
-- 4번 섹션(tasks 정의) 이후로 옮겨 정의함

-- 정령의 하루 기록 (일별 플레이버 로그)
CREATE TABLE spirit_daily_logs
(
    id         BIGSERIAL PRIMARY KEY,
    spirit_id  BIGINT NOT NULL,
    log_date   DATE   NOT NULL,
    content    TEXT   NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_spirit_daily_logs_spirit FOREIGN KEY (spirit_id) REFERENCES spirits (id) ON DELETE CASCADE,
    CONSTRAINT uq_spirit_daily_log UNIQUE (spirit_id, log_date)
);

-- 성체가 된 정령이 보낸 편지
CREATE TABLE spirit_letters
(
    id       BIGSERIAL PRIMARY KEY,
    spirit_id BIGINT NOT NULL,
    user_id  BIGINT NOT NULL,
    content  TEXT   NOT NULL,
    sent_at  TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    read_at  TIMESTAMP WITH TIME ZONE,
    CONSTRAINT fk_spirit_letters_spirit FOREIGN KEY (spirit_id) REFERENCES spirits (id) ON DELETE CASCADE,
    CONSTRAINT fk_spirit_letters_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
);

-- 성체가 된 정령이 여행에서 얻은 발견물
CREATE TABLE spirit_discoveries
(
    id            BIGSERIAL PRIMARY KEY,
    spirit_id     BIGINT       NOT NULL,
    user_id       BIGINT       NOT NULL,
    item_name     VARCHAR(100) NOT NULL,
    description   TEXT,
    icon_url      TEXT,
    discovered_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_spirit_discoveries_spirit FOREIGN KEY (spirit_id) REFERENCES spirits (id) ON DELETE CASCADE,
    CONSTRAINT fk_spirit_discoveries_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
);

-- spirits 테이블 확장: 알/성장(활동일 기준)/생애주기/진로
ALTER TABLE spirits
    ADD COLUMN egg_option_id      BIGINT REFERENCES spirit_egg_options (id),
    ADD COLUMN target_growth_days INT         NOT NULL DEFAULT 35, -- 개체별 목표 성장기간(활동일 기준). 28~42 사이 랜덤 부여, 평균 35
    ADD COLUMN active_days_count  INT         NOT NULL DEFAULT 0,  -- TODO/루틴을 완료해 생활기록이 생긴 날짜 수. 누적만 되고 감소하지 않음
    ADD COLUMN last_active_date   DATE,                            -- active_days_count 하루 중복 증가 방지용
    ADD COLUMN status             VARCHAR(20) NOT NULL DEFAULT 'GROWING', -- spirit_status ENUM
    ADD COLUMN matured_at         TIMESTAMP WITH TIME ZONE,        -- 성체(ADULT)가 된 시점
    ADD COLUMN departed_at        TIMESTAMP WITH TIME ZONE,        -- 여행을 떠나 이력으로 남은(DEPARTED) 시점
    ADD COLUMN career_growth_type VARCHAR(20);                     -- 성체가 될 때 확정되는 진로. growth_type ENUM 재사용 (exp가 가장 높은 축)

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

-- 정령이 실제로 수행한 장소/행동 로그 (자주 했던 행동 및 장소 집계용). tasks를 참조하므로 여기서 정의
CREATE TABLE spirit_action_logs
(
    id          BIGSERIAL PRIMARY KEY,
    spirit_id   BIGINT NOT NULL,
    place_id    BIGINT NOT NULL,
    action_id   BIGINT,          -- spirit_place_actions FK. 카테고리 문구 기반으로 발생한 경우 null
    task_id     BIGINT,          -- 이 행동을 유발한 TODO/루틴. 정령 자체 행동인 경우 null
    occurred_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_spirit_action_logs_spirit FOREIGN KEY (spirit_id) REFERENCES spirits (id) ON DELETE CASCADE,
    CONSTRAINT fk_spirit_action_logs_place FOREIGN KEY (place_id) REFERENCES spirit_action_places (id),
    CONSTRAINT fk_spirit_action_logs_action FOREIGN KEY (action_id) REFERENCES spirit_place_actions (id),
    CONSTRAINT fk_spirit_action_logs_task FOREIGN KEY (task_id) REFERENCES tasks (id) ON DELETE SET NULL
);

CREATE INDEX idx_spirit_action_logs_spirit ON spirit_action_logs (spirit_id, occurred_at DESC);

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

-- Task 미루기 이력 (SCHEDULE/ROUTINE 공통, append-only 이벤트 로그)
-- 미룬 횟수는 COUNT(*) WHERE task_id=? AND original_date=? 로 구함
-- ROUTINE의 현재 유효 날짜/시간은 같은 (task_id, original_date)의 최신(created_at) row 기준
CREATE TABLE task_postponements
(
    id             BIGSERIAL PRIMARY KEY,
    task_id        BIGINT      NOT NULL,
    user_id        BIGINT      NOT NULL,           -- 리포트 집계용 비정규화
    category       VARCHAR(20) NOT NULL,           -- 리포트 집계용 비정규화 (postpone 시점 task.category 스냅샷)
    task_type      VARCHAR(20) NOT NULL,           -- SCHEDULE / ROUTINE
    original_date  DATE        NOT NULL,           -- ROUTINE: 반복 규칙상 원래 occurrence 날짜 / SCHEDULE: 최초 예정일 (둘 다 불변, 집계 키)
    postponed_date DATE        NOT NULL,           -- 이 이동으로 새로 정해진 날짜
    postponed_time TIME,
    created_at     TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at     TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_task_postponements_task FOREIGN KEY (task_id) REFERENCES tasks (id) ON DELETE CASCADE,
    CONSTRAINT fk_task_postponements_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
);

CREATE INDEX idx_task_postponements_user_original_date ON task_postponements (user_id, original_date);
CREATE INDEX idx_task_postponements_task_original_date ON task_postponements (task_id, original_date);

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
    id                    BIGSERIAL PRIMARY KEY,
    user_id               BIGINT      NOT NULL UNIQUE,
    dark_mode             BOOLEAN     NOT NULL     DEFAULT FALSE,
    language              VARCHAR(10) NOT NULL     DEFAULT 'SYSTEM', -- language ENUM (SYSTEM, KO, EN, JA, ZH)
    dday_display_enabled  BOOLEAN     NOT NULL     DEFAULT TRUE,     -- 플랜(task) 디데이 표시
    auto_backup_enabled   BOOLEAN     NOT NULL     DEFAULT FALSE,
    created_at            TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at            TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_user_settings_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
);

-- ============================================================
--  16. 알림 설정 테이블
-- ============================================================
CREATE TABLE notification_settings
(
    id                          BIGSERIAL PRIMARY KEY,
    user_id                     BIGINT  NOT NULL UNIQUE,
    reminder_enabled            BOOLEAN NOT NULL DEFAULT TRUE,  -- 리마인드 알림
    postponed_task_enabled      BOOLEAN NOT NULL DEFAULT FALSE, -- 미룬 플랜 정기 알림
    routine_reminder_enabled    BOOLEAN NOT NULL DEFAULT TRUE,  -- 루틴 안내 알림
    streak_save_enabled         BOOLEAN NOT NULL DEFAULT TRUE,  -- 스트릭(연속 달성) 세이브 알림
    night_push_enabled          BOOLEAN NOT NULL DEFAULT FALSE, -- 야간 푸시 알림 수신
    created_at                  TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at                  TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_notification_settings_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
);

-- ============================================================
--  16-1. 알림 목록 테이블 (사용자에게 발송된 알림 이력)
-- ============================================================
CREATE TABLE notifications
(
    id         BIGSERIAL PRIMARY KEY,
    user_id    BIGINT      NOT NULL,
    category   VARCHAR(20) NOT NULL, -- notification_category ENUM (SYSTEM, EVENT, SPIRIT)
    content    TEXT        NOT NULL,
    is_read    BOOLEAN     NOT NULL DEFAULT FALSE,
    read_at    TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_notifications_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
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

-- 알림 목록 조회 (커서 기반 페이지네이션: created_at, id 복합키로 seek)
CREATE INDEX idx_notifications_user_created ON notifications (user_id, created_at DESC, id DESC);