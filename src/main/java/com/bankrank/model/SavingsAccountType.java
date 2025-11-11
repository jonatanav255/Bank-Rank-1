package com.bankrank.model;

public class SavingsAccountType implements AccountType {

    @Override
    public double getMiniumBalance() {
        return 100;
    }

    @Override
    public boolean canWithdraw(double currentBalance, double withDrawAmount) {
        return (currentBalance - withDrawAmount) >= getMiniumBalance();
    }
}
