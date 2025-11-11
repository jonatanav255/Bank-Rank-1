package com.bankrank.model;

public interface AccountType {
    // Savings and Checking

    double getMiniumBalance();

    boolean canWithdraw(double currentBalance, double withDrawAmount);

    // "What's your minimum balance?"
    // "Can I withdraw this amount given the current balance?"
}
