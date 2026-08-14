create table if not exists speciality
(
    id   uuid         not null
    constraint speciality_pk primary key,
    name varchar(255) not null
    constraint speciality_name_unique unique
    constraint speciality_name_check check (name in ('EL', 'TN', 'COMMON_PART'))
    );