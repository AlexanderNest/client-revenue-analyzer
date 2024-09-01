CREATE TABLE "user"
(
    id                 int auto_increment PRIMARY KEY,
    username           VARCHAR(30) UNIQUE NOT NULL,
    main_calendar      VARCHAR(100)       NOT NULL,
    cancelled_calendar VARCHAR(100)
);

CREATE TABLE Client
(
    id             int auto_increment PRIMARY KEY,
    name           varchar(255),
    description    varchar(255),
    price_per_hour int,
    active         boolean,
    user_id        bigint REFERENCES "user" (id) NOT NULL
);