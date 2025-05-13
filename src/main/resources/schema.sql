-- src/main/resources/schema.sql 또는 src/test/resources/schema.sql

-- 기존 테이블이 있다면 삭제 (초기화 목적)
-- 개발/테스트 환경에서 사용 권장. 운영 환경에서는 데이터 손실 방지 로직 필요.
DROP TABLE IF EXISTS users;
DROP TABLE IF EXISTS todos;

-- Users 테이블 생성 (UsersEntity 기반)
CREATE TABLE users (
    seq INTEGER PRIMARY KEY AUTOINCREMENT,
    user_id VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL
);

-- Todos 테이블 생성 (이전에 공유해주신 TodosEntity 기반)
CREATE TABLE todos (
    seq INTEGER PRIMARY KEY AUTOINCREMENT,
    content VARCHAR(255) NOT NULL
);
