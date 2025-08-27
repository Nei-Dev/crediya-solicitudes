package com.crediya.r2dbc.mappers;

import com.crediya.model.creditapplication.StateCreditApplication;

import java.util.Map;

public class StateCreditApplicationMapper {
	
	private final static Map<StateCreditApplication, String> MAP_STATE = Map.of(
		StateCreditApplication.PENDING, "PENDIENTE",
		StateCreditApplication.APPROVED, "APROBADO",
		StateCreditApplication.REJECTED, "RECHAZADO"
	);
	
	public static String toDatabase(StateCreditApplication state) {
		return MAP_STATE.getOrDefault(state, state.name());
	}
}
