package com.asialocalguide.gateway.auth.service;

/**
 * Abstraction over an external authentication provider (e.g. Firebase Auth).
 *
 * <p>Implementations are responsible for provider-specific communication, including
 * email lookups and user account deletion.
 */
public interface AuthProviderService {

	/**
	 * Returns {@code true} if the given email address is already registered with the provider.
	 *
	 * @param email the email address to check
	 * @return {@code true} if a user with this email exists; {@code false} otherwise
	 */
	boolean checkExistingEmail(String email);

	/**
	 * Deletes the provider-side user account identified by the given UID.
	 *
	 * @param uid the provider-assigned user identifier to delete
	 */
	void deleteProviderUser(String uid);
}
