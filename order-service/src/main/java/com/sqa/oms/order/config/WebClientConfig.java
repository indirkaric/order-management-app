package com.sqa.oms.order.config;

import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

import java.time.Duration;

@Configuration
public class WebClientConfig {

    private static final int RESPONSE_TIMEOUT_MINUTES = 5;

    private static final int CONNECT_TIMEOUT_MILLIS = 30_000;

    private static final int READ_TIMEOUT_SECONDS = 300;

    private static final int WRITE_TIMEOUT_SECONDS = 300;

    private static final int MAX_IN_MEMORY_BYTES = 256 * 1024 * 1024;

    /**
     * Creates the shared {@link WebClient} used by the outbound service clients.
     *
     * @return WebClient
     */
    @Bean
    public WebClient webClient() {
        HttpClient httpClient = HttpClient.create()
                .followRedirect(true)
                .responseTimeout(Duration.ofMinutes(RESPONSE_TIMEOUT_MINUTES))
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, CONNECT_TIMEOUT_MILLIS)
                .doOnConnected(connection -> connection
                        .addHandlerLast(new ReadTimeoutHandler(READ_TIMEOUT_SECONDS))
                        .addHandlerLast(new WriteTimeoutHandler(WRITE_TIMEOUT_SECONDS)));

        return WebClient.builder()
                .exchangeStrategies(ExchangeStrategies.builder()
                        .codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(MAX_IN_MEMORY_BYTES))
                        .build())
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .build();
    }
}
