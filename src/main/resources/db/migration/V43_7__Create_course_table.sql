create table if not exists course
(
    id        uuid         not null
    constraint course_pk primary key,
    reference varchar(255) not null
    constraint course_reference_unique unique,
    title     varchar(255) not null,
    credit    integer      not null
    );