package com.github.comrada.crypto.rbl.dao;

import com.github.comrada.crypto.rbl.domain.Wallet;
import java.time.Instant;
import java.util.Collection;
import java.util.Map;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

public class WalletDao {

  private final NamedParameterJdbcTemplate jdbcTemplate;
  private final WalletRowMapper rowMapper;

  public WalletDao(NamedParameterJdbcTemplate jdbcTemplate) {
    this.jdbcTemplate = jdbcTemplate;
    this.rowMapper = new WalletRowMapper();
  }

  public Collection<Wallet> getExchangeWallets() {
    return jdbcTemplate.query("""
            SELECT * FROM wallets w
            WHERE w.exchange = true AND w.status = 'OK' AND w.blockchain = 'Ripple' AND w.asset = 'XRP'
        """, Map.of(), rowMapper);
  }

  public void update(Wallet wallet) {
    jdbcTemplate.update("""
        UPDATE wallets
        SET balance = :balance, checked_at = :checkedAt
        WHERE address = :address AND locked = false
        """, Map.of(
        "balance", wallet.balance(),
        "checkedAt", Instant.now(),
        "address", wallet.address()
    ));
  }
}
