package com.dws.challenge.domain;

import java.math.BigDecimal;

import com.dws.challenge.exception.InsufficientBalanceException;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class Account {

  @NotNull
  @NotEmpty
  private final String accountId;

  @NotNull
  @Min(value = 0, message = "Initial balance must be positive.")
  private BigDecimal balance;

  public Account(String accountId) {
    this.accountId = accountId;
    this.balance = BigDecimal.ZERO;
  }

  @JsonCreator
  public Account(@JsonProperty("accountId") String accountId,
    @JsonProperty("balance") BigDecimal balance) {
    this.accountId = accountId;
    this.balance = balance;
  }
  
  public synchronized void withdraw(BigDecimal amount) throws IllegalArgumentException {
	    if (amount.compareTo(BigDecimal.ZERO) <= 0) {
	        throw new IllegalArgumentException("Withdrawal amount must be positive.");
	    }
	    if (this.balance.compareTo(amount) < 0) {
	        throw new InsufficientBalanceException("Insufficient funds for withdrawal.");
	    }
	    this.balance = this.balance.subtract(amount);
	}

	public synchronized void deposit(BigDecimal amount) {
	    if (amount.compareTo(BigDecimal.ZERO) <= 0) {
	        throw new IllegalArgumentException("Deposit amount must be positive.");
	    }
	    this.balance = this.balance.add(amount);
	}
  
}
