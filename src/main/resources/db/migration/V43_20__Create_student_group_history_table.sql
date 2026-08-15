create table if not exists student_group_history
(
    id         uuid not null
    constraint student_group_history_pk primary key,

    student_id uuid not null
    constraint student_group_history_student_fk references student (id),

    group_id   uuid not null
    constraint student_group_history_group_fk references "group" (id),

    start_date date not null,
    end_date   date
    );