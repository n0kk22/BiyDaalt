import org.junit.jupiter.api.*;
import org.junit.jupiter.api.io.TempDir;
import static org.junit.jupiter.api.Assertions.*;

import java.io.*;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Scanner;

class RegistrationTest {

    private Registration registration;

    @TempDir
    Path tempDir;

    private Path subjectsFile;
    private Path majorsFile;
    private Path examsFile;

    private Object getPrivateField(Object obj, String fieldName) throws Exception {
        Field field = obj.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        return field.get(obj);
    }

    private void setPrivateField(Object obj, String fieldName, Object value) throws Exception {
        Field field = obj.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(obj, value);
    }

    private Object callPrivateMethod(Object obj, String methodName, Class<?>[] paramTypes, Object[] args) throws Exception {
        Method method = obj.getClass().getDeclaredMethod(methodName, paramTypes);
        method.setAccessible(true);
        return method.invoke(obj, args);
    }

    @BeforeEach
    void setUp() throws Exception {
        registration = new Registration();

        subjectsFile = tempDir.resolve("Subjects.txt");
        majorsFile = tempDir.resolve("Professions.txt");
        examsFile = tempDir.resolve("Exams.txt");

        setPrivateField(registration, "subjectsFile", subjectsFile.toString());
        setPrivateField(registration, "majorsFile", majorsFile.toString());
        setPrivateField(registration, "examsFile", examsFile.toString());

        createTestFiles();
    }

    private void createTestFiles() throws IOException {

        String subjectsContent =
                "CS101/Introduction to Computer Science/3.0\n" +
                        "MATH101/Calculus I/4.0\n" +
                        "PHY101/Physics I/3.5\n" +
                        "ENG101/English Composition/3.0\n" +
                        "CHEM101/Chemistry I/3.0\n" +
                        "BIO101/Biology I/3.5\n" +
                        "HIST101/World History/3.0\n" +
                        "ART101/Art Appreciation/2.5\n";
        Files.writeString(subjectsFile, subjectsContent);

        String majorsContent =
                "CS/Computer Science\n" +
                        "MA/Mathematics\n" +
                        "PH/Physics\n" +
                        "EN/English\n" +
                        "CH/Chemistry\n" +
                        "BI/Biology\n" +
                        "HI/History\n" +
                        "AR/Art\n";
        Files.writeString(majorsFile, majorsContent);

        String examsContent =
                "CS001/CS101/85\n" +
                        "CS001/MATH101/92\n" +
                        "CS001/PHY101/78\n" +
                        "CS001/ENG101/88\n" +
                        "CS001/CHEM101/45\n" +  // F grade
                        "MA001/MATH101/95\n" +
                        "MA001/CS101/82\n" +
                        "MA001/ENG101/76\n" +
                        "MA001/PHY101/89\n" +
                        "CS002/CS101/96\n" +
                        "CS002/MATH101/98\n" +
                        "CS002/PHY101/94\n" +
                        "CS002/ENG101/92\n" +
                        "CS003/CS101/42\n" +   // F grade
                        "CS003/MATH101/44\n" + // F grade
                        "CS003/PHY101/48\n" +  // F grade
                        "CS003/ENG101/50\n" +  // F grade
                        "CS003/CHEM101/55\n" + // F grade
                        "CS003/BIO101/58\n" +  // F grade
                        "PH001/PHY101/91\n" +
                        "PH001/MATH101/89\n" +
                        "PH001/CS101/79\n" +
                        "EN001/ENG101/96\n" +
                        "EN001/CS101/81\n" +
                        "EN001/MATH101/72\n";
        Files.writeString(examsFile, examsContent);
    }

    @Test
    void testLoadSubjects() {
        registration.loadSubjects();
        assertEquals(8, registration.subjectList.size());

        Subject subject = (Subject) registration.subjectList.get(0);
        assertEquals("CS101", subject.code);
        assertEquals("Introduction to Computer Science", subject.name);
        assertEquals(3.0f, subject.credit);

        Subject lastSubject = (Subject) registration.subjectList.get(registration.subjectList.size() - 1);
        assertEquals("ART101", lastSubject.code);
    }

    @Test
    void testLoadMajors() {
        registration.loadMajors();
        assertEquals(8, registration.majorList.size());

        Major major = (Major) registration.majorList.get(0);
        assertEquals("CS", major.code);
        assertEquals("Computer Science", major.name);

        Major lastMajor = (Major) registration.majorList.get(registration.majorList.size() - 1);
        assertEquals("AR", lastMajor.code);
    }

