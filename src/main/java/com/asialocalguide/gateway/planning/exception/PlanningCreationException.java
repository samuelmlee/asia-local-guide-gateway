package com.asialocalguide.gateway.planning.exception;

public class PlanningCreationException extends RuntimeException {
	public PlanningCreationException(String s) {
		super(s);
	}

	public PlanningCreationException(String s, Exception e) {
		super(s, e);
	}
}
