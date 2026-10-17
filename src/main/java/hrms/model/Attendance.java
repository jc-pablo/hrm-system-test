package hrms.model;

import java.time.LocalDate;
import java.time.LocalTime;

/** Domain model for daily employee attendance. */
public class Attendance {
    private int attendanceId, employeeId; private LocalDate attendanceDate; private LocalTime timeIn, timeOut; private String status;
    public Attendance() { }
    public Attendance(int attendanceId, int employeeId, LocalDate attendanceDate, LocalTime timeIn, LocalTime timeOut, String status) { this.attendanceId=attendanceId; this.employeeId=employeeId; this.attendanceDate=attendanceDate; this.timeIn=timeIn; this.timeOut=timeOut; this.status=status; }
    public int getAttendanceId(){return attendanceId;} public void setAttendanceId(int v){attendanceId=v;}
    public int getEmployeeId(){return employeeId;} public void setEmployeeId(int v){employeeId=v;}
    public LocalDate getAttendanceDate(){return attendanceDate;} public void setAttendanceDate(LocalDate v){attendanceDate=v;}
    public LocalTime getTimeIn(){return timeIn;} public void setTimeIn(LocalTime v){timeIn=v;}
    public LocalTime getTimeOut(){return timeOut;} public void setTimeOut(LocalTime v){timeOut=v;}
    public String getStatus(){return status;} public void setStatus(String v){status=v;}
}
