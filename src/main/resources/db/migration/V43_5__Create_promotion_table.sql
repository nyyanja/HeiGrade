create table if not exists promotion
(
    id   uuid         not null
    constraint promotion_pk primary key,
    name varchar(255) not null,
    year integer      not null
    );