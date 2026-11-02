-- ============================================================
--  기록(일간/주간/월간) 조회 API 삭제에 따른 미사용 테이블 삭제
--  daily_records / monthly_records 는 코드에서 사용하지 않습니다.
--  ddl.sql 최신본으로 새로 만든 DB라면 실행할 필요 없습니다.
--  주의: 두 테이블의 데이터는 복구할 수 없습니다.
-- ============================================================

DROP TABLE IF EXISTS daily_records;
DROP TABLE IF EXISTS monthly_records;
