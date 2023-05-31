DROP TABLE IF EXISTS users CASCADE;
DROP TABLE IF EXISTS items CASCADE;
DROP TABLE IF EXISTS bookings CASCADE;
DROP TABLE IF EXISTS requests CASCADE;
DROP TABLE IF EXISTS comments CASCADE;


CREATE TABLE IF NOT EXISTS users (
  id BIGINT GENERATED BY DEFAULT AS IDENTITY NOT NULL,
  name VARCHAR(255) NOT NULL,
  email VARCHAR(512) NOT NULL,
  CONSTRAINT pk_user PRIMARY KEY (id),
  CONSTRAINT uq_user_email UNIQUE (email)
);

CREATE TABLE IF NOT EXISTS requests (
	id BIGINT GENERATED BY DEFAULT AS IDENTITY NOT NULL,
	description VARCHAR(255) NOT NULL,
	requestor_id BIGINT REFERENCES USERS(id),
	CONSTRAINT pk_requests PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS items (
	id BIGINT GENERATED BY DEFAULT AS IDENTITY NOT NULL,
	name VARCHAR(255) NOT NULL,
	description VARCHAR(1000) NOT NULL,
	available BOOLEAN NOT NULL,
	owner_id BIGINT NOT NULL REFERENCES USERS(ID) ON DELETE CASCADE,
	request_id BIGINT REFERENCES requests(id) ON DELETE CASCADE,
	CONSTRAINT pk_item PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS bookings (
	id BIGINT GENERATED BY DEFAULT AS IDENTITY NOT NULL,
	start_date TIMESTAMP WITHOUT TIME ZONE NOT NULL,
	end_date TIMESTAMP WITHOUT TIME ZONE NOT NULL,
	item_id BIGINT REFERENCES ITEMS(id),
	booker_id BIGINT REFERENCES USERS(id),
	status VARCHAR(30),
	CONSTRAINT pk_booking PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS comments (
	id BIGINT GENERATED BY DEFAULT AS IDENTITY NOT NULL,
	TEXT VARCHAR(255),
	item_id BIGINT REFERENCES ITEMS(id),
	author_id BIGINT REFERENCES USERS(id),
	created_date TIMESTAMP WITH TIME ZONE NOT NULL,
	CONSTRAINT pk_comments PRIMARY KEY (id)
);