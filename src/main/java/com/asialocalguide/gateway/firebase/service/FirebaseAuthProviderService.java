package com.asialocalguide.gateway.firebase.service;

import com.asialocalguide.gateway.core.exception.AuthProviderException;
import com.asialocalguide.gateway.core.service.auth.AuthProviderService;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import java.util.Objects;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class FirebaseAuthProviderService implements AuthProviderService {

  private final FirebaseAuth firebaseAuth;

  public FirebaseAuthProviderService(FirebaseAuth firebaseAuth) {
    this.firebaseAuth = firebaseAuth;
  }

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
