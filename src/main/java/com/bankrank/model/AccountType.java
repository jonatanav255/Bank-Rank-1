package com.bankrank.model;

import java.math.BigDecimal;

public interface AccountType {

    BigDecimal getMiniumBalance();

    boolean canWithdraw(BigDecimal currentBalance, BigDecimal withDrawAmount);

    BigDecimal getInterestRate();
}
