
package com.mycompany.healthinformationsystem;
public class HealthInformationSystem {

    public static void main(String[] args) {
    HealthView hv = new HealthView();
    hv.setVisible(true);
     new HealthController (hv);
    
    }
}
