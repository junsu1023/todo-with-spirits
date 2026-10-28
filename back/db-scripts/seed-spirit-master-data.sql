-- ============================================================
--  정령 육성 시스템 마스터 데이터 시드
--  대상: spirit_action_places, spirit_place_actions, category_spirit_actions,
--        spirit_home_objects, spirit_egg_options
--  전제: ddl.sql의 3-1. 정령 육성 시스템 테이블이 이미 생성되어 있어야 함
--
--  재실행 주의:
--   - spirit_egg_options / spirit_action_places / spirit_home_objects 는 code가 UNIQUE라서
--     ON CONFLICT (code) DO NOTHING 으로 재실행해도 안전함
--   - spirit_place_actions / category_spirit_actions 는 자연키(unique)가 없어서
--     최초 1회 실행을 전제로 함. 재실행하면 행이 중복 삽입되니, 재시드가 필요하면
--     아래 두 테이블만 TRUNCATE 하고 다시 실행할 것
--     TRUNCATE TABLE spirit_place_actions, category_spirit_actions;
-- ============================================================

-- ------------------------------------------------------------
-- 1. 행동장소 (spirit_action_places)
-- ------------------------------------------------------------
INSERT INTO spirit_action_places (code, place_name, representative_action, sort_order)
VALUES ('BOOKSHELF', '책장, 작은책상', '학습 및 기록', 1),
       ('WORKBENCH', '공작대', '업무 및 제작', 2),
       ('FRONT_YARD', '앞마당, 숲길 입구', '건강 및 탐색', 3),
       ('TEA_TABLE', '티테이블', '교류', 4),
       ('BED', '침상, 쉼터', '휴식', 5)
ON CONFLICT (code) DO NOTHING;

-- ------------------------------------------------------------
-- 2. 장소별 행동예시 (spirit_place_actions)
-- requires_object = TRUE 인 행동은 해당 장소의 오브젝트(가구)가 설치되어 있어야 노출됨
-- ------------------------------------------------------------

-- 책장, 작은책상
INSERT INTO spirit_place_actions (place_id, action_text, requires_object, sort_order)
SELECT p.id, v.action_text, v.requires_object, v.sort_order
FROM spirit_action_places p,
     (VALUES ('책 읽기', FALSE, 1),
             ('기록하기', FALSE, 2),
             ('자료 살펴보기', FALSE, 3),
             ('책 고르기', TRUE, 4),
             ('책읽기', TRUE, 5),
             ('책 정리하기', TRUE, 6),
             ('기록하기', TRUE, 7)
     ) AS v (action_text, requires_object, sort_order)
WHERE p.code = 'BOOKSHELF';

-- 공작대
INSERT INTO spirit_place_actions (place_id, action_text, requires_object, sort_order)
SELECT p.id, v.action_text, v.requires_object, v.sort_order
FROM spirit_action_places p,
     (VALUES ('만들기', FALSE, 1),
             ('고치기', FALSE, 2),
             ('정리하기', FALSE, 3)
     ) AS v (action_text, requires_object, sort_order)
WHERE p.code = 'WORKBENCH';

-- 앞마당, 숲길 입구
INSERT INTO spirit_place_actions (place_id, action_text, requires_object, sort_order)
SELECT p.id, v.action_text, v.requires_object, v.sort_order
FROM spirit_action_places p,
     (VALUES ('걷기', FALSE, 1),
             ('뛰기', FALSE, 2),
             ('주변 관찰', FALSE, 3)
     ) AS v (action_text, requires_object, sort_order)
WHERE p.code = 'FRONT_YARD';

-- 티테이블
INSERT INTO spirit_place_actions (place_id, action_text, requires_object, sort_order)
SELECT p.id, v.action_text, v.requires_object, v.sort_order
FROM spirit_action_places p,
     (VALUES ('친구와 차 마시기', FALSE, 1),
             ('대화하기', FALSE, 2),
             ('혼자 차 마시기', TRUE, 3),
             ('친구 초대하기', TRUE, 4),
             ('간식 먹기', TRUE, 5)
     ) AS v (action_text, requires_object, sort_order)
