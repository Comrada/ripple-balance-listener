package com.github.comrada.crypto.rbl.client;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Set;

public record SubscribeRequest(
    @JsonProperty
    String id,
    @JsonProperty
    String command,
    @JsonProperty
    Set<String> accounts
) {}
