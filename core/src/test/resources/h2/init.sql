CREATE TABLE client
(
    id    int auto_increment PRIMARY KEY,
    name  varchar(30) NOT NULL,
    pricePerHour int NOT NULL,
    description varchar(255)
);