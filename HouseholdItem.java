package com.mycompany.healthinformationsystem;

public class HouseholdItem {

    private final int householdId;
    private final int sitioId;
    private final String houseNo;
    private final String economicStatus;

    public HouseholdItem(int householdId, String houseNo, String economicStatus, int sitioId) {
        this.householdId = householdId;
        this.houseNo = houseNo;
        this.economicStatus = economicStatus;
        this.sitioId = sitioId;
    }

    public int getHouseholdId() { return householdId; }
    public int getSitioId() { return sitioId; }
    public String getHouseNo() { return houseNo; }
    public String getEconomicStatus() { return economicStatus; }
    
}