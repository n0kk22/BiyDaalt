package main;

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
            System.out.println("\n===== OYUTNII DUN BURTGEL=====");
            System.out.println("1. Buh hicheeliih haruulah");
            System.out.println("2. Buh mergejliig haruulah");
            System.out.println("3. Hicheeleer dung jagsaah");
            System.out.println("4. Mergejleer dung jagsaah");
            System.out.println("5. Niit suragchiin golchiig haruulah");
            System.out.println("6. 3 ba tuunees deesh F tei suragchid");
            System.out.println("7. Shalgaltiin dun oruulah");
            System.out.println("8. Dun ustgah");
            System.out.println("9. Admin mode");
            System.out.println("0. Garah");
            System.out.print("Songolt: ");

            while (!sc.hasNextInt()) {
<<<<<<< HEAD:src/Main.java
                System.out.println("Please enter a valid number.");
                sc.next(); 
                System.out.print("Choose option: ");
            }
            choice = sc.nextInt();
            sc.nextLine(); 
=======
                System.out.println("Deerh toonuudaas oruulna uu.");
                sc.next();
                System.out.print("Songolt: ");
            }
            choice = sc.nextInt();
            sc.nextLine();
>>>>>>> 5b278b0 (main func admin):src/main/Main.java

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
                    System.out.print("Oyutnii code: ");
                    String studentCode = sc.nextLine().trim();
                    System.out.print("Hicheeliin code: ");
                    String subCode = sc.nextLine().trim();
                    System.out.print("Shalgaltiin dun(0–100): ");
                    int score = sc.nextInt();
                    sc.nextLine();
                    reg.addExamRecord(studentCode, subCode, score);
                    break;
                case 8:
                    System.out.print("Oyutnii code: ");
                    String delStudent = sc.nextLine().trim();
                    System.out.print("Hicheeliin code: ");
                    String delSubject = sc.nextLine().trim();
                    reg.deleteExamRecord(delStudent, delSubject);
                    break;
                case 9:
                    reg.adminMenu(sc);
                    break;
                case 0:
                    System.out.println("Systemees garch baina... ");
                    break;
                default:
                    System.out.println("Iim songolt alga. Dahin oroldono uu.");
            }
        } while (choice != 0);

        sc.close();
    }
}

