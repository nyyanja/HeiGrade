ALTER TABLE "user"
    ALTER COLUMN role SET NOT NULL,
    ADD CONSTRAINT user_role_values
        CHECK (role IN ('TEACHER', 'STUDENT', 'ADMIN'));

ALTER TABLE "user"
    ADD CONSTRAINT user_sex_values
        CHECK (sex IS NULL OR sex IN ('MALE', 'FEMALE', 'OTHER'));