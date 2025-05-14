package com.project.Trinity.Entity;

public enum Status {
    ACTIVE("AKTİF"),
    INACTIVE("PASİF");

    private final String displayName;

    Status(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public static Status fromDisplayName(String displayName) {
        for (Status status : Status.values()) {
            if (status.displayName.equalsIgnoreCase(displayName)) {
                return status;
            }
        }
        throw new IllegalArgumentException("Geçersiz durum: " + displayName);
    }
}