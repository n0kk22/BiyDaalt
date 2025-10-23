import dataStructures.ArrayLinearList;

import java.io.*;
import java.util.*;

public class Registration {

    public ArrayLinearList studentList = new ArrayLinearList();
    public ArrayLinearList subjectList = new ArrayLinearList();
    public ArrayLinearList majorList = new ArrayLinearList();

    private String subjectsFile = "src//Subjects.txt";
    private String majorsFile = "src//Professions.txt";
    private String examsFile = "src//Exams.txt";

    private Subject findSubject(String code) {
        for (int i = 0; i < subjectList.size(); i++) {
            Subject s = (Subject) subjectList.get(i);
            if (s.code.equals(code)) return s;
        }
        return null;
    }

    private Major findMajorByStudentCode(String studentCode) {
        if (studentCode.length() < 2) return null;
        String prefix = studentCode.substring(0, 2);
        for (int i = 0; i < majorList.size(); i++) {
            Major m = (Major) majorList.get(i);
            if (m.code.equals(prefix)) return m;
        }
        return null;
    }

    public void loadSubjects() {
        try (BufferedReader br = new BufferedReader(new FileReader(subjectsFile))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty()) continue;
                String[] parts = line.split("/");
                if (parts.length < 3) continue;
                String code = parts[0];
                String name = parts[1];
                float credit = Float.parseFloat(parts[2]);
                subjectList.add(subjectList.size(), new Subject(code, name, credit));
            }
        } catch (IOException e) {
            System.out.println("Error reading subjects: " + e);
            System.exit(1);
        }
    }

    public void loadMajors() {
        try (BufferedReader br = new BufferedReader(new FileReader(majorsFile))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty()) continue;
                String[] parts = line.split("/");
                if (parts.length < 2) continue;
                String code = parts[0];
                String name = parts[1];
                majorList.add(majorList.size(), new Major(code, name));
            }
        } catch (IOException e) {
            System.out.println("Error reading majors: " + e);
            System.exit(1);
        }
    }

    public void loadExams() {
        HashMap<String, Student> map = new LinkedHashMap<>();
        try (BufferedReader br = new BufferedReader(new FileReader(examsFile))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty()) continue;
                String[] parts = line.split("/");
                if (parts.length < 3) continue;
                String studCode = parts[0];
                String subCode = parts[1];
                int score = Integer.parseInt(parts[2]);
                Subject subj = findSubject(subCode);
                if (subj == null) {
                    System.out.println("Warning: subject not found: " + subCode);
                    continue;
                }
                Student st = map.get(studCode);
                if (st == null) {
                    st = new Student(studCode);
                    map.put(studCode, st);
                }
                st.lessons.add(st.lessons.size(), new Lessons(subj, score));
            }
        } catch (FileNotFoundException e) {
            System.out.println("Exams file not found. A new one will be created.");
        } catch (IOException e) {
            System.out.println("Error reading exams: " + e);
        }

        for (Student s : map.values()) {
            computeGPA(s);
            studentList.add(studentList.size(), s);
        }
    }

    private float scoreToGPA(int score) {
        if (score >= 96) return 4.0f;
        if (score >= 91) return 3.7f;
        if (score >= 88) return 3.4f;
        if (score >= 84) return 3.0f;
        if (score >= 81) return 2.7f;
        if (score >= 78) return 2.4f;
        if (score >= 74) return 2.0f;
        if (score >= 71) return 1.7f;
        if (score >= 68) return 1.3f;
        if (score >= 64) return 1.0f;
        if (score >= 61) return 0.7f;
        return 0.0f;
    }

    private void computeGPA(Student s) {
        float total = 0f, credits = 0f;
        for (int i = 0; i < s.lessons.size(); i++) {
            Lessons ls = (Lessons) s.lessons.get(i);
            float gp = scoreToGPA(ls.score);
            total += gp * ls.learned.credit;
            credits += ls.learned.credit;
        }
        s.GPA = credits > 0 ? total / credits : 0f;
    }

    public void saveExams() {
        try (PrintWriter pw = new PrintWriter(new FileWriter(examsFile))) {
            for (int i = 0; i < studentList.size(); i++) {
                Student s = (Student) studentList.get(i);
                for (int j = 0; j < s.lessons.size(); j++) {
                    Lessons ls = (Lessons) s.lessons.get(j);
                    pw.println(s.code + "/" + ls.learned.code + "/" + ls.score);
                }
            }
        } catch (IOException e) {
            System.out.println("Error writing exams: " + e);
        }
    }

    public void showSubjects() {
        System.out.println("--- Subjects ---");
        for (int i = 0; i < subjectList.size(); i++) {
            System.out.println(subjectList.get(i));
        }
    }

    public void showMajors() {
        System.out.println("--- Majors ---");
        for (int i = 0; i < majorList.size(); i++) {
            System.out.println(majorList.get(i));
        }
    }

    public void showAverageGPA() {
        if (studentList.isEmpty()) {
            System.out.println("No students");
            return;
        }
        float sum = 0;
        int n = studentList.size();
        for (int i = 0; i < n; i++) {
            Student s = (Student) studentList.get(i);
            sum += s.GPA;
        }
        System.out.printf("Average GPA: %.3f\n", sum / n);
    }

    public void listStudentsMoreThan3F() {
        System.out.println("--- Students with >3 F ---");
        boolean found = false;
        for (int i = 0; i < studentList.size(); i++) {
            Student s = (Student) studentList.get(i);
            int fCount = 0;
            for (int j = 0; j < s.lessons.size(); j++) {
                Lessons ls = (Lessons) s.lessons.get(j);
                if (scoreToGPA(ls.score) == 0f) fCount++;
            }
            if (fCount > 3) {
                System.out.println(s.code + " F=" + fCount);
                found = true;
            }
        }
        if (!found) System.out.println("No students found");
    }

    public void showGradesBySubject() {
        System.out.println("--- Grades by Subject ---");
        for (int i = 0; i < subjectList.size(); i++) {
            Subject sub = (Subject) subjectList.get(i);
            System.out.println(sub);
            ArrayList<StudentScore> studentScores = new ArrayList<>();

            for (int j = 0; j < studentList.size(); j++) {
                Student s = (Student) studentList.get(j);
                for (int k = 0; k < s.lessons.size(); k++) {
                    Lessons ls = (Lessons) s.lessons.get(k);
                    if (ls.learned.code.equals(sub.code)) {
                        studentScores.add(new StudentScore(s, ls.score));
                    }
                }
            }

            studentScores.sort((a, b) -> b.score - a.score);
            for (StudentScore ss : studentScores) {
                System.out.println("  " + ss.student.code + " - " + ss.score);
            }
            if (studentScores.isEmpty()) System.out.println("  (no records)");
        }
    }

    private static class StudentScore {
        Student student;
        int score;

        StudentScore(Student student, int score) {
            this.student = student;
            this.score = score;
        }
    }

    public void showGradesByMajor() {
        System.out.println("--- Grades by Major ---");
        for (int i = 0; i < majorList.size(); i++) {
            Major m = (Major) majorList.get(i);
            System.out.println(m);
            boolean any = false;
            for (int j = 0; j < studentList.size(); j++) {
                Student s = (Student) studentList.get(j);
                if (s.code.startsWith(m.code)) {
                    any = true;
                    System.out.printf("  %s - GPA: %.3f\n", s.code, s.GPA);
                }
            }
            if (!any) System.out.println("  (no students)");
        }
    }

    public void addExamRecord(String studentCode, String subCode, int score) {
        Subject subj = findSubject(subCode);
        if (subj == null) {
            System.out.println("Subject not found");
            return;
        }
        Student st = null;
        for (int i = 0; i < studentList.size(); i++) {
            Student s = (Student) studentList.get(i);
            if (s.code.equals(studentCode)) {
                st = s;
                break;
            }
        }
        if (st == null) {
            st = new Student(studentCode);
            studentList.add(studentList.size(), st);
        }
        st.lessons.add(st.lessons.size(), new Lessons(subj, score));
        computeGPA(st);
        saveExams();
        System.out.println("Record added");
    }

    public void deleteExamRecord(String studentCode, String subCode) {
        boolean found = false;
        for (int i = 0; i < studentList.size(); i++) {
            Student s = (Student) studentList.get(i);
            if (!s.code.equals(studentCode)) continue;

            for (int j = 0; j < s.lessons.size(); j++) {
                Lessons ls = (Lessons) s.lessons.get(j);
                if (ls.learned.code.equals(subCode)) {
                    s.lessons.remove(j);
                    computeGPA(s);
                    found = true;
                    break;
                }
            }
            if (found) break;
        }

        if (found) {
            saveExams();
            System.out.println("Record deleted");
        } else {
            System.out.println("Record not found");
        }
    }

    public void adminMenu(Scanner sc) {
        System.out.print("Enter admin password: ");
        String password = sc.nextLine().trim();

        if (!password.equals("admin1234")) {
            System.out.println("Access denied. Invalid password.");
            return;
        }

        int choice;
        do {
            System.out.println("\n===== ADMIN MENU =====");
            System.out.println("1. Add new subject");
            System.out.println("2. Add new major");
            System.out.println("3. Add new student");
            System.out.println("0. Return to main menu");
            System.out.print("Choose option: ");

            while (!sc.hasNextInt()) {
                System.out.println("Please enter a valid number.");
                sc.next();
                System.out.print("Choose option: ");
            }
            choice = sc.nextInt();
            sc.nextLine(); // consume newline

            switch (choice) {
                case 1:
                    addSubject(sc);
                    break;
                case 2:
                    addMajor(sc);
                    break;
                case 3:
                    addStudent(sc);
                    break;
                case 0:
                    System.out.println("Returning to main menu...");
                    break;
                default:
                    System.out.println("Invalid choice.");
            }
        } while (choice != 0);
    }

    private void addSubject(Scanner sc) {
        System.out.print("Enter subject code: ");
        String code = sc.nextLine().trim();
        System.out.print("Enter subject name: ");
        String name = sc.nextLine().trim();
        System.out.print("Enter subject credit: ");
        float credit = sc.nextFloat();
        sc.nextLine();

        subjectList.add(subjectList.size(), new Subject(code, name, credit));

        // Append to file
        try (PrintWriter pw = new PrintWriter(new FileWriter("Subjects.txt", true))) {
            pw.println(code + "/" + name + "/" + credit);
        } catch (IOException e) {
            System.out.println("Error saving subject: " + e.getMessage());
        }

        System.out.println("Subject added successfully.");
    }

    private void addMajor(Scanner sc) {
        System.out.print("Enter major code (2 letters): ");
        String code = sc.nextLine().trim();
        System.out.print("Enter major name: ");
        String name = sc.nextLine().trim();

        majorList.add(majorList.size(), new Major(code, name));

        try (PrintWriter pw = new PrintWriter(new FileWriter("Professions.txt", true))) {
            pw.println(code + "/" + name);
        } catch (IOException e) {
            System.out.println("Error saving major: " + e.getMessage());
        }

        System.out.println("Major added successfully.");
    }

    private void addStudent(Scanner sc) {
        System.out.print("Enter student code: ");
        String code = sc.nextLine().trim();

        Student s = new Student(code);
        studentList.add(studentList.size(), s);

        System.out.println("Student added successfully (no exams yet).");
    }

}