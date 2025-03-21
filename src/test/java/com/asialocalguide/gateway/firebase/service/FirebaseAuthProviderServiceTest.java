package com.asialocalguide.gateway.firebase.service;

import com.google.firebase.ErrorCode;
import com.google.firebase.auth.AuthErrorCode;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.UserRecord;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FirebaseAuthProviderServiceTest {

    @Mock
    FirebaseAuth firebaseAuth;

    @InjectMocks
    FirebaseAuthProviderService service;

    @Test
    void existingEmailReturnsTrueWhenUserRecordIsFound() throws Exception {
        UserRecord mockRecord = mock(UserRecord.class);
        when(firebaseAuth.getUserByEmail("valid@example.com")).thenReturn(mockRecord);
        boolean result = service.isExistingEmail("valid@example.com");
        assertTrue(result);
    }

    @Test
    void existingEmailReturnsFalseWhenNoUserRecordIsFound() throws Exception {
        when(firebaseAuth.getUserByEmail("unknown@example.com")).thenReturn(null);
        boolean result = service.isExistingEmail("unknown@example.com");
        assertFalse(result);
    }

    @Test
    void existingEmailReturnsFalseWhenExceptionIsThrown() throws Exception {

        FirebaseAuthException exception = new FirebaseAuthException(
                ErrorCode.INTERNAL,
                "Sample error message",
                new Exception("Cause of failure"),
                null,
                AuthErrorCode.INVALID_ID_TOKEN
        );

        when(firebaseAuth.getUserByEmail("throws@example.com"))
                .thenThrow(exception);
        boolean result = service.isExistingEmail("throws@example.com");
        assertFalse(result);
    }

    @Test
    void existingEmailHandlesNullEmail() {
        assertThatThrownBy(() -> service.isExistingEmail(null)).isInstanceOf(NullPointerException.class);
    }

}