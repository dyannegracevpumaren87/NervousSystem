package com.mycompany.healthinformationsystem;


import com.mycompany.healthinforamtionsystem.Sitio.SitioController;
import com.mycompany.healthinformationsystem.CheckUp.CheckUpController;
import com.mycompany.healthinformationsystem.Residents.ResidentsController;
import com.mycompany.healthinformationsystem.HealthWorkers.HealthWorkersController;
import com.mycompany.healthinformatioonsystem.dashboard.DashboardController;



public class HealthInformationSystem {

    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(HealthView.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(HealthView.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(HealthView.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(HealthView.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
               java.awt.EventQueue.invokeLater(() ->{
            HealthView view = new HealthView();
            new DashboardController(view);
            new SitioController(view);
            new HealthController(view);
            new ResidentsController(view);
            new CheckUpController(view);
            new HealthWorkersController(view);

            view.setVisible(true);
        });
    }
    }
