package hrms.model;

/** Domain model for a job position. */
public class Position {
    private int positionId; private String positionTitle; private String description;
    public Position() { }
    public Position(int positionId, String positionTitle, String description) { this.positionId = positionId; this.positionTitle = positionTitle; this.description = description; }
    public int getPositionId() { return positionId; } public void setPositionId(int value) { positionId = value; }
    public String getPositionTitle() { return positionTitle; } public void setPositionTitle(String value) { positionTitle = value; }
    public String getDescription() { return description; } public void setDescription(String value) { description = value; }
}
