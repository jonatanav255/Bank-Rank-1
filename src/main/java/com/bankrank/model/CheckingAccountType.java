package com.bankrank.model;

public class CheckingAccountType implements AccountType {

    @Override
    public double getMiniumBalance() {
        return 0;
    }

    @Override
    public boolean canWithdraw(double currentBalance, double withDrawAmount) {
        return (currentBalance - withDrawAmount) >= getMiniumBalance();
    }

    @Override
    public double getInterestRate() {
        return 0.0;
    }
}
