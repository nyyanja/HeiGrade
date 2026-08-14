create table if not exists grade_history
(
    id          uuid             not null
    constraint grade_history_pk primary key,
    date        timestamp        not null,
    old_value   double precision,
    new_value   double precision not null,
    reason      varchar(255),
    grade_id    uuid             not null
    constraint grade_history_grade_fk references grade (id),
    modified_by uuid             not null
    constraint grade_history_user_fk references "user" (id)
    );