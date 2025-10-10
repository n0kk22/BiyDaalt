import dataStructures.Chain;

public class Student {
    public String code;
    public float GPA;
    public Chain lessons;

    public Student(String code) {
        this.code = code;
        lessons = new Chain();
        GPA = 0f;
    }

}
