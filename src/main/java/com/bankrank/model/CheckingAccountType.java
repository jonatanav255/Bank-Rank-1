package com.bankrank.model;

import java.math.BigDecimal;

public class CheckingAccountType implements AccountType {

    @Override
    public BigDecimal getMiniumBalance() {
        return BigDecimal.ZERO;
    }

    @Override
    public boolean canWithdraw(BigDecimal currentBalance, BigDecimal withDrawAmount) {
        return currentBalance.subtract(withDrawAmount).compareTo(getMiniumBalance()) >= 0;
    }

    @Override
    public BigDecimal getInterestRate() {
        return BigDecimal.ZERO;
    }
}
