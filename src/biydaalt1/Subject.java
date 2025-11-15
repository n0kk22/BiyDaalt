package biydaalt1;

public class Subject {
    public String code;
    public String name;
    public float credit;

    public Subject(String code, String name, float credit) {
        this.code = code;
        this.name = name;
        this.credit = credit;
    }

    public String toString() {
        return code + " - " + name + " (" + credit + ")";
    }

}
