-- Спочатку дропнемо таблиці в правильному порядку залежностей
DROP TABLE IF EXISTS grades   CASCADE;
DROP TABLE IF EXISTS courses  CASCADE;
DROP TABLE IF EXISTS students CASCADE;
DROP TABLE IF EXISTS teachers CASCADE;
DROP TABLE IF EXISTS groups   CASCADE;

-- Таблиця груп
CREATE TABLE IF NOT EXISTS groups (
                                      id      BIGSERIAL PRIMARY KEY,
                                      name    VARCHAR(100) NOT NULL UNIQUE,
                                      year    SMALLINT     NOT NULL CHECK (year BETWEEN 1 AND 6)
);

-- Таблиця студентів
CREATE TABLE IF NOT EXISTS students (
                                        id              BIGSERIAL PRIMARY KEY,
                                        first_name      VARCHAR(100) NOT NULL,
                                        last_name       VARCHAR(100) NOT NULL,
                                        group_id        BIGINT       REFERENCES groups(id) ON DELETE SET NULL,
                                        email           VARCHAR(150),
                                        enrollment_year SMALLINT,
                                        created_at      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_students_group_id
    ON students(group_id);


-- Таблиця викладачів
CREATE TABLE IF NOT EXISTS teachers (
                                        id          BIGSERIAL PRIMARY KEY,
                                        first_name  VARCHAR(100) NOT NULL,
                                        last_name   VARCHAR(100) NOT NULL,
                                        department  VARCHAR(150),
                                        email       VARCHAR(150)
);

-- Таблиця курсів
CREATE TABLE IF NOT EXISTS courses (
                                       id          BIGSERIAL PRIMARY KEY,
                                       name        VARCHAR(150) NOT NULL,
                                       semester    SMALLINT     NOT NULL CHECK (semester BETWEEN 1 AND 2),
                                       year        SMALLINT     NOT NULL,  -- РІК КУРСУ (відповідає моделі/DAO)
                                       teacher_id  BIGINT       REFERENCES teachers(id) ON DELETE SET NULL
);

CREATE INDEX IF NOT EXISTS idx_courses_teacher_id
    ON courses(teacher_id);

-- Таблиця оцінок
CREATE TABLE IF NOT EXISTS grades (
                                      id          BIGSERIAL PRIMARY KEY,
                                      student_id  BIGINT  NOT NULL REFERENCES students(id) ON DELETE CASCADE,
                                      course_id   BIGINT  NOT NULL REFERENCES courses(id)  ON DELETE CASCADE,
                                      value       NUMERIC(5,2) NOT NULL CHECK (value >= 0 AND value <= 100),
                                      grade_date  DATE        NOT NULL DEFAULT CURRENT_DATE,
                                      teacher_id  BIGINT      REFERENCES teachers(id) ON DELETE SET NULL
);

CREATE INDEX IF NOT EXISTS idx_grades_student
    ON grades(student_id);

CREATE INDEX IF NOT EXISTS idx_grades_course
    ON grades(course_id);

CREATE INDEX IF NOT EXISTS idx_grades_teacher
    ON grades(teacher_id);
