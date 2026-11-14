package com.mycompany.healthinformationsystem.dashboard;

import com.mycompany.healthinformationsystem.HealthView;

public class DashboardController {

    private final HealthView hv;
    private final DashboardDao dao;

    public DashboardController(HealthView temp) {

        hv = temp;
        dao = new DashboardDao();

        // Register this controller in the view
        hv.setDashboardController(this);

        loadDashboard();
    }

    public void loadDashboard() {

        hv.householdNo.setText(
                String.valueOf(
                        dao.getTotalHouseholds()));

        hv.residentsNo.setText(
                String.valueOf(
                        dao.getTotalResidents()));

        hv.healthWorkersNo.setText(
                String.valueOf(
                        dao.getTotalHealthWorkers()));

        // "Appointments Today" tile (the label is still named checkupsNo in HealthView)
        hv.checkupsNo.setText(
                String.valueOf(
                        dao.getTodaysAppointments()));
    }
}