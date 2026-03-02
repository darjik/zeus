package org.zeus.ims.entity;

public enum UserRole {
    OWNER("Owner"),
    SALES("Sales"),
    PRODUCTION_MANAGER("Production Manager"),
    WORKSHOP_PERSONNEL("Workshop Personnel"),
    ACCOUNTANT("Accountant");

    private final String displayName;

    UserRole(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
