create table if not exists group_exam
(
    id       uuid not null
    constraint group_exam_pk primary key,
    group_id uuid not null
    constraint group_exam_group_fk references "group" (id),
    exam_id  uuid not null
    constraint group_exam_exam_fk references exam (id)
    );