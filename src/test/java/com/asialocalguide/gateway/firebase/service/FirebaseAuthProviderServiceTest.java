package com.asialocalguide.gateway.firebase.service;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

import com.asialocalguide.gateway.core.exception.AuthProviderException;
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

@ExtendWith(MockitoExtension.class)
class FirebaseAuthProviderServiceTest {

  @Mock FirebaseAuth firebaseAuth;

  @InjectMocks FirebaseAuthProviderService service;

  @Test
  void existingEmailReturnsTrueWhenUserRecordIsFound() throws Exception {
    UserRecord mockRecord = mock(UserRecord.class);
    when(firebaseAuth.getUserByEmail("valid@example.com")).thenReturn(mockRecord);
    boolean result = service.checkExistingEmail("valid@example.com");
    assertTrue(result);
  }

  @Test
  void existingEmailReturnsFalseWhenNoUserRecordIsFound() throws Exception {
    when(firebaseAuth.getUserByEmail("unknown@example.com")).thenReturn(null);
    boolean result = service.checkExistingEmail("unknown@example.com");
    assertFalse(result);
  }

  @Test
  void existingEmailReturnsFalseWhenExceptionIsThrown() throws Exception {

    FirebaseAuthException exception =
        new FirebaseAuthException(
            ErrorCode.INTERNAL,
            "Sample error message",
            new Exception("Cause of failure"),
            null,
            AuthErrorCode.INVALID_ID_TOKEN);

    when(firebaseAuth.getUserByEmail("throws@example.com")).thenThrow(exception);
    boolean result = service.checkExistingEmail("throws@example.com");
    assertFalse(result);
  }

  @Test
  void existingEmailHandlesNullEmail() {
    assertThatThrownBy(() -> service.checkExistingEmail(null)).isInstanceOf(NullPointerException.class);
  }

  @Test
  void deleteUser_shouldCallFirebaseDeleteUser() throws Exception {
    // Arrange
    String testUid = "test-user-id";

    // Act
    service.deleteUser(testUid);

    // Assert
    verify(firebaseAuth, times(1)).deleteUser(testUid);
  }

  @Test
  void deleteUser_shouldHandleFirebaseAuthException() throws Exception {
    // Arrange
    String testUid = "error-user-id";
    FirebaseAuthException exception =
        new FirebaseAuthException(
            ErrorCode.INTERNAL,
            "Sample error message",
            new Exception("Cause of failure"),
            null,
            AuthErrorCode.USER_NOT_FOUND);
    doThrow(exception).when(firebaseAuth).deleteUser(testUid);

    // Act & Assert - should not throw exception
    assertThatThrownBy(() -> service.deleteUser(testUid)).isInstanceOf(AuthProviderException.class);

    verify(firebaseAuth).deleteUser(testUid);
  }

  @Test
  void deleteUser_shouldThrowNullPointerException_whenUidIsNull() {
    // Act & Assert
    assertThatThrownBy(() -> service.deleteUser(null)).isInstanceOf(NullPointerException.class);

    verifyNoInteractions(firebaseAuth);
  }
}
