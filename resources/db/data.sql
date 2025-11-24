INSERT INTO groups (name, year)
VALUES
    ('IP-11', 1),
    ('IP-21', 2);


INSERT INTO teachers (first_name, last_name, department, email)
VALUES
    ('Ivan',   'Ivanenko',  'Computer Science', 'ivan.ivanenko@example.com'),
    ('Olena',  'Petrenko',  'Mathematics',      'olena.petrenko@example.com');


INSERT INTO courses (name, semester, year, teacher_id, credits)
VALUES
    ('Programming 1', 1, 1, 1, 4),
    ('Linear Algebra', 1, 1, 2, 3),
    ('Discrete Math', 2, 1, 2, 4);


INSERT INTO students (first_name, last_name, email, group_id, enrollment_year)
VALUES
    ('Maksym', 'Shevchenko', 'maks.shevchenko@example.com', 1, 2024),
    ('Iryna',  'Koval',      'iryna.koval@example.com',     1, 2024),
    ('Andrii', 'Melnyk',     'andrii.melnyk@example.com',   2, 2023);


INSERT INTO grades (student_id, course_id, teacher_id, value, grade_date)
VALUES
    (1, 1, 1, 95.0, '2024-09-20'),
    (1, 2, 2, 88.0, '2024-10-05'),
    (2, 1, 1, 78.5, '2024-09-22'),
    (2, 3, 2, 91.0, '2024-10-15'),
    (3, 1, 1, 82.0, '2024-09-25');
