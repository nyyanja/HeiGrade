create table if not exists "user"
(
    id         uuid         not null
    constraint user_pk primary key,
    first_name varchar(255) not null,
    last_name  varchar(255) ,
    birthday   date,
    sex        varchar(20)
    constraint user_sex_check check (sex in ('MALE', 'FEMALE')),
    address    varchar(255),
    email      varchar(255) not null
    constraint user_email_unique unique,
    role       varchar(20)  not null
    constraint user_role_check check (role in ('TEACHER', 'STUDENT', 'ADMIN'))
    );