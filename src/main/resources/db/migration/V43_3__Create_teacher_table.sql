create table if not exists teacher
(
    id         uuid not null
    constraint teacher_pk primary key
    constraint teacher_user_fk references "user" (id),
    speciality varchar(255)
    );