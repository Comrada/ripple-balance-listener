package com.github.comrada.crypto.rbl.client;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.xrpl.xrpl4j.model.transactions.ImmutablePayment;

@JsonIgnoreProperties(ignoreUnknown = true)
public record TransactionResponse(
    @JsonProperty
    ImmutablePayment transaction
) {}
