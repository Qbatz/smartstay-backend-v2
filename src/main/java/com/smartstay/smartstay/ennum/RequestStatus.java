package com.smartstay.smartstay.ennum;

import lombok.Getter;

import java.util.Set;

@Getter
public enum RequestStatus {
    NEW("New", "New demo request is requested"),
    ASSIGNED("Assigned", "Demo request assigned to agent"),
    CONTACTED("Contacted", "Contacted the customer"),
    DEMO_SCHEDULED("Demo Scheduled", "Demo scheduled with the customer"),
    DEMO_COMPLETED("Demo Completed", "Demo completed by the agent"),
    TRIAL_STARTED("Trial Started", "Trial started by the customer"),
    CONVERTED("Converted", "Converted to a paid plan"),
    DROPPED("Dropped", "Dropped the demo request");

    private final String value;
    private final String description;

    RequestStatus(String value, String description) {
        this.value = value;
        this.description = description;
    }

    public Set<RequestStatus> getAllowedStatuses() {
        return switch (this) {
            case NEW -> Set.of(ASSIGNED, DROPPED);
            case ASSIGNED -> Set.of(ASSIGNED, CONTACTED, DROPPED);
            case CONTACTED -> Set.of(DEMO_SCHEDULED, DROPPED);
            case DEMO_SCHEDULED -> Set.of(DEMO_COMPLETED, DROPPED);
            case DEMO_COMPLETED -> Set.of(TRIAL_STARTED, DROPPED);
            case TRIAL_STARTED -> Set.of(CONVERTED, DROPPED);
            case CONVERTED -> Set.of();
            case DROPPED -> Set.of(ASSIGNED);
        };
    }

    public boolean canMoveTo(RequestStatus nextStatus) {
        return getAllowedStatuses().contains(nextStatus);
    }
}
