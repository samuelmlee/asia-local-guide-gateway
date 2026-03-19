package com.asialocalguide.gateway.planning.exception;

/**
 * Thrown when building or persisting a {@link com.asialocalguide.gateway.planning.domain.Planning} fails.
 */
public class PlanningCreationException extends RuntimeException {

	/**
	 * @param s description of the creation failure
	 */
	public PlanningCreationException(String s) {
		super(s);
	}

	/**
	 * @param s description of the creation failure
	 * @param e the underlying exception
	 */
	public PlanningCreationException(String s, Exception e) {
		super(s, e);
	}
}
