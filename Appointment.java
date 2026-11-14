package com.mycompany.healthinformationsystem.Appointment;

public class Appointment {
    private final int appointmentId;
    private final int residentId;
    private final String healthWorkerId;
    private final String appointmentDate;
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

    public Appointment(int appointmentId, int residentId, String healthWorkerId, String appointmentDate, String weight, String height, double bmi, String bmiClassification,
                     String systolicBp, String diastolicBp, String bpCategory, String bloodSugar, String classification, String checkupNotes ) {
        
        this.appointmentId = appointmentId;
        this.residentId = residentId;
        this.healthWorkerId = healthWorkerId;
        this.appointmentDate = appointmentDate;
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

    public int getAppointmentId() {
        return appointmentId;
    }

    public int getResidentId() {
        return residentId;
    }

    public String getHealthWorkerId() {
        return healthWorkerId;
    }

    public String getAppointmentDate() {
        return appointmentDate;
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