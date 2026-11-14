package com.mycompany.healthinformationsystem.dashboard;

public class DashboardModel {

    private int totalSitios;
    private int totalHouseholds;
    private int totalResidents;
    private int totalHealthWorkers;
    private int todaysAppointments;

    public int getTotalSitios() {
        return totalSitios;
    }

    public void setTotalSitios(int totalSitios) {
        this.totalSitios = totalSitios;
    }

    public int getTotalHouseholds() {
        return totalHouseholds;
    }

    public void setTotalHouseholds(int totalHouseholds) {
        this.totalHouseholds = totalHouseholds;
    }

    public int getTotalResidents() {
        return totalResidents;
    }

    public void setTotalResidents(int totalResidents) {
        this.totalResidents = totalResidents;
    }

    public int getTotalHealthWorkers() {
        return totalHealthWorkers;
    }

    public void setTotalHealthWorkers(int totalHealthWorkers) {
        this.totalHealthWorkers = totalHealthWorkers;
    }

    public int getTodaysAppointments() {
        return todaysAppointments;
    }

    public void setTodaysAppointments(int todaysAppointments) {
        this.todaysAppointments = todaysAppointments;
    }
}