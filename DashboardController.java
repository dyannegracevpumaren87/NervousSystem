package com.mycompany.healthinformatioonsystem.dashboard;

import com.mycompany.healthinformationsystem.HealthView;

public class DashboardController {

    private final HealthView hv;
    private final DashboardDao dao;

    public DashboardController(
            HealthView temp) {

        hv = temp;

        dao = new DashboardDao();

        loadDashboard();
    }

    private void loadDashboard() {

        hv.sitioNo.setText(
                String.valueOf(
                        dao.getTotalSitios()));

        hv.householdNo.setText(
                String.valueOf(
                        dao.getTotalHouseholds()));

        hv.residentsNo.setText(
                String.valueOf(
                        dao.getTotalResidents()));

        hv.healthWorkersNo.setText(
                String.valueOf(
                        dao.getTotalHealthWorkers()));

        hv.checkupsNo.setText(
                String.valueOf(
                        dao.getTodaysCheckups()));
        
    }
}