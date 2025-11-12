package com.bankrank.model;

import java.math.BigDecimal;

public class CheckingAccountType implements AccountType {

    @Override
    public BigDecimal getMinimumBalance() {
        return BigDecimal.ZERO;
    }

    @Override
    public boolean canWithdraw(BigDecimal currentBalance, BigDecimal withDrawAmount) {
        return currentBalance.subtract(withDrawAmount).compareTo(getMinimumBalance()) >= 0;
    }

    @Override
    public BigDecimal getInterestRate() {
        return BigDecimal.ZERO;
    }
}