WHERE p.code = 'TEA_TABLE';

-- 침상, 쉼터
INSERT INTO spirit_place_actions (place_id, action_text, requires_object, sort_order)
SELECT p.id, v.action_text, v.requires_object, v.sort_order
FROM spirit_action_places p,
     (VALUES ('낮잠', FALSE, 1),
             ('음악 듣기', FALSE, 2),
             ('멍하니 있기', FALSE, 3)
     ) AS v (action_text, requires_object, sort_order)
WHERE p.code = 'BED';

-- ------------------------------------------------------------
-- 3. 카테고리별 정령 표현 (category_spirit_actions)
-- category 값은 CategoryType ENUM 재사용
-- ------------------------------------------------------------
INSERT INTO category_spirit_actions (category, action_text, sort_order)
VALUES ('WORK_STUDY', '책 읽기', 1),
       ('WORK_STUDY', '기록 정리', 2),
       ('WORK_STUDY', '공방 일', 3),
       ('WORK_STUDY', '연구', 4),
       ('WORK_STUDY', '연습', 5),

       ('HEALTH', '산책', 1),
       ('HEALTH', '달리기', 2),
       ('HEALTH', '스트레칭', 3),
       ('HEALTH', '몸 돌보기', 4),

       ('LIFE', '청소', 1),
       ('LIFE', '요리', 2),
       ('LIFE', '정리', 3),
       ('LIFE', '물건 수리', 4),
       ('LIFE', '화분 돌보기', 5),

       ('RELATIONSHIP', '친구 만나기', 1),
       ('RELATIONSHIP', '차 마시기', 2),
       ('RELATIONSHIP', '편지 보내기', 3),
       ('RELATIONSHIP', '다른 정령 돕기', 4),

       ('GROWTH', '새로운 책', 1),
       ('GROWTH', '새로운 기술 연습', 2),
       ('GROWTH', '하루 기록', 3),
       ('GROWTH', '목표 정리', 4),

       ('HOBBY', '그림', 1),
       ('HOBBY', '음악', 2),
       ('HOBBY', '수집', 3),
       ('HOBBY', '놀이', 4),
       ('HOBBY', '가벼운 탐색', 5),

       ('REST', '낮잠', 1),
       ('REST', '명상', 2),
       ('REST', '구름 보기', 3),
       ('REST', '산책', 4),
       ('REST', '음악 듣기', 5),

       ('FINANCE', '창고 정리', 1),
       ('FINANCE', '생활 장부', 2),
       ('FINANCE', '필요한 물건 계획하기', 3),
       ('FINANCE', '시장 소식 읽기', 4),
       ('FINANCE', '상업 공부', 5),
       ('FINANCE', '마을 거래 상황 살펴보기', 6);

-- ------------------------------------------------------------
-- 4. 정령의 집/숲 오브젝트 (spirit_home_objects)
-- 앞마당(FRONT_YARD)은 "설치 여부"가 언급되지 않아 오브젝트를 만들지 않음
-- ------------------------------------------------------------
INSERT INTO spirit_home_objects (code, object_name, place_id)
SELECT v.code, v.object_name, p.id
FROM (VALUES ('BOOKSHELF_MAIN', '책장', 'BOOKSHELF'),
             ('SMALL_DESK', '작은 책상', 'BOOKSHELF'),
             ('WORKBENCH_MAIN', '공작대', 'WORKBENCH'),
             ('TEA_TABLE_MAIN', '티테이블', 'TEA_TABLE'),
             ('BED_MAIN', '침상', 'BED')
     ) AS v (code, object_name, place_code)
         JOIN spirit_action_places p ON p.code = v.place_code
