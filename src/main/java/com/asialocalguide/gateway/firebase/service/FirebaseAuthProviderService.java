package com.asialocalguide.gateway.firebase.service;

import com.asialocalguide.gateway.auth.exception.AuthProviderException;
import com.asialocalguide.gateway.auth.service.AuthProviderService;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import java.util.Objects;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * {@link AuthProviderService} implementation backed by the Firebase Admin SDK.
 *
 * <p>Delegates email lookups and user deletion to the Firebase Authentication API.
 */
@Service
@Slf4j
public class FirebaseAuthProviderService implements AuthProviderService {

	private final FirebaseAuth firebaseAuth;

	/**
	 * @param firebaseAuth the Firebase Authentication client bean
	 */
	public FirebaseAuthProviderService(FirebaseAuth firebaseAuth) {
		this.firebaseAuth = firebaseAuth;
	}

	/**
	 * {@inheritDoc}
	 *
	 * <p>Returns {@code false} (rather than throwing) when Firebase reports no user for the email,
	 * as this is an expected outcome for new registrations.
	 *
	 * @throws NullPointerException if {@code email} is {@code null}
	 */
	@Override
	public boolean checkExistingEmail(String email) {
		Objects.requireNonNull(email);

		try {
			return firebaseAuth.getUserByEmail(email) != null;

		} catch (FirebaseAuthException ex) {
			log.info("No user found for email : {}", email);
			return false;
		}
	}

	/**
	 * {@inheritDoc}
	 *
	 * @throws AuthProviderException if the Firebase deletion call fails
	 * @throws NullPointerException  if {@code uid} is {@code null}
	 */
	@Override
	public void deleteProviderUser(String uid) {
		Objects.requireNonNull(uid);

		try {
			firebaseAuth.deleteUser(uid);
		} catch (FirebaseAuthException ex) {
			throw new AuthProviderException(String.format("Error deleting Firebase user with uid: %s", uid), ex);
		}
	}
}
