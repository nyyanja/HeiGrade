create table if not exists admin
(
    id uuid not null
    constraint admin_pk primary key
    constraint admin_user_fk references "user" (id)
    );