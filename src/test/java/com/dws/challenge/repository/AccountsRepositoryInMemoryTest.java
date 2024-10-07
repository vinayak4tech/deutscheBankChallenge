package com.dws.challenge.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;

import com.dws.challenge.domain.Account;
import com.dws.challenge.exception.DuplicateAccountIdException;

public class AccountsRepositoryInMemoryTest {

	@InjectMocks
    private AccountsRepositoryInMemory accountsRepositoryInMemory;

    @BeforeEach
    public void setUp() {
        // Initialize the in-memory repository
        accountsRepositoryInMemory = new AccountsRepositoryInMemory();
    }

    @Test
    public void testSaveAndFindAccount() {
        // Create and save an account
        Account account = new Account("103", new BigDecimal("1000"));
        accountsRepositoryInMemory.createAccount(account);

        // Retrieve the account and verify its balance
        Optional<Account> foundAccount = Optional.of(accountsRepositoryInMemory.getAccount("103"));
        assertTrue(foundAccount.isPresent());
        assertEquals(new BigDecimal("1000"), foundAccount.get().getBalance());
    }

    @Test
    public void testFindAccount_NotFound() {
        // Try finding a non-existent account
        Optional<Account> foundAccount = Optional.ofNullable(accountsRepositoryInMemory.getAccount("99"));
        assertFalse(foundAccount.isPresent());
    }

    @Test
    public void testUpdateAccountBalance() {
    	accountsRepositoryInMemory.clearAccounts();
        // Create and save an account
        Account account = new Account("105", new BigDecimal("500"));
        accountsRepositoryInMemory.createAccount(account);

        // Update the account balance
        account.deposit(new BigDecimal("200"));
        accountsRepositoryInMemory.createAccount(account);

        // Retrieve the updated account and verify the balance
        Account updatedAccount = accountsRepositoryInMemory.getAccount("102");
        assertEquals(new BigDecimal("700"), updatedAccount.getBalance());
    }

    @Test
    public void testDuplicateAccountIdException() {
        // Create and save an account with the same ID twice
        Account account1 = new Account("101", new BigDecimal("1000"));
        Account account2 = new Account("101", new BigDecimal("500"));

        accountsRepositoryInMemory.createAccount(account1);
        
        Exception exception = assertThrows(DuplicateAccountIdException.class, () -> {
            accountsRepositoryInMemory.createAccount(account2);
        });

        String expectedMessage = "Account id " + account2.getAccountId() + " already exists!";
        String actualMessage = exception.getMessage();

        assertTrue(actualMessage.contains(expectedMessage));
    }
    
    public void tearDown() throws Exception {
    	accountsRepositoryInMemory = null;	
    }
}

