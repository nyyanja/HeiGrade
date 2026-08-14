create table if not exists speciality_course
(
    id            uuid not null
    constraint speciality_course_pk primary key,
    speciality_id uuid not null
    constraint speciality_course_speciality_fk references speciality (id),
    course_id     uuid not null
    constraint speciality_course_course_fk references course (id)
    );