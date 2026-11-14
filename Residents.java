package com.mycompany.healthinformationsystem.Residents;

import java.time.LocalDate;

public class Residents {

    private int residentId;
    private int householdId;
    private String barangayId;
    private String firstName;
    private String middleName;
    private String lastName;
    private String suffix;
    private LocalDate birthdate;
    private String sex;
    private String civilStatus;
    private String contactNumber;
    private String emailAddress;
    private String disability;
    private String pwdId;

    public Residents() {}

    public int getResidentId() { return residentId; }
    public void setResidentId(int residentId) { this.residentId = residentId; }

    public int getHouseholdId() { return householdId; }
    public void setHouseholdId(int householdId) { this.householdId = householdId; }

    public String getBarangayId() { return barangayId; }
    public void setBarangayId(String barangayId) { this.barangayId = barangayId; }

    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }

    public String getMiddleName() { return middleName; }
    public void setMiddleName(String middleName) { this.middleName = middleName; }

    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }

    public String getSuffix() { return suffix; }
    public void setSuffix(String suffix) { this.suffix = suffix; }

    public LocalDate getBirthdate() { return birthdate; }
    public void setBirthdate(LocalDate birthdate) { this.birthdate = birthdate; }

    public String getSex() { return sex; }
    public void setSex(String sex) { this.sex = sex; }

    public String getCivilStatus() { return civilStatus; }
    public void setCivilStatus(String civilStatus) { this.civilStatus = civilStatus; }

    public String getContactNumber() { return contactNumber; }
    public void setContactNumber(String contactNumber) { this.contactNumber = contactNumber; }

    public String getEmailAddress() { return emailAddress; }
    public void setEmailAddress(String emailAddress) { this.emailAddress = emailAddress; }

    public String getDisability() { return disability; }
    public void setDisability(String disability) { this.disability = disability; }

    public String getPwdId() { return pwdId; }
    public void setPwdId(String pwdId) { this.pwdId = pwdId; }

    /** "First Middle Last Suffix" name shown in the table's Full Name column. */
    public String getFullName() {
        StringBuilder sb = new StringBuilder();
        sb.append(firstName == null ? "" : firstName);
        if (middleName != null && !middleName.isBlank()) sb.append(' ').append(middleName);
        sb.append(' ').append(lastName == null ? "" : lastName);
        if (suffix != null && !suffix.isBlank()) sb.append(' ').append(suffix);
        return sb.toString().trim();
    }
}