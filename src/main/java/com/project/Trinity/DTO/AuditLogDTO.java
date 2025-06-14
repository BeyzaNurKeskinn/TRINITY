package com.project.Trinity.DTO;

public class AuditLogDTO {
    private String admin;
    private String action;
    private String timestamp;

    public AuditLogDTO(String admin, String action, String timestamp) {
        this.admin = admin;
        this.action = action;
        this.timestamp = timestamp;
    }

    // Getter ve Setter'lar
    public String getAdmin() { return admin; }
    public void setAdmin(String admin) { this.admin = admin; }
    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }
    public String getTimestamp() { return timestamp; }
    public void setTimestamp(String timestamp) { this.timestamp = timestamp; }
}