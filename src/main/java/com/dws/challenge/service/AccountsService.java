package com.dws.challenge.service;

import java.math.BigDecimal;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.dws.challenge.domain.Account;
import com.dws.challenge.exception.AccountNotFoundException;
import com.dws.challenge.exception.InsufficientBalanceException;
import com.dws.challenge.repository.AccountsRepository;

import jakarta.validation.Valid;
import lombok.Getter;

@Service
public class AccountsService {

	@Getter
	@Autowired
	private final AccountsRepository accountsRepository;

	@Getter
	@Autowired
	private final NotificationService notificationService;

	public AccountsService(AccountsRepository accountsRepository, NotificationService notificationService) {
		this.accountsRepository = accountsRepository;
		this.notificationService = notificationService;
	}

	public synchronized void transferMoney(String accountFromId, String accountToId, BigDecimal amount) {
		if (amount.compareTo(BigDecimal.ZERO) <= 0) {
			throw new IllegalArgumentException("Transfer amount must be positive.");
		}

		Account accountFrom = accountsRepository.getAccount(accountFromId);
		Account accountTo = accountsRepository.getAccount(accountToId);

		if (accountFrom == null || accountTo == null)
			throw new AccountNotFoundException("Account not found.");
		if (accountFrom.getBalance().compareTo(amount) < 0)
			throw new InsufficientBalanceException("Insufficient funds in account 101");

		// Lock both accounts to ensure thread safety and avoid deadlock
		synchronized (accountFrom) {
			synchronized (accountTo) {
				// Withdraw from accountFrom and deposit to accountTo
				accountFrom.withdraw(amount);
				accountTo.deposit(amount);
			}
		}

		// Send notifications
		notificationService.notifyAboutTransfer(accountFrom,
				"Transferred "+amount +" to "+accountTo.getAccountId());
		notificationService.notifyAboutTransfer(accountTo,
				"Received " +amount+" from "+ accountFrom.getAccountId());
//        }
	}

	public Account getAccount(String accountId) {
		return accountsRepository.getAccount(accountId);
	}

	public void createAccount(@Valid Account account) {
		accountsRepository.createAccount(account);
	}

}
