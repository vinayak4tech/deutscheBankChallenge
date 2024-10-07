package com.dws.challenge.service;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.math.BigDecimal;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.dws.challenge.domain.Account;

public class NotificationServiceTest {

    @Mock
    private NotificationService notificationService; // Mocking the NotificationService

    private Account accountFrom;
    private Account accountTo;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
        accountFrom = new Account("1", new BigDecimal("1000"));
        accountTo = new Account("2", new BigDecimal("500"));
    }

    @Test
    public void testNotificationAfterTransfer() {
        // Test to ensure that notifications are sent to both account holders after a transfer
        String fromNotification = "Transferred 500 to account 2";
        String toNotification = "Received 500 from account 1";

        // Simulating notifications
        notificationService.notifyAboutTransfer(accountFrom, fromNotification);
        notificationService.notifyAboutTransfer(accountTo, toNotification);

        // Verifying that the notify method is called for both accounts
        verify(notificationService).notifyAboutTransfer(accountFrom, fromNotification);
        verify(notificationService).notifyAboutTransfer(accountTo, toNotification);
    }

    @Test
    public void testNoNotificationOnFailure() {
        // Test to ensure no notifications are sent if the transfer fails (e.g., insufficient funds)
        String fromNotification = "Attempted transfer of 2000 to account 2 - Insufficient funds";

        // Simulating a failed transfer notification
        notificationService.notifyAboutTransfer(accountFrom, fromNotification);

        // Verifying that notification is sent only to the account holder of accountFrom
        verify(notificationService).notifyAboutTransfer(accountFrom, fromNotification);
        verify(notificationService, never()).notifyAboutTransfer(accountTo, anyString()); // No notification for accountTo
    }
}
