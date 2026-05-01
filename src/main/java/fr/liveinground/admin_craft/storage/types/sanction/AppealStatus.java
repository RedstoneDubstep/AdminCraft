package fr.liveinground.admin_craft.storage.types.sanction;

public enum AppealStatus {
    NOT_ALLOWED("Not allowed"),
    DELAYED("Delayed"),
    NOT_REQUESTED("Not requested"),
    IN_PROGRESS("In progress"),
    REFUSED("Refused"),
    ACCEPTED("Accepted");

    private final String label;

    AppealStatus(String label) {
        this.label = label;
    }

    public String status() {
        return label;
    }
}
