alter table exam
    add column if not exists course_id uuid not null
    constraint exam_course_fk references course (id);