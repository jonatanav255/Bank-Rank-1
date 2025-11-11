package com.bankrank.model;

public class CheckingAccountType implements AccountType {

    @Override
    public double getMiniumBalance() {
        return 100;
    }

    @Override
    public boolean canWithdraw(double currentBalance, double withDrawAmount) {
        return (currentBalance - withDrawAmount) >= getMiniumBalance();
    }
}
