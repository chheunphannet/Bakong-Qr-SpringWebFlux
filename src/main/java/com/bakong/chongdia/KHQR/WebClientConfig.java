package com.bakong.chongdia.KHQR;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import java.time.Duration;

import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import reactor.netty.http.client.HttpClient;
import reactor.netty.resources.ConnectionProvider;

@Configuration
public class WebClientConfig {

    @Bean
    WebClient webClient() {
        // 1. Create ConnectionProvider for pooling (replaces .maxConnections)
        ConnectionProvider provider = ConnectionProvider.builder("custom-provider")
                .maxConnections(100)
                .maxIdleTime(Duration.ofSeconds(20))
                .maxLifeTime(Duration.ofSeconds(60))
                .pendingAcquireTimeout(Duration.ofSeconds(60))
                .evictInBackground(Duration.ofSeconds(120))
                .build();

        // 2. Create HttpClient
        HttpClient httpClient = HttpClient.create(provider)
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 10_000)
                .responseTimeout(Duration.ofSeconds(5))
                .doOnConnected(conn -> 
                        conn.addHandlerLast(new ReadTimeoutHandler(5))
                            .addHandlerLast(new WriteTimeoutHandler(5))
                )
                .compress(true)
                .wiretap(true) // Logs detailed network info (good for debug)
                .keepAlive(true);

        return WebClient.builder()
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .build();
    
    }
}

