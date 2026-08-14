create table if not exists grade
(
    id         uuid             not null
    constraint grade_pk primary key,
    value      double precision not null,
    date       date             not null,
    student_id uuid             not null
    constraint grade_student_fk references student (id),
    exam_id    uuid             not null
    constraint grade_exam_fk references exam (id)
    );