ON CONFLICT (code) DO NOTHING;

-- ------------------------------------------------------------
-- 5. 알 (spirit_egg_options)
-- 이름/설명/이미지는 기획 확정 전 임시(placeholder) 콘텐츠.
-- 콘텐츠 확정 후 UPDATE로 교체할 것. is_tutorial=TRUE 1개는 최초 가입 시 자동 배정됨
-- ------------------------------------------------------------
INSERT INTO spirit_egg_options (code, egg_name, description, is_tutorial, is_active, sort_order)
VALUES ('EGG_TUTORIAL', '맨 처음 만난 알', '튜토리얼에서 자동으로 만나게 되는 알', TRUE, TRUE, 0),
       ('EGG_FOREST', '숲빛 알', '(placeholder) 숲의 기운이 느껴지는 알', FALSE, TRUE, 1),
       ('EGG_OCEAN', '물결 알', '(placeholder) 잔잔한 파도 소리가 들리는 알', FALSE, TRUE, 2),
       ('EGG_STAR', '별가루 알', '(placeholder) 별빛 가루가 뿌려진 알', FALSE, TRUE, 3),
       ('EGG_FLAME', '불씨 알', '(placeholder) 따뜻한 온기가 도는 알', FALSE, TRUE, 4),
       ('EGG_MOON', '달그림자 알', '(placeholder) 은은한 달빛을 품은 알', FALSE, TRUE, 5)
ON CONFLICT (code) DO NOTHING;

-- ------------------------------------------------------------
-- 6. 정령 외형 (spirit_variants)
-- 이름/이미지는 기획 확정 전 임시(placeholder) 콘텐츠.
-- DEFAULT 1개는 항상 해금 상태(unlock_metric=DEFAULT)이며, "정령 만나기" 풀이 비지 않도록 반드시 있어야 함.
-- 나머지는 버릇/추억/친밀도 데이터 모델 구현 전까지는 자동 해금되지 않음(수동 시드 또는 추후 로직 필요)
-- ------------------------------------------------------------
INSERT INTO spirit_variants (code, variant_name, unlock_metric, unlock_threshold, weight, sort_order, asset_key)
VALUES ('DEFAULT', '기본', 'DEFAULT', 0, 1, 0, 'spirit_variant_default'),
       ('STARLIGHT', '별빛', 'HABIT_TYPE_COUNT', 3, 1, 1, 'spirit_variant_starlight'),
       ('EMBER', '불씨', 'HABIT_TYPE_COUNT', 4, 1, 2, 'spirit_variant_ember'),
       ('MOONSHADOW', '달그림자', 'MEMORY_COUNT', 5, 1, 3, 'spirit_variant_moonshadow'),
       ('MIST', '물결', 'INTIMACY_STAGE', 2, 1, 4, 'spirit_variant_mist'),
       ('FOREST', '숲빛', 'INTIMACY_STAGE', 3, 1, 5, 'spirit_variant_forest')
ON CONFLICT (code) DO NOTHING;

-- ------------------------------------------------------------
-- 7. 장식 (decorations)
-- 이름/가격/이미지는 기획 확정 전 임시(placeholder) 콘텐츠.
-- 기존 spirit_home_objects(무료)를 대체하는 유료 버전 — 같은 가구를 유료로 재정의함
-- ------------------------------------------------------------
INSERT INTO decorations (code, decoration_name, price, asset_key)
VALUES ('BOOKSHELF_MAIN', '책장', 200, 'decoration_bookshelf_main'),
       ('SMALL_DESK', '작은 책상', 150, 'decoration_small_desk'),
       ('WORKBENCH_MAIN', '공작대', 250, 'decoration_workbench_main'),
       ('TEA_TABLE_MAIN', '티테이블', 150, 'decoration_tea_table_main'),
       ('BED_MAIN', '침상', 200, 'decoration_bed_main')
ON CONFLICT (code) DO NOTHING;
