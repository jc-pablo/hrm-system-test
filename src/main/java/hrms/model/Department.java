package hrms.model;

/** Domain model for an organizational department. */
public class Department {
    private int departmentId; private String departmentName; private String description;
    public Department() { }
    public Department(int departmentId, String departmentName, String description) { this.departmentId = departmentId; this.departmentName = departmentName; this.description = description; }
    public int getDepartmentId() { return departmentId; } public void setDepartmentId(int value) { departmentId = value; }
    public String getDepartmentName() { return departmentName; } public void setDepartmentName(String value) { departmentName = value; }
    public String getDescription() { return description; } public void setDescription(String value) { description = value; }
}
