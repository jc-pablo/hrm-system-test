package hrms.model;

/** Authenticated application user. */
public final class User {
    private final int userId;
    private final String username;
    private final String fullName;
    private final String role;

    public User(int userId, String username, String fullName, String role) {
        this.userId = userId;
        this.username = username;
        this.fullName = fullName;
        this.role = role;
    }

    public int getUserId() { return userId; }
    public String getUsername() { return username; }
    public String getFullName() { return fullName; }
    public String getRole() { return role; }
    public boolean isAdministrator() { return "ADMIN".equals(role); }
}
