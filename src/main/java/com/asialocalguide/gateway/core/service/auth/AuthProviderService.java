package com.asialocalguide.gateway.core.service.auth;

public interface AuthProviderService {

  boolean checkExistingEmail(String email);

  void deleteUser(String uid);
}
