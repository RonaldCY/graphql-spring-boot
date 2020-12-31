-- // First migration.
-- Migration SQL that makes the change goes here.
CREATE TABLE user (
  id VARCHAR(36) NOT NULL,
  name VARCHAR(255) NOT NULL,
  nick_name VARCHAR(255) NULL,
  PRIMARY KEY(id)
);

-- //@UNDO
-- SQL to undo the change goes here.
DROP TABLE user;