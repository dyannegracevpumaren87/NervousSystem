package com.mycompany.healthinformationsystem;
import com.mycompany.healthinformationsystem.Appointment.AppointmentController;
import com.mycompany.healthinformationsystem.Household.HouseholdController;
import com.mycompany.healthinformationsystem.HealthWorkers.HealthWorkersController;
import com.mycompany.healthinformationsystem.Residents.ResidentsController;
import com.mycompany.healthinformationsystem.dashboard.DashboardController;

public class HealthInformationSystem {
 
    public static void main(String args[]) {
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException | InstantiationException
                 | IllegalAccessException | javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(HealthView.class.getName())
                    .log(java.util.logging.Level.SEVERE, null, ex);
        }
 
        java.awt.EventQueue.invokeLater(() -> {
            HealthView view = new HealthView();
            new DashboardController(view);
            new HouseholdController(view);
            new ResidentsController(view);
            new AppointmentController(view);
            new HealthWorkersController(view);
 
            view.setVisible(true);
        });
    }
}