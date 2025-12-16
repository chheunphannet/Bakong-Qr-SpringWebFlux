package com.bakong.chongdia.serviceImpl;

import java.io.IOException;

import org.springframework.stereotype.Service;

import com.bakong.chongdia.dto.PaidUserResponse;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.FluxSink;

@Service
@Slf4j
public class FluxSinkService {

    private final Flux<PaidUserResponse> flux;
    private FluxSink<PaidUserResponse> sink;

    public FluxSinkService() {
        this.flux = Flux.<PaidUserResponse>create(emitter -> {
                    this.sink = emitter;
                }, FluxSink.OverflowStrategy.BUFFER)
                .publish()
                .autoConnect()
                .doOnError(ex -> {
                    if (ex instanceof IOException) {
                        log.warn("Client disconnected.");
                    } else {
                        log.error("Unexpected error: ", ex);
                    }
                });
    }

    public Flux<PaidUserResponse> getFlux() {
        return flux;
    }

    public void publish(PaidUserResponse response) {
        if (sink != null) {
            try {
                sink.next(response);
            } catch (Exception ex) {  
                log.error("Error while publishing to sink:", ex);
            }
        }
    }
}
