package com.asialocalguide.gateway.auth.service;

public interface AuthProviderService {

	boolean checkExistingEmail(String email);

	void deleteProviderUser(String uid);
}
