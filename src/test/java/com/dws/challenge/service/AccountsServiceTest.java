
package com.dws.challenge.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.dws.challenge.domain.Account;
import com.dws.challenge.exception.AccountNotFoundException;
import com.dws.challenge.exception.InsufficientBalanceException;
import com.dws.challenge.repository.AccountsRepository;

public class AccountsServiceTest {

    @Mock
    private AccountsRepository accountsRepository;

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private AccountsService accountsService;

    private Account accountFrom;
    private Account accountTo;
    private Map<String, Account> accountsMap;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
        accountFrom = new Account("101", new BigDecimal("1000"));
        accountTo = new Account("102", new BigDecimal("500"));
        
        accountsMap = new HashMap<>();
        accountsMap.put(accountFrom.getAccountId(), accountFrom);
        accountsMap.put(accountTo.getAccountId(), accountTo);
    }

    @Test
    public void testSuccessfulTransfer() {
        BigDecimal transferAmount = new BigDecimal("200");

        when(accountsRepository.getAccount("101")).thenReturn(accountFrom);
        when(accountsRepository.getAccount("102")).thenReturn(accountTo);
        accountsService.transferMoney(accountFrom.getAccountId(), accountTo.getAccountId(), transferAmount);

        assertEquals(new BigDecimal("800"), accountFrom.getBalance());
        assertEquals(new BigDecimal("700"), accountTo.getBalance());

        verify(notificationService).notifyAboutTransfer(accountFrom, "Transferred 200 to 102");
        verify(notificationService).notifyAboutTransfer(accountTo, "Received 200 from 101");
    }

    @Test
    public void testTransferWithInsufficientBalance() {
        BigDecimal transferAmount = new BigDecimal("2000");
        when(accountsRepository.getAccount("101")).thenReturn(accountFrom);
        when(accountsRepository.getAccount("102")).thenReturn(accountTo);
        Exception exception = assertThrows(InsufficientBalanceException.class, () -> {
            accountsService.transferMoney(accountFrom.getAccountId(), accountTo.getAccountId(), transferAmount);
        });

        assertEquals("Insufficient funds in account 101", exception.getMessage());

        verify(notificationService, never()).notifyAboutTransfer(any(), any());
    }

    @Test
    public void testTransferWithNegativeAmount() {
        BigDecimal transferAmount = new BigDecimal("-100");

        when(accountsRepository.getAccount("101")).thenReturn(accountFrom);
        when(accountsRepository.getAccount("102")).thenReturn(accountTo);
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            accountsService.transferMoney(accountFrom.getAccountId(), accountTo.getAccountId(), transferAmount);
        });

        assertEquals("Transfer amount must be positive.", exception.getMessage());

        verify(notificationService, never()).notifyAboutTransfer(any(), any());
    }

    @Test
    public void testTransferWithNonExistentAccount() {
        when(accountsRepository.getAccount("103")).thenReturn(null);

        when(accountsRepository.getAccount("101")).thenReturn(accountFrom);
        when(accountsRepository.getAccount("102")).thenReturn(accountTo);
        
        Exception exception = assertThrows(AccountNotFoundException.class, () -> {
            accountsService.transferMoney("103", accountTo.getAccountId(), new BigDecimal("100"));
        });

        assertEquals("Account not found.", exception.getMessage());

        verify(notificationService, never()).notifyAboutTransfer(any(), any());
    }
}
