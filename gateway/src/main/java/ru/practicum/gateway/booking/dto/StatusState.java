package ru.practicum.gateway.booking.dto;

import java.util.Optional;

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

	public static Optional<StatusState> from(String stringState) {
		for (StatusState state : values()) {
			if (state.name().equalsIgnoreCase(stringState)) {
				return Optional.of(state);
			}
		}
		return Optional.empty();
	}
}