    @Test
    void testLoadExams() {
        registration.loadSubjects();
        registration.loadExams();

        assertEquals(6, registration.studentList.size()); // CS001, MA001, CS002, CS003, PH001, EN001

        Student student = (Student) registration.studentList.get(0);
        assertEquals("CS001", student.code);
        assertTrue(student.lessons.size() >= 4);

        assertTrue(student.GPA > 0);
    }

    @Test
    void testLoadExamsWithNonexistentSubject() throws IOException {

        String invalidExams = "CS999/INVALID999/85\n";
        Files.writeString(examsFile, invalidExams);

        registration.loadSubjects();

        ByteArrayOutputStream outContent = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outContent));

        registration.loadExams();

        String output = outContent.toString();
        assertTrue(output.contains("Warning: subject not found") || output.contains("INVALID999"));

        System.setOut(System.out);
    }

    @Test
    void testFindSubject() throws Exception {
        registration.loadSubjects();

        Subject found = (Subject) callPrivateMethod(registration, "findSubject",
                new Class<?>[]{String.class}, new Object[]{"CS101"});
        assertNotNull(found);
        assertEquals("CS101", found.code);
        assertEquals("Introduction to Computer Science", found.name);
        assertEquals(3.0f, found.credit);

        Subject notFound = (Subject) callPrivateMethod(registration, "findSubject",
                new Class<?>[]{String.class}, new Object[]{"NONEXISTENT"});
        assertNull(notFound);

        Subject caseSensitive = (Subject) callPrivateMethod(registration, "findSubject",
                new Class<?>[]{String.class}, new Object[]{"cs101"}); // lowercase
        assertNull(caseSensitive);
    }

    @Test
    void testFindMajorByStudentCode() throws Exception {
        registration.loadMajors();

        Major csMajor = (Major) callPrivateMethod(registration, "findMajorByStudentCode",
                new Class<?>[]{String.class}, new Object[]{"CS001"});
        assertNotNull(csMajor);
        assertEquals("CS", csMajor.code);
        assertEquals("Computer Science", csMajor.name);

        Major mathMajor = (Major) callPrivateMethod(registration, "findMajorByStudentCode",
                new Class<?>[]{String.class}, new Object[]{"MA001"});
        assertNotNull(mathMajor);
        assertEquals("MA", mathMajor.code);

        Major invalid = (Major) callPrivateMethod(registration, "findMajorByStudentCode",
                new Class<?>[]{String.class}, new Object[]{"X"}); // Too short
        assertNull(invalid);

        Major unknown = (Major) callPrivateMethod(registration, "findMajorByStudentCode",
                new Class<?>[]{String.class}, new Object[]{"XX001"}); // Unknown major
        assertNull(unknown);
    }

    @Test
    void testScoreToGPA() throws Exception {

        assertEquals(4.0f, callPrivateMethod(registration, "scoreToGPA",
                new Class<?>[]{int.class}, new Object[]{96}));
        assertEquals(4.0f, callPrivateMethod(registration, "scoreToGPA",
                new Class<?>[]{int.class}, new Object[]{100}));
        assertEquals(3.7f, callPrivateMethod(registration, "scoreToGPA",
                new Class<?>[]{int.class}, new Object[]{91}));
        assertEquals(3.7f, callPrivateMethod(registration, "scoreToGPA",
                new Class<?>[]{int.class}, new Object[]{95}));
        assertEquals(3.4f, callPrivateMethod(registration, "scoreToGPA",
                new Class<?>[]{int.class}, new Object[]{88}));
        assertEquals(3.4f, callPrivateMethod(registration, "scoreToGPA",
                new Class<?>[]{int.class}, new Object[]{90}));
        assertEquals(3.0f, callPrivateMethod(registration, "scoreToGPA",
                new Class<?>[]{int.class}, new Object[]{84}));
        assertEquals(3.0f, callPrivateMethod(registration, "scoreToGPA",
                new Class<?>[]{int.class}, new Object[]{87}));
        assertEquals(2.7f, callPrivateMethod(registration, "scoreToGPA",
                new Class<?>[]{int.class}, new Object[]{81}));
        assertEquals(2.7f, callPrivateMethod(registration, "scoreToGPA",
                new Class<?>[]{int.class}, new Object[]{83}));
        assertEquals(2.4f, callPrivateMethod(registration, "scoreToGPA",
                new Class<?>[]{int.class}, new Object[]{78}));
        assertEquals(2.4f, callPrivateMethod(registration, "scoreToGPA",
                new Class<?>[]{int.class}, new Object[]{80}));
        assertEquals(2.0f, callPrivateMethod(registration, "scoreToGPA",
                new Class<?>[]{int.class}, new Object[]{74}));
        assertEquals(2.0f, callPrivateMethod(registration, "scoreToGPA",
                new Class<?>[]{int.class}, new Object[]{77}));
        assertEquals(1.7f, callPrivateMethod(registration, "scoreToGPA",
                new Class<?>[]{int.class}, new Object[]{71}));
        assertEquals(1.7f, callPrivateMethod(registration, "scoreToGPA",
                new Class<?>[]{int.class}, new Object[]{73}));
        assertEquals(1.3f, callPrivateMethod(registration, "scoreToGPA",
                new Class<?>[]{int.class}, new Object[]{68}));
        assertEquals(1.3f, callPrivateMethod(registration, "scoreToGPA",
                new Class<?>[]{int.class}, new Object[]{70}));
        assertEquals(1.0f, callPrivateMethod(registration, "scoreToGPA",
                new Class<?>[]{int.class}, new Object[]{64}));
        assertEquals(1.0f, callPrivateMethod(registration, "scoreToGPA",
                new Class<?>[]{int.class}, new Object[]{67}));
        assertEquals(0.7f, callPrivateMethod(registration, "scoreToGPA",
                new Class<?>[]{int.class}, new Object[]{61}));
        assertEquals(0.7f, callPrivateMethod(registration, "scoreToGPA",
                new Class<?>[]{int.class}, new Object[]{63}));
        assertEquals(0.0f, callPrivateMethod(registration, "scoreToGPA",
                new Class<?>[]{int.class}, new Object[]{60}));
        assertEquals(0.0f, callPrivateMethod(registration, "scoreToGPA",
                new Class<?>[]{int.class}, new Object[]{0}));
        assertEquals(0.0f, callPrivateMethod(registration, "scoreToGPA",
                new Class<?>[]{int.class}, new Object[]{50}));
    }

    @Test
    void testComputeGPA() throws Exception {
        registration.loadSubjects();
        registration.loadExams();

        Student student = (Student) registration.studentList.get(0); // CS001
        float initialGPA = student.GPA;

        callPrivateMethod(registration, "computeGPA",
                new Class<?>[]{Student.class}, new Object[]{student});

        assertEquals(initialGPA, student.GPA, 0.001f);

        Student emptyStudent = new Student("EMPTY001");
        callPrivateMethod(registration, "computeGPA",
                new Class<?>[]{Student.class}, new Object[]{emptyStudent});
        assertEquals(0.0f, emptyStudent.GPA);

        Student fStudent = new Student("FSTUDENT");
        Subject testSubject = (Subject) registration.subjectList.get(0);
        fStudent.lessons.add(fStudent.lessons.size(), new Lessons(testSubject, 50)); // F grade
        callPrivateMethod(registration, "computeGPA",
                new Class<?>[]{Student.class}, new Object[]{fStudent});
        assertEquals(0.0f, fStudent.GPA);

        Student aStudent = new Student("ASTUDENT");
        aStudent.lessons.add(aStudent.lessons.size(), new Lessons(testSubject, 100)); // A grade
        callPrivateMethod(registration, "computeGPA",
                new Class<?>[]{Student.class}, new Object[]{aStudent});
        assertEquals(4.0f, aStudent.GPA, 0.001f);
    }

    @Test
    void testAddExamRecord() {
        registration.loadSubjects();
        registration.loadExams();

        int initialSize = registration.studentList.size();

        registration.addExamRecord("CS001", "HIST101", 85);

        Student student = findStudentByCode("CS001");
        assertNotNull(student);
        boolean foundNewLesson = false;
        for (int i = 0; i < student.lessons.size(); i++) {
            Lessons lesson = (Lessons) student.lessons.get(i);
            if (lesson.learned.code.equals("HIST101")) {
                foundNewLesson = true;
                assertEquals(85, lesson.score);
                break;
            }
        }
        assertTrue(foundNewLesson);

        registration.addExamRecord("NEW001", "CS101", 90);
        assertEquals(initialSize + 1, registration.studentList.size());

        Student newStudent = findStudentByCode("NEW001");
        assertNotNull(newStudent);
        assertEquals(1, newStudent.lessons.size());
        assertEquals("CS101", ((Lessons)newStudent.lessons.get(0)).learned.code);
        assertEquals(90, ((Lessons)newStudent.lessons.get(0)).score);
    }

    @Test
    void testAddExamRecordInvalidSubject() {
        registration.loadSubjects();

        ByteArrayOutputStream outContent = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outContent));

        registration.addExamRecord("CS001", "INVALID999", 85);

        String output = outContent.toString();
        assertTrue(output.contains("Subject not found"));

        System.setOut(System.out);
    }

    @Test
    void testDeleteExamRecord() {
        registration.loadSubjects();
        registration.loadExams();

        Student studentBefore = findStudentByCode("CS001");
        assertNotNull(studentBefore);
        int lessonsBefore = studentBefore.lessons.size();

        registration.deleteExamRecord("CS001", "CS101");

        Student studentAfter = findStudentByCode("CS001");
        assertEquals(lessonsBefore - 1, studentAfter.lessons.size());

        boolean foundDeletedLesson = false;
        for (int i = 0; i < studentAfter.lessons.size(); i++) {
            Lessons lesson = (Lessons) studentAfter.lessons.get(i);
            if (lesson.learned.code.equals("CS101")) {
                foundDeletedLesson = true;
                break;
            }
        }
        assertFalse(foundDeletedLesson);
    }

    @Test
    void testDeleteExamRecordNotFound() {
        registration.loadSubjects();
        registration.loadExams();

        ByteArrayOutputStream outContent = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outContent));

        registration.deleteExamRecord("NONEXISTENT", "CS101");

        String output = outContent.toString();
        assertTrue(output.contains("Record not found"));

        System.setOut(System.out);

        outContent = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outContent));

        registration.deleteExamRecord("CS001", "INVALID999");

        output = outContent.toString();
        assertTrue(output.contains("Record not found"));

        System.setOut(System.out);
    }

    @Test
    void testListStudentsMoreThan3F() {
        registration.loadSubjects();
        registration.loadExams();

        ByteArrayOutputStream outContent = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outContent));

        registration.listStudentsMoreThan3F();

        String output = outContent.toString();
        assertTrue(output.contains("CS003") && output.contains("F="));

        System.setOut(System.out);
    }

    @Test
    void testShowAverageGPA() {
        registration.loadSubjects();
        registration.loadExams();

        ByteArrayOutputStream outContent = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outContent));

        registration.showAverageGPA();

        String output = outContent.toString();
        assertTrue(output.contains("Average GPA:"));

        System.setOut(System.out);
    }

    @Test
    void testShowAverageGPAEmpty() throws Exception {

        Files.writeString(examsFile, "");

        registration.loadExams();

        ByteArrayOutputStream outContent = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outContent));

        registration.showAverageGPA();

        String output = outContent.toString();
        assertTrue(output.contains("No students"));

        System.setOut(System.out);
    }

    @Test
    void testShowSubjects() {
        registration.loadSubjects();

        ByteArrayOutputStream outContent = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outContent));

        registration.showSubjects();

        String output = outContent.toString();
        assertTrue(output.contains("Subjects") && output.contains("CS101"));

        System.setOut(System.out);
    }

    @Test
    void testShowMajors() {
        registration.loadMajors();

        ByteArrayOutputStream outContent = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outContent));

        registration.showMajors();

        String output = outContent.toString();
        assertTrue(output.contains("Majors") && output.contains("Computer Science"));

        System.setOut(System.out);
    }

    @Test
    void testSaveExams() throws Exception {
        registration.loadSubjects();
        registration.loadExams();

        registration.addExamRecord("SAVETEST001", "CS101", 95);
        registration.addExamRecord("SAVETEST002", "MATH101", 87);

        Registration newRegistration = new Registration();
        setPrivateField(newRegistration, "subjectsFile", subjectsFile.toString());
        setPrivateField(newRegistration, "majorsFile", majorsFile.toString());
        setPrivateField(newRegistration, "examsFile", examsFile.toString());

        newRegistration.loadSubjects();
        newRegistration.loadExams();

        boolean foundTest1 = false;
        boolean foundTest2 = false;
        for (int i = 0; i < newRegistration.studentList.size(); i++) {
            Student s = (Student) newRegistration.studentList.get(i);
            if (s.code.equals("SAVETEST001")) {
                foundTest1 = true;
            }
            if (s.code.equals("SAVETEST002")) {
                foundTest2 = true;
            }
        }
        assertTrue(foundTest1 && foundTest2);
    }

    @Test
    void testAdminMenuInvalidPassword() {
        String input = "wrongpassword\n0\n";
        Scanner sc = new Scanner(input);

        ByteArrayOutputStream outContent = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outContent));

        registration.adminMenu(sc);

        String output = outContent.toString();
        assertTrue(output.contains("Access denied") || output.contains("Invalid password"));

        System.setOut(System.out);
    }

    @Test
    void testAdminMenuValidPassword() {
        String input = "admin1234\n0\n";
        Scanner sc = new Scanner(input);

        ByteArrayOutputStream outContent = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outContent));

        registration.adminMenu(sc);

        String output = outContent.toString();
        assertTrue(output.contains("ADMIN MENU") || output.contains("Returning to main menu"));

        System.setOut(System.out);
    }

    @Test
    void testAddSubjectThroughAdmin() throws Exception {
        registration.loadSubjects();
        int initialSize = registration.subjectList.size();

        String input = "admin1234\n1\nTEST001\nTest Subject\n2.0\n0\n";
        Scanner sc = new Scanner(input);
        sc.useDelimiter("\n");

        registration.adminMenu(sc);

        assertEquals(initialSize + 1, registration.subjectList.size());

        Subject newSubject = (Subject) callPrivateMethod(registration, "findSubject",
                new Class<?>[]{String.class}, new Object[]{"TEST001"});
        assertNotNull(newSubject);
        assertEquals("Test Subject", newSubject.name);
        assertEquals(2.0f, newSubject.credit);
    }

    @Test
    void testAddMajorThroughAdmin() throws Exception {
        registration.loadMajors();
        int initialSize = registration.majorList.size();

        String input = "admin1234\n2\nTE\nTest Engineering\n0\n";
        Scanner sc = new Scanner(input);
        sc.useDelimiter("\n");

        registration.adminMenu(sc);

        assertEquals(initialSize + 1, registration.majorList.size());

        Major newMajor = (Major) callPrivateMethod(registration, "findMajorByStudentCode",
                new Class<?>[]{String.class}, new Object[]{"TE001"});
        assertNotNull(newMajor);
        assertEquals("Test Engineering", newMajor.name);
    }

    @Test
    void testAddStudentThroughAdmin() {
        registration.loadExams();
        int initialSize = registration.studentList.size();

        String input = "admin1234\n3\nADMIN999\n0\n";
        Scanner sc = new Scanner(input);
        sc.useDelimiter("\n");

        registration.adminMenu(sc);

        assertEquals(initialSize + 1, registration.studentList.size());

        Student newStudent = findStudentByCode("ADMIN999");
        assertNotNull(newStudent);
        assertEquals(0, newStudent.lessons.size()); // No exams yet
        assertEquals(0.0f, newStudent.GPA); // No GPA calculated yet
    }

    @Test
    void testShowGradesBySubject() {
        registration.loadSubjects();
        registration.loadExams();

        ByteArrayOutputStream outContent = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outContent));

        registration.showGradesBySubject();

        String output = outContent.toString();
        assertTrue(output.contains("Grades by Subject") && output.contains("CS101"));

        System.setOut(System.out);
    }

    @Test
    void testShowGradesByMajor() {
        registration.loadSubjects();
        registration.loadMajors();
        registration.loadExams();

        ByteArrayOutputStream outContent = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outContent));

        registration.showGradesByMajor();

        String output = outContent.toString();
        assertTrue(output.contains("Grades by Major") && output.contains("Computer Science"));

        System.setOut(System.out);
    }

    private Student findStudentByCode(String code) {
        for (int i = 0; i < registration.studentList.size(); i++) {
            Student s = (Student) registration.studentList.get(i);
            if (s.code.equals(code)) {
                return s;
            }
        }
        return null;
    }

    @AfterEach
    void tearDown() {

        System.setOut(System.out);
    }
}