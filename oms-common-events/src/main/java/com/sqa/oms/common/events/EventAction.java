package com.sqa.oms.common.events;

public enum EventAction {
    CREATE("CREATED"),
    UPDATE("UPDATED"),
    DELETE("DELETED");

    private final String pastTense;

    EventAction(final String pastTense) {
        this.pastTense = pastTense;
    }

    public String pastTense() {
        return pastTense;
    }
}
