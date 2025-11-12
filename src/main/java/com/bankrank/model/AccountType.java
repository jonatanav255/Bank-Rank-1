package com.bankrank.model;

import java.math.BigDecimal;

public interface AccountType {

    BigDecimal getMinimumBalance();

    boolean canWithdraw(BigDecimal currentBalance, BigDecimal withDrawAmount);

    BigDecimal getInterestRate();
}
