package com.bankrank;

import java.util.UUID;

import com.bankrank.model.Account;
import com.bankrank.model.SavingsAccountType;

public class Main {

    public static void main(String[] args) {
        System.out.println("Welcome to Bank Rank!");
        UUID id = UUID.randomUUID();
        SavingsAccountType Savings = new SavingsAccountType();
        // Account account1 = new Account(id, "CustomerName", 500);
        // Account account1 = new Account(id, "CustomerName", 500);
        Account account1 = new Account(id, "CustomerName", 500.0, Savings);
    }
}
