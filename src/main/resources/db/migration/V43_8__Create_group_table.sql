create table if not exists "group"
(
    id            uuid         not null
    constraint group_pk primary key,
    name          varchar(255) not null,
    promotion_id  uuid         not null
    constraint group_promotion_fk references promotion (id),
    speciality_id uuid
    constraint group_speciality_fk references speciality (id)
    );