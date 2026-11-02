-- ============================================================
--  tasks 의 memo / category / is_public 컬럼 삭제 마이그레이션
--  일정·루틴 모두 메모, 카테고리, 공개상태를 더 이상 사용하지 않습니다.
--  task_postponements.category 는 tasks.category 스냅샷이었으므로 함께 삭제합니다.
--  ddl.sql 최신본으로 새로 만든 DB라면 실행할 필요 없습니다.
--  주의: 기존 데이터의 memo / category / is_public 값은 복구할 수 없습니다.
-- ============================================================

DROP INDEX IF EXISTS idx_tasks_user_category;

ALTER TABLE tasks
    DROP COLUMN IF EXISTS memo,
    DROP COLUMN IF EXISTS category,
    DROP COLUMN IF EXISTS is_public;

ALTER TABLE task_postponements
    DROP COLUMN IF EXISTS category;
