package com.crediya.r2dbc.mappers;

import com.crediya.model.creditapplication.StateCreditApplication;
import com.crediya.r2dbc.exceptions.StateNotFoundException;

import java.util.Map;

import static com.crediya.r2dbc.constants.ErrorMessage.STATE_NOT_FOUND;

public class StateCreditApplicationMapper {
	
	private StateCreditApplicationMapper() {}
	
	private static final Map<StateCreditApplication, String> MAP_STATE = Map.of(
		StateCreditApplication.PENDING, "PENDIENTE",
		StateCreditApplication.APPROVED, "APROBADO",
		StateCreditApplication.REJECTED, "RECHAZADO"
	);
	
	public static String toDatabase(StateCreditApplication state) {
		return MAP_STATE.getOrDefault(state, state.name());
	}
	
	public static StateCreditApplication fromDatabase(String state) {
		return MAP_STATE.entrySet()
			.stream()
			.filter(entry -> entry.getValue().equalsIgnoreCase(state))
			.map(Map.Entry::getKey)
			.findFirst()
			.orElseThrow(() -> new StateNotFoundException(STATE_NOT_FOUND));
	}
}
