create table if not exists student
(
    id        uuid         not null
    constraint student_pk primary key
    constraint student_user_fk references "user" (id),
    reference varchar(255) not null
    constraint student_reference_unique unique
    );