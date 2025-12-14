package scene1;

public class UserSession {
    private static String userId;
    private static String userName;
    private static String role; // student / teacher

    public static void set(String id, String name, String r) {
        userId = id;
        userName = name;
        role = r;
    }

    public static String getUserId() {
        return userId;
    }

    public static String getUserName() {
        return userName;
    }

    public static String getRole() {
        return role;
    }

    public static boolean isTeacher() {
        return "teacher".equals(role);
    }

    public static boolean isStudent() {
        return "student".equals(role);
    }
}
