create table if not exists teacher_course
(
    id         uuid not null
    constraint teacher_course_pk primary key,
    teacher_id uuid not null
    constraint teacher_course_teacher_fk references teacher (id),
    course_id  uuid not null
    constraint teacher_course_course_fk references course (id)
    );