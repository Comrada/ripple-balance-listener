package com.github.comrada.crypto.rbl.domain;

import java.math.BigDecimal;

public record Wallet(
    String blockchain,
    String address,
    String asset,
    BigDecimal balance,
    boolean exchange,
    WalletStatus status,
    boolean token,
    String contract
) {
}
