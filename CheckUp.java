
package com.mycompany.healthinformationsystem.CheckUp;

public class CheckUp {
    private final int checkupId;
    private final int residentId;
    private final int workerId;
    private final String checkupDate;
    private final String weight;
    private final String height;
    private final double bmi;
    private final String bmiClassification;
    private final String systolicBp;
    private final String diastolicBp;
    private final String bpCategory;
    private final String bloodSugar;
    private final String classification;
    private final String checkupNotes;

    public CheckUp(int checkupId, int residentId, int workerId,String checkupDate, String weight, String height, double bmi, String bmiClassification,
                     String systolicBp, String diastolicBp, String bpCategory, String bloodSugar, String classification, String checkupNotes ) {
        
        this.checkupId = checkupId;
        this.residentId = residentId;
        this.workerId = workerId;
        this.checkupDate = checkupDate;
        this.weight = weight;
        this.height = height;
        this.bmi = bmi;
        this.bmiClassification = bmiClassification;
        this.systolicBp = systolicBp;
        this.diastolicBp = diastolicBp;
        this.bpCategory = bpCategory;
        this.bloodSugar = bloodSugar;
        this.classification = classification;
        this.checkupNotes = checkupNotes;    
    }

    public int getCheckupId() {
        return checkupId;
    }

    public int getResidentId() {
        return residentId;
    }

    public int getWorkerId() {
        return workerId;
    }

    public String getCheckupDate() {
        return checkupDate;
    }

    public String getWeight() {
        return weight;
    }

    public String getHeight() {
        return height;
    }

    public double getBmi() {
        return bmi;
    }

    public String getBmiClassification() {
        return bmiClassification;
    }

    public String getSystolicBp() {
        return systolicBp;
    }

    public String getDiastolicBp() {
        return diastolicBp;
    }

    public String getBpCategory() {
        return bpCategory;
    }

    public String getBloodSugar() {
        return bloodSugar;
    }

    public String getClassification() {
        return classification;
    }

    public String getCheckupNotes() {
        return checkupNotes;
    }

}
