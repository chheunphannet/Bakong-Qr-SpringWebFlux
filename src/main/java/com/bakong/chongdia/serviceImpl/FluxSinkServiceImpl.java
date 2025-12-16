package com.bakong.chongdia.serviceImpl;


import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Service;

import com.bakong.chongdia.dto.PaidUserResponse;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;
import reactor.core.publisher.Sinks.Many;

@Service
@Slf4j
public class FluxSinkServiceImpl {
    
    private final Map<String, Many<PaidUserResponse>> activeSinks = new ConcurrentHashMap<>();
    
    public Flux<PaidUserResponse> createFlux(String clientId) {
        // Remove old sink if exists
        removeSink(clientId);
        
        Many<PaidUserResponse> sink = Sinks.many()
                .multicast()
                .onBackpressureBuffer(100); // Adjust buffer size as needed
        
        activeSinks.put(clientId, sink);
        log.info("Created new SSE sink for client: {}", clientId);
        
        return sink.asFlux()
                .doOnCancel(() -> {
                    log.info("Client {} cancelled subscription", clientId);
                    removeSink(clientId);
                })
                .doOnError(ex -> {
                    if (ex instanceof IOException) {
                        log.warn("Client {} disconnected: {}", clientId, ex.getMessage());
                    } else {
                        log.error("Error for client {}: ", clientId, ex);
                    }
                    removeSink(clientId);
                })
                .doFinally(signal -> {
                    log.info("Stream completed for client {} with signal: {}", clientId, signal);
                    removeSink(clientId);
                });
    }
    
    /**
     * Publish to all active clients
     */
    public void publishToAll(PaidUserResponse response) {
        log.debug("Publishing to {} active clients", activeSinks.size());
        
        activeSinks.forEach((clientId, sink) -> {
            try {
                Sinks.EmitResult result = sink.tryEmitNext(response);
                
                if (result.isFailure()) {
                    log.warn("Failed to emit to client {}: {}", clientId, result);
                    if (result == Sinks.EmitResult.FAIL_TERMINATED || 
                        result == Sinks.EmitResult.FAIL_CANCELLED) {
                        removeSink(clientId);
                    }
                }
            } catch (Exception ex) {
                log.error("Error publishing to client {}: ", clientId, ex);
                removeSink(clientId);
            }
        });
    }
    
    /**
     * Publish to specific client
     */
    public void publishToClient(String clientId, PaidUserResponse response) {
        Many<PaidUserResponse> sink = activeSinks.get(clientId);
        
        if (sink != null) {
            try {
                Sinks.EmitResult result = sink.tryEmitNext(response);
                
                if (result.isFailure()) {
                    log.warn("Failed to emit to client {}: {}", clientId, result);
                    if (result == Sinks.EmitResult.FAIL_TERMINATED || 
                        result == Sinks.EmitResult.FAIL_CANCELLED) {
                        removeSink(clientId);
                    }
                }
            } catch (Exception ex) {
                log.error("Error publishing to client {}: ", clientId, ex);
                removeSink(clientId);
            }
        } else {
            log.warn("Attempted to publish to non-existent client: {}", clientId);
        }
    }
    
    /**
     * Remove sink and clean up resources
     */
    private void removeSink(String clientId) {
        Many<PaidUserResponse> sink = activeSinks.remove(clientId);
        if (sink != null) {
            try {
                sink.tryEmitComplete();
            } catch (Exception ex) {
                log.debug("Error completing sink for {}: {}", clientId, ex.getMessage());
            }
            log.info("Removed sink for client: {}", clientId);
        }
    }
    
    /**
     * Get count of active connections
     */
    public int getActiveConnectionCount() {
        return activeSinks.size();
    }
    
    /**
     * Close all connections (useful for shutdown)
     */
    public void closeAll() {
        log.info("Closing all {} active SSE connections", activeSinks.size());
        activeSinks.keySet().forEach(this::removeSink);
    }
}
