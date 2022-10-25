package com.github.comrada.crypto.rbl.config;

import static java.util.stream.Collectors.toUnmodifiableSet;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.comrada.crypto.rbl.client.SubscribeRequest;
import com.github.comrada.crypto.rbl.client.WSClient;
import com.github.comrada.crypto.rbl.dao.WalletDao;
import com.github.comrada.crypto.rbl.client.TransactionResponse;
import com.github.comrada.crypto.rbl.domain.Wallet;
import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.InvalidJsonException;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.PathNotFoundException;
import java.util.Set;
import java.util.function.Consumer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.xrpl.xrpl4j.model.jackson.ObjectMapperFactory;
import org.xrpl.xrpl4j.model.transactions.ImmutablePayment;
import org.xrpl.xrpl4j.model.transactions.TransactionType;

@Configuration
public class RippleWSClientConfig {

  @Bean
  WalletDao walletDao(NamedParameterJdbcTemplate jdbcTemplate) {
    return new WalletDao(jdbcTemplate);
  }

  @Bean
  ObjectMapper objectMapper() {
    return ObjectMapperFactory.create();
  }

  @Bean
  WSClient rippleWSClient(WalletDao dao, ObjectMapper objectMapper, Consumer<String> messageListener)
      throws JsonProcessingException {
    Set<String> exchangeAddresses = dao.getExchangeWallets().stream()
        .map(Wallet::address)
        .collect(toUnmodifiableSet());
    SubscribeRequest subscribeRequest = new SubscribeRequest("XRP exchanges", "subscribe", exchangeAddresses);
    String request = objectMapper.writeValueAsString(subscribeRequest);
    return new WSClient("wss://xrplcluster.com/", messageListener, request);
  }

  @Bean
  Consumer<String> messageListener(ObjectMapper objectMapper) {
    return message -> {
      try {
        if (message.contains("\"transaction\"")) {
          DocumentContext jsonContext = JsonPath.parse(message);
          String txType = jsonContext.read("$.transaction.TransactionType");
          if (txType != null && txType.equals("Payment")) {
            TransactionResponse response = objectMapper.readValue(message, TransactionResponse.class);
            ImmutablePayment transaction = response.transaction();
            if (transaction != null && transaction.transactionType() == TransactionType.PAYMENT) {
              System.out.printf("from: %s, to: %s, amount: %s%n", transaction.account(), transaction.destination(),
                  transaction.amount());
            }
          }
        }
      } catch (JsonProcessingException e) {
        System.err.println(e.getMessage());
        System.out.println(message);
      } catch (InvalidJsonException | PathNotFoundException e) {
        System.out.println(message);
        System.err.println(e.getMessage());
      }
    };
  }
}
