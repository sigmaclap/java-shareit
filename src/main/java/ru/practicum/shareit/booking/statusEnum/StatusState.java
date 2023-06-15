package ru.practicum.shareit.booking.statusEnum;

import lombok.Generated;

@Generated
public enum StatusState {
    ALL("ALL"),
    CURRENT("CURRENT"),
    PAST("PAST"),
    FUTURE("FUTURE"),
    REJECTED("REJECTED"),
    WAITING("WAITING");

    private final String stateValue;

    StatusState(String stateValue) {
        this.stateValue = stateValue;
    }

    public String getStateValue() {
        return stateValue;
    }
}
