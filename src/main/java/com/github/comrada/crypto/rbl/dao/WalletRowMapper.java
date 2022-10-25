package com.github.comrada.crypto.rbl.dao;

import static java.util.Objects.requireNonNullElse;

import com.github.comrada.crypto.rbl.domain.Wallet;
import com.github.comrada.crypto.rbl.domain.WalletStatus;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.springframework.jdbc.core.RowMapper;

public class WalletRowMapper implements RowMapper<Wallet> {

  @Override
  public Wallet mapRow(ResultSet rs, int rowNum) throws SQLException {
    return new Wallet(
        rs.getString("blockchain"),
        rs.getString("address"),
        rs.getString("asset"),
        rs.getBigDecimal("balance"),
        rs.getBoolean("exchange"),
        WalletStatus.valueOf(requireNonNullElse(rs.getString("status"), "OK")),
        rs.getBoolean("token"),
        rs.getString("contract")
    );
  }
}
