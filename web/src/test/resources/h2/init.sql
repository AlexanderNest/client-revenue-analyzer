CREATE TABLE Client (
    id int auto_increment PRIMARY KEY,
    name varchar(255),
    description varchar(255),
    price_per_hour int,
    active boolean
);