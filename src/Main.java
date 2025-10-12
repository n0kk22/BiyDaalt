import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Registration reg = new Registration();
        Scanner sc = new Scanner(System.in);

        reg.loadSubjects();
        reg.loadMajors();
        reg.loadExams();

        int choice;
        do {
            System.out.println("\n===== STUDENT REGISTRATION SYSTEM =====");
            System.out.println("1. Show all subjects");
            System.out.println("2. Show all majors");
            System.out.println("3. Show grades by subject");
            System.out.println("4. Show grades by major");
            System.out.println("5. Show average GPA of all students");
            System.out.println("6. List students with more than 3 F's");
            System.out.println("7. Add exam record");
            System.out.println("8. Delete exam record");
            System.out.println("9. Admin mode");
            System.out.println("0. Exit");
            System.out.print("Choose option: ");

            while (!sc.hasNextInt()) {
                System.out.println("Please enter a valid number.");
                sc.next(); 
                System.out.print("Choose option: ");
            }
            choice = sc.nextInt();
            sc.nextLine(); 

            switch (choice) {
                case 1:
                    reg.showSubjects();
                    break;
                case 2:
                    reg.showMajors();
                    break;
                case 3:
                    reg.showGradesBySubject();
                    break;
                case 4:
                    reg.showGradesByMajor();
                    break;
                case 5:
                    reg.showAverageGPA();
                    break;
                case 6:
                    reg.listStudentsMoreThan3F();
                    break;
                case 7:
                    System.out.print("Enter student code: ");
                    String studentCode = sc.nextLine().trim();
                    System.out.print("Enter subject code: ");
                    String subCode = sc.nextLine().trim();
                    System.out.print("Enter score (0–100): ");
                    int score = sc.nextInt();
                    sc.nextLine();
                    reg.addExamRecord(studentCode, subCode, score);
                    break;
                case 8:
                    System.out.print("Enter student code: ");
                    String delStudent = sc.nextLine().trim();
                    System.out.print("Enter subject code: ");
                    String delSubject = sc.nextLine().trim();
                    reg.deleteExamRecord(delStudent, delSubject);
                    break;
                case 9:
                    reg.adminMenu(sc);
                    break;
                case 0:
                    System.out.println("Exiting... Goodbye!");
                    break;
                default:
                    System.out.println("Invalid option. Try again.");
            }
        } while (choice != 0);

        sc.close();
    }
}

