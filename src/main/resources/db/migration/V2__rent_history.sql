CREATE TABLE IF NOT EXISTS rent_histories (
  id bigint NOT NULL AUTO_INCREMENT,
  user_id bigint NOT NULL,
  rent_request_id bigint NOT NULL,
  rent_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  return_date TIMESTAMP NULL DEFAULT NULL,
  PRIMARY KEY (id),
  CONSTRAINT fk_rent_histories_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
  CONSTRAINT fk_rent_histories_rent_request FOREIGN KEY (rent_request_id) REFERENCES rent_requests (id) ON DELETE CASCADE
);
