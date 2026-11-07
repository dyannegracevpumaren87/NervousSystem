package com.mycompany.healthinformatioonsystem.dashboard;

public class DashboardModel {

    private int totalSitios;
    private int totalHouseholds;
    private int totalResidents;
    private int totalHealthWorkers;
    private int todaysCheckups;

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

    public int getTodaysCheckups() {
        return todaysCheckups;
    }

    public void setTodaysCheckups(int todaysCheckups) {
        this.todaysCheckups = todaysCheckups;
    }
}