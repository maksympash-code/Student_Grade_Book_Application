package ua.knu.pashchenko_maksym.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import ua.knu.pashchenko_maksym.dao.CourseDao;
import ua.knu.pashchenko_maksym.dao.GradeDao;
import ua.knu.pashchenko_maksym.dao.GroupDao;
import ua.knu.pashchenko_maksym.dao.StudentDao;
import ua.knu.pashchenko_maksym.dao.TeacherDao;
import ua.knu.pashchenko_maksym.model.Course;
import ua.knu.pashchenko_maksym.model.Grade;
import ua.knu.pashchenko_maksym.model.Group;
import ua.knu.pashchenko_maksym.model.Student;
import ua.knu.pashchenko_maksym.model.Teacher;

public class GradeBookService {

    private final StudentDao studentDao;
    private final GroupDao groupDao;
    private final CourseDao courseDao;
    private final TeacherDao teacherDao;
    private final GradeDao gradeDao;

    public GradeBookService(StudentDao studentDao,
                            GroupDao groupDao,
                            CourseDao courseDao,
                            TeacherDao teacherDao,
                            GradeDao gradeDao) {
        this.studentDao = studentDao;
        this.groupDao = groupDao;
        this.courseDao = courseDao;
        this.teacherDao = teacherDao;
        this.gradeDao = gradeDao;
    }


    public Student createStudent(String firstName,
                                 String lastName,
                                 String email,
                                 Long groupId,
                                 int enrollmentYear) {
        Student student = new Student();
        student.setFirstName(firstName);
        student.setLastName(lastName);
        student.setEmail(email);
        student.setGroupId(groupId);
        student.setEnrollmentYear((short) enrollmentYear);
        return studentDao.insert(student);
    }

    public boolean updateStudent(Student student) {
        return studentDao.update(student);
    }

    public boolean deleteStudent(Long id) {
        return studentDao.delete(id);
    }

    public Student getStudentById(Long id) {
        return studentDao.findById(id);
    }

    public List<Student> getAllStudents() {
        return studentDao.findAll();
    }

    public List<Student> getStudentsByGroup(Long groupId) {
        return studentDao.findByGroupId(groupId);
    }

    public List<Student> getStudentsByCourse(Long courseId) {
        return studentDao.findByCourseId(courseId);
    }


    public Group createGroup(String name, short year) {
        Group existing = groupDao.findByName(name);
        if (existing != null) {
            return existing;
        }

        Group group = new Group();
        group.setName(name);
        group.setYear(year);

        return groupDao.insert(group);
    }

    public boolean updateGroup(Group group) {
        return groupDao.update(group);
    }

    public boolean deleteGroup(Long id) {
        return groupDao.delete(id);
    }

    public Group getGroupById(Long id) {
        return groupDao.findById(id);
    }

    public Group getGroupByName(String name) {
        return groupDao.findByName(name);
    }

    public List<Group> getAllGroups() {
        return groupDao.findAll();
    }

    // =========================
    // COURSES
    // =========================

    public Course createCourse(String name,
                               Integer semester,
                               Integer year,
                               Long teacherId,
                               Integer credits) {
        Course course = new Course();
        course.setName(name);
        if (semester != null) {
            course.setSemester(semester.shortValue());
        }
        if (year != null) {
            course.setYear(year.shortValue());
        }
        course.setTeacherId(teacherId);
        if (credits != null) {
            course.setCredits(credits.shortValue());
        }
        return courseDao.insert(course);
    }

    public boolean updateCourse(Course course) {
        return courseDao.update(course);
    }

    public boolean deleteCourse(Long id) {
        return courseDao.delete(id);
    }

    public Course getCourseById(Long id) {
        return courseDao.findById(id);
    }

    public Course getCourseByName(String name) {
        return courseDao.findByName(name);
    }

    public List<Course> getAllCourses() {
        return courseDao.findAll();
    }

    public List<Course> getCoursesByTeacher(Long teacherId) {
        return courseDao.findByTeacherId(teacherId);
    }


