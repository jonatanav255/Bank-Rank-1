package com.bankrank.model;

import java.math.BigDecimal;

public class SavingsAccountType implements AccountType {

    @Override
    public BigDecimal getMiniumBalance() {
        return new BigDecimal("100.00");
    }

    @Override
    public boolean canWithdraw(BigDecimal currentBalance, BigDecimal withDrawAmount) {
        return currentBalance.subtract(withDrawAmount).compareTo(getMiniumBalance()) >= 0;
    }

    @Override
    public BigDecimal getInterestRate() {
        return new BigDecimal("0.025");
    }
}
