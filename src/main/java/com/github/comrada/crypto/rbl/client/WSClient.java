package com.github.comrada.crypto.rbl.client;

import static java.util.Objects.requireNonNull;

import java.net.URI;
import java.util.function.Consumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.reactive.socket.CloseStatus;
import org.springframework.web.reactive.socket.WebSocketMessage;
import org.springframework.web.reactive.socket.client.ReactorNettyWebSocketClient;
import reactor.core.publisher.ConnectableFlux;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;
import reactor.netty.http.client.WebsocketClientSpec;

public class WSClient implements AutoCloseable {

  private static final Logger LOGGER = LoggerFactory.getLogger(WSClient.class);
  private final ReactorNettyWebSocketClient client;
  private final Object lock = new Object();

  public WSClient(String url, Consumer<String> messageListener, String request) {
    client = new ReactorNettyWebSocketClient(HttpClient.create(),
        () -> WebsocketClientSpec.builder().maxFramePayloadLength(2097152));
    ConnectableFlux<String> connectableFlux =
        Flux.<String>create(emitter -> client
                .execute(URI.create(url), session -> {
                  WebSocketMessage initialMessage = session.textMessage(request);
                  Flux<String> flux = session.send(Mono.just(initialMessage))
                      .thenMany(session.receive())
                      .map(WebSocketMessage::getPayloadAsText)
                      .doOnNext(emitter::next);

                  Flux<String> sessionStatus = session.closeStatus()
                      .switchIfEmpty(Mono.just(CloseStatus.GOING_AWAY))
                      .map(CloseStatus::toString)
                      .doOnNext(emitter::next)
                      .flatMapMany(Flux::just);

                  return flux
                      .mergeWith(sessionStatus)
                      .then();
                })
                .subscribe())
            .publish();
    connectableFlux.subscribe(messageListener);
    connectableFlux.connect();
    lock();
    /*client.execute(URI.create(url), session -> session.send(Mono.just(session.textMessage(request)))
            .thenMany(
                session.receive()
                    .map(WebSocketMessage::getPayloadAsText)
                    .flatMap(d -> {
                      messageListener.accept(d);
                      return Mono.just(d);
                    })
            )
            .then())
        .block();*/
  }

  private void lock() {
    new Thread(() -> {
      try {
        synchronized (lock) {
          lock.wait();
        }
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
        LOGGER.warn("Locking Thread Interrupted");
      }
    }).start();
  }

  @Override
  public void close() {
    LOGGER.info("Shutdown the listener");
    synchronized (lock) {
      lock.notify();
    }
  }
}
