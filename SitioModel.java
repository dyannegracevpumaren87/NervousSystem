package com.mycompany.healthinforamtionsystem.Sitio;

public class SitioModel {

    private int householdId;
    private String houseNo;
    private String economicStatus;

    public int getHouseholdId() {
        return householdId;
    }

    public void setHouseholdId(int householdId) {
        this.householdId = householdId;
    }

    public String getHouseNo() {
        return houseNo;
    }

    public void setHouseNo(String houseNo) {
        this.houseNo = houseNo;
    }

    public String getEconomicStatus() {
        return economicStatus;
    }

    public void setEconomicStatus(String economicStatus) {
        this.economicStatus = economicStatus;
    }
}