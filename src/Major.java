public class Major {
    public String code;
    public String name;

    public Major(String code, String name) {
        this.code = code;
        this.name = name;
    }

    public String toString() {
        return code + " - " + name;
    }

}
