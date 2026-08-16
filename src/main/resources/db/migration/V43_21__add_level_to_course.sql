ALTER TABLE course
    ADD COLUMN level INTEGER NOT NULL;

ALTER TABLE course
    ADD CONSTRAINT course_level_check
        CHECK (level IN (1, 2, 3));