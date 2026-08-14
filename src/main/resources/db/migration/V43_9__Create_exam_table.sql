create table if not exists exam
(
    id    uuid             not null
    constraint exam_pk primary key,
    date  date             not null,
    coeff double precision not null,
    title varchar(255)     not null
    );