    public Teacher createTeacher(String firstName,
                                 String lastName,
                                 String department,
                                 String email) {
        Teacher teacher = new Teacher();
        teacher.setFirstName(firstName);
        teacher.setLastName(lastName);
        teacher.setDepartment(department);
        teacher.setEmail(email);
        return teacherDao.insert(teacher);
    }

    public boolean updateTeacher(Teacher teacher) {
        return teacherDao.update(teacher);
    }

    public boolean deleteTeacher(Long id) {
        return teacherDao.delete(id);
    }

    public Teacher getTeacherById(Long id) {
        return teacherDao.findById(id);
    }

    public List<Teacher> getAllTeachers() {
        return teacherDao.findAll();
    }

    public List<Teacher> findTeachersByLastName(String lastName) {
        return teacherDao.findByLastName(lastName);
    }


    public Grade addGrade(Long studentId,
                          Long courseId,
                          Long teacherId,
                          double value,
                          LocalDate date) {

        if (studentDao.findById(studentId) == null) {
            throw new IllegalArgumentException("Student with id " + studentId + " not found");
        }
        if (courseDao.findById(courseId) == null) {
            throw new IllegalArgumentException("Course with id " + courseId + " not found");
        }
        if (teacherId != null && teacherDao.findById(teacherId) == null) {
            throw new IllegalArgumentException("Teacher with id " + teacherId + " not found");
        }

        Grade grade = new Grade();
        grade.setStudentId(studentId);
        grade.setCourseId(courseId);
        grade.setTeacherId(teacherId);
        grade.setValue(BigDecimal.valueOf(value));
        grade.setGradeDate(date != null ? date : LocalDate.now());

        return gradeDao.insert(grade);
    }

    public boolean updateGrade(Grade grade) {
        return gradeDao.update(grade);
    }

    public boolean deleteGrade(Long id) {
        return gradeDao.delete(id);
    }

    public Grade getGradeById(Long id) {
        return gradeDao.findById(id);
    }

    public List<Grade> getGradesForStudent(Long studentId) {
        return gradeDao.findByStudentId(studentId);
    }

    public List<Grade> getGradesForCourse(Long courseId) {
        return gradeDao.findByCourseId(courseId);
    }

    public List<Grade> getGradesForTeacher(Long teacherId) {
        return gradeDao.findByTeacherId(teacherId);
    }

    public List<Grade> getGradesForStudentAndCourse(Long studentId, Long courseId) {
        return gradeDao.findByStudentAndCourse(studentId, courseId);
    }

    // =========================
    // AVERAGES
    // =========================

    /**
     * Середній бал студента по всім його оцінкам.
     *
     * @return 0.0, якщо оцінок немає
     */
    public double getStudentAverageGrade(Long studentId) {
        List<Grade> grades = gradeDao.findByStudentId(studentId);
        return averageFromGrades(grades);
    }

    /**
     * Середній бал групи по конкретному курсу.
     *
     * <p>Бере всі оцінки з таблиці grades для всіх студентів цієї групи по цьому курсу.
     */
    public double getGroupAverageForCourse(Long groupId, Long courseId) {
        List<Student> students = studentDao.findByGroupId(groupId);
        List<Grade> allGrades = new ArrayList<>();

        for (Student s : students) {
            allGrades.addAll(gradeDao.findByStudentAndCourse(s.getId(), courseId));
        }

        return averageFromGrades(allGrades);
    }

    /**
     * Середній бал по викладачу: усі оцінки, де виставляв цей викладач.
     */
    public double getTeacherAverageGrade(Long teacherId) {
        List<Grade> grades = gradeDao.findByTeacherId(teacherId);
        return averageFromGrades(grades);
    }

    private double averageFromGrades(List<Grade> grades) {
        if (grades == null || grades.isEmpty()) {
            return 0.0;
        }
        double sum = 0.0;
        int count = 0;
        for (Grade grade : grades) {
            if (grade.getValue() != null) {
                sum += grade.getValue().doubleValue();
                count++;
            }
        }
        if (count == 0) {
            return 0.0;
        }
        return sum / count;
    }
}
