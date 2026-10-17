package hrms.model;

import java.time.LocalDate;

/** Domain model for employee personal and employment details. */
public class Employee {
    private int employeeId; private String employeeNo, firstName, lastName, gender, email, phone, address, status;
    private LocalDate birthDate, dateHired; private int departmentId, positionId;
    public Employee() { }
    public Employee(int employeeId, String employeeNo, String firstName, String lastName, LocalDate birthDate, String gender, String email, String phone, String address, LocalDate dateHired, String status, int departmentId, int positionId) {
        this.employeeId=employeeId; this.employeeNo=employeeNo; this.firstName=firstName; this.lastName=lastName; this.birthDate=birthDate; this.gender=gender; this.email=email; this.phone=phone; this.address=address; this.dateHired=dateHired; this.status=status; this.departmentId=departmentId; this.positionId=positionId;
    }
    public int getEmployeeId(){return employeeId;} public void setEmployeeId(int v){employeeId=v;}
    public String getEmployeeNo(){return employeeNo;} public void setEmployeeNo(String v){employeeNo=v;}
    public String getFirstName(){return firstName;} public void setFirstName(String v){firstName=v;}
    public String getLastName(){return lastName;} public void setLastName(String v){lastName=v;}
    public LocalDate getBirthDate(){return birthDate;} public void setBirthDate(LocalDate v){birthDate=v;}
    public String getGender(){return gender;} public void setGender(String v){gender=v;}
    public String getEmail(){return email;} public void setEmail(String v){email=v;}
    public String getPhone(){return phone;} public void setPhone(String v){phone=v;}
    public String getAddress(){return address;} public void setAddress(String v){address=v;}
    public LocalDate getDateHired(){return dateHired;} public void setDateHired(LocalDate v){dateHired=v;}
    public String getStatus(){return status;} public void setStatus(String v){status=v;}
    public int getDepartmentId(){return departmentId;} public void setDepartmentId(int v){departmentId=v;}
    public int getPositionId(){return positionId;} public void setPositionId(int v){positionId=v;}
}
