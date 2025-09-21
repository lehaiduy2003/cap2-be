CREATE TABLE roommates
(
    id       BIGINT AUTO_INCREMENT NOT NULL,
    gender   VARCHAR(255)          NULL,
    yob      INT                   NULL,
    hometown VARCHAR(255)          NULL,
    job      VARCHAR(255)          NULL,
    hobbies  VARCHAR(255)          NULL,
    more     TEXT                  NULL,
    user_id  BIGINT                NOT NULL,
    CONSTRAINT pk_roommates PRIMARY KEY (id)
);

ALTER TABLE roommates
    ADD CONSTRAINT FK_ROOMMATES_ON_USER FOREIGN KEY (user_id) REFERENCES users (id);