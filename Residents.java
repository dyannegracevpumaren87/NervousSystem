package com.mycompany.healthinformationsystem.Residents;

public class Residents {
    
    private final int householdId;
    private final int barangayId;
    private final String fullName;
    private final String birthdate;
    private final String sex;
    private final String civilStatus;
    private final String contactNumber;
    private final String emailAddress;
    private final String disability;
    private final String pwdId;

    public int getHouseholdId() {
        return householdId;
    }
    
    public int getBarangayId() {
        return barangayId;
    }

    public String getFullName() {
        return fullName;
    }

    public String getBirthdate() {
        return birthdate;
    }

    public String getSex() {
        return sex;
    }

    public String getCivilStatus() {
        return civilStatus;
    }

    public String getContactNumber() {
        return contactNumber;
    }

    public String getEmailAddress() {
        return emailAddress;
    }

    public String getDisability() {
        return disability;
    }

    public String getPwdId() {
        return pwdId;
    }
    
    public Residents(int householdId, int barangayId, String fullName, String birthdate, String sex, String civilStatus, String contactNumber, String emailAddress, String disability, String pwdId) {
        this.householdId = householdId;
        this.barangayId = barangayId;
        this.fullName = fullName;
        this.birthdate = birthdate;
        this.sex = sex;
        this.civilStatus = civilStatus;
        this.contactNumber = contactNumber;
        this.emailAddress = emailAddress;
        this.disability = disability;
        this.pwdId = pwdId;
    }
}

  