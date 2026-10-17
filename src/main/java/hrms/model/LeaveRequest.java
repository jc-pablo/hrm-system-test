package hrms.model;

import java.time.LocalDate;

/** Domain model for employee leave requests and review status. */
public class LeaveRequest {
    private int leaveId, employeeId; private String leaveType, reason, status; private LocalDate startDate, endDate, dateFiled;
    public LeaveRequest() { }
    public LeaveRequest(int leaveId, int employeeId, String leaveType, LocalDate startDate, LocalDate endDate, String reason, String status, LocalDate dateFiled) { this.leaveId=leaveId; this.employeeId=employeeId; this.leaveType=leaveType; this.startDate=startDate; this.endDate=endDate; this.reason=reason; this.status=status; this.dateFiled=dateFiled; }
    public int getLeaveId(){return leaveId;} public void setLeaveId(int v){leaveId=v;}
    public int getEmployeeId(){return employeeId;} public void setEmployeeId(int v){employeeId=v;}
    public String getLeaveType(){return leaveType;} public void setLeaveType(String v){leaveType=v;}
    public LocalDate getStartDate(){return startDate;} public void setStartDate(LocalDate v){startDate=v;}
    public LocalDate getEndDate(){return endDate;} public void setEndDate(LocalDate v){endDate=v;}
    public String getReason(){return reason;} public void setReason(String v){reason=v;}
    public String getStatus(){return status;} public void setStatus(String v){status=v;}
    public LocalDate getDateFiled(){return dateFiled;} public void setDateFiled(LocalDate v){dateFiled=v;}
}
