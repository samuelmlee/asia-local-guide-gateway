package com.asialocalguide.gateway.firebase.service;


import com.asialocalguide.gateway.core.domain.auth.AuthProviderService;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
@Slf4j
public class FirebaseAuthProviderService implements AuthProviderService {

    private final FirebaseAuth firebaseAuth;

    public FirebaseAuthProviderService(FirebaseAuth firebaseAuth) {
        this.firebaseAuth = firebaseAuth;
    }

    public boolean checkExistingEmail(String email) {
        Objects.requireNonNull(email);

        try {
            return firebaseAuth.getUserByEmail(email) != null;

        } catch (FirebaseAuthException ex) {
            log.info("No user found for email : {}", email);
            return false;
        }
    }
}
