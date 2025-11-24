DROP TABLE IF EXISTS grades CASCADE;
DROP TABLE IF EXISTS students CASCADE;
DROP TABLE IF EXISTS courses CASCADE;
DROP TABLE IF EXISTS groups CASCADE;
DROP TABLE IF EXISTS teachers CASCADE;

CREATE TABLE teachers (
                          id          BIGSERIAL       PRIMARY KEY,
                          first_name  VARCHAR(50)     NOT NULL,
                          last_name   VARCHAR(50)     NOT NULL,
                          department  VARCHAR(100),
                          email       VARCHAR(100)    UNIQUE
);

CREATE TABLE groups (
                        id          BIGSERIAL       PRIMARY KEY,
                        name        VARCHAR(50)     NOT NULL UNIQUE,
                        year        SMALLINT        NOT NULL CHECK (year >= 1 AND year <= 6)
    );


CREATE TABLE students (
                          id              BIGSERIAL       PRIMARY KEY,
                          first_name      VARCHAR(50)     NOT NULL,
                          last_name       VARCHAR(50)     NOT NULL,
                          email           VARCHAR(100)    UNIQUE,
                          group_id        BIGINT          REFERENCES groups(id)
                                                              ON UPDATE CASCADE
                                                              ON DELETE SET NULL,
                          enrollment_year SMALLINT        CHECK (enrollment_year >= 2000),
                          created_at      TIMESTAMP       NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_students_group_id ON students(group_id);


CREATE TABLE courses (
                         id          BIGSERIAL       PRIMARY KEY,
                         name        VARCHAR(100)    NOT NULL UNIQUE,
                         semester    SMALLINT        NOT NULL CHECK (semester BETWEEN 1 AND 2),
                         year        SMALLINT        CHECK (year >= 1 AND year <= 6),
    teacher_id  BIGINT          REFERENCES teachers(id)
                                  ON UPDATE CASCADE
                                  ON DELETE SET NULL,
    credits     SMALLINT        CHECK (credits >= 0)
);

CREATE INDEX idx_courses_teacher_id ON courses(teacher_id);


CREATE TABLE grades (
                        id          BIGSERIAL       PRIMARY KEY,
                        student_id  BIGINT          NOT NULL REFERENCES students(id)
                            ON UPDATE CASCADE
                            ON DELETE CASCADE,
                        course_id   BIGINT          NOT NULL REFERENCES courses(id)
                            ON UPDATE CASCADE
                            ON DELETE CASCADE,
                        teacher_id  BIGINT          REFERENCES teachers(id)
                                                        ON UPDATE CASCADE
                                                        ON DELETE SET NULL,
                        value       NUMERIC(5,2)    NOT NULL CHECK (value >= 0 AND value <= 100),
                        grade_date  DATE            NOT NULL DEFAULT CURRENT_DATE
);

CREATE INDEX idx_grades_student_id ON grades(student_id);
CREATE INDEX idx_grades_course_id  ON grades(course_id);
CREATE INDEX idx_grades_teacher_id ON grades(teacher_id);

ALTER TABLE grades
    ADD CONSTRAINT uq_grade_student_course_date
        UNIQUE (student_id, course_id, grade_date);
