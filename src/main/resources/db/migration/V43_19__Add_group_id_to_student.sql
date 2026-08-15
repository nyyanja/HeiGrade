alter table student
    add column if not exists group_id uuid
    constraint student_group_fk references "group" (id);