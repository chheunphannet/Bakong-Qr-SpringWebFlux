package com.bakong.chongdia.KHQR;

import java.time.Duration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.WebClient;

import io.netty.handler.timeout.ReadTimeoutException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@Slf4j
@Service
@RequiredArgsConstructor
public class TransactionServiceImpl implements TransactionService {

    private final RestTemplate restTemplate;
    private final WebClient webClient;
    
    @Value("${bakong.api.base-url}")
    private String BASE_URL;
    @Value("${bakong.api.token}")
    private String authorization;

    @Override
    public CheckTransactionResponse checkTransactionByMd5(
    		CheckTransactionByMd5Request request
    		) {
        try {
            String url = BASE_URL + "/v1/check_transaction_by_md5";
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization","Bearer " + authorization);
            
            HttpEntity<CheckTransactionByMd5Request> entity = new HttpEntity<>(request, headers);
            
            ResponseEntity<CheckTransactionResponse> response = restTemplate.exchange(
                url,
                HttpMethod.POST,
                entity,
                CheckTransactionResponse.class
            );
            
            CheckTransactionResponse body = response.getBody();
            
            log.info("Transaction check response - Code: {}, ErrorCode: {}, Message: {}", 
                    body.getResponseCode(), body.getErrorCode(), body.getResponseMessage());
            
            return body;
            
        } catch (HttpClientErrorException e) {
            log.error("Client error - Status: {}, Body: {}", e.getStatusCode(), e.getResponseBodyAsString());
            throw handleHttpClientError(e);
            
        } catch (HttpServerErrorException e) {
            log.error("Server error - Status: {}, Body: {}", e.getStatusCode(), e.getResponseBodyAsString());
            throw new BakongApiException(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                ErrorCode.CANNOT_CONNECT_SERVER.getCode(),
                "Internal server error occurred"
            );
            
        } catch (ResourceAccessException e) {
            log.error("Connection error: {}", e.getMessage());
            throw new BakongApiException(
                HttpStatus.SERVICE_UNAVAILABLE.value(),
                ErrorCode.CANNOT_CONNECT_SERVER.getCode(),
                ErrorCode.CANNOT_CONNECT_SERVER.getDescription()
            );
            
        } catch (Exception e) {
            log.error("Unexpected error: {}", e.getMessage(), e);
            throw new BakongApiException(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                ErrorCode.CANNOT_CONNECT_SERVER.getCode(),
                "Unexpected error occurred"
            );
        }
    }
    
    private BakongApiException handleHttpClientError(HttpClientErrorException e) {
        HttpStatus status = (HttpStatus) e.getStatusCode();
        
        // Try to parse error response
        try {
            CheckTransactionResponse errorResponse = e.getResponseBodyAs(CheckTransactionResponse.class);
            if (errorResponse != null && errorResponse.getErrorCode() != null) {
                return new BakongApiException(
                    status.value(),
                    errorResponse.getErrorCode(),
                    errorResponse.getResponseMessage()
                );
            }
        } catch (Exception parseEx) {
            log.warn("Could not parse error response body");
        }
        
        // Handle by HTTP status code
        return switch (status) {
            case BAD_REQUEST -> new BakongApiException(
                HttpStatus.BAD_REQUEST.value(),
                ErrorCode.MISSING_REQUIRED_FIELDS.getCode(),
                "Bad request"
            );
            case UNAUTHORIZED -> new BakongApiException(
                HttpStatus.UNAUTHORIZED.value(),
                ErrorCode.UNAUTHORIZED.getCode(),
                ErrorCode.UNAUTHORIZED.getDescription()
            );
            case FORBIDDEN -> new BakongApiException(
                HttpStatus.FORBIDDEN.value(),
                ErrorCode.UNAUTHORIZED.getCode(),
                "Forbidden"
            );
            case NOT_FOUND -> new BakongApiException(
                HttpStatus.NOT_FOUND.value(),
                ErrorCode.TRANSACTION_NOT_FOUND.getCode(),
                ErrorCode.TRANSACTION_NOT_FOUND.getDescription()
            );
            case TOO_MANY_REQUESTS -> new BakongApiException(
                HttpStatus.TOO_MANY_REQUESTS.value(),
                null,
                "Too many requests"
            );
            default -> new BakongApiException(
                status.value(),
                ErrorCode.CANNOT_CONNECT_SERVER.getCode(),
                "Client error occurred"
            );
        };
    }


	@Override
	public Mono<Boolean> checkIfPaid(String md5, String authorization, Duration timeOut) {
		return checkTransactionApi(md5, authorization)
				.expand(response -> {
					if(response.getResponseCode() != 1) {
						return Mono.empty();
					}
					return Mono.delay(Duration.ofSeconds(2))
							.flatMap(ignore -> checkTransactionApi(md5, authorization));
				})
			.take(timeOut)
            .filter(response -> response.getResponseCode() != 1)
            .next() // យកលទ្ធផលដំបូងដែលជួប
            .map(response -> true)
            .defaultIfEmpty(false);
//            .onErrorReturn(false);

	}
	
	private Mono<CheckTransactionResponse> checkTransactionApi(String md5, String authorization) {
        return webClient.post()
                .uri(BASE_URL + "/v1/check_transaction_by_md5")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + authorization)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(new CheckTransactionByMd5Request(md5))
                .retrieve()
                .bodyToMono(CheckTransactionResponse.class)
                .doOnError(ex -> {
                	if(ex instanceof ReadTimeoutException) {
                		log.error("Final Read Timeout occurred for MD5: {}. Abandoning check.", md5);
                	} else {
                		log.error("API error for MD5: {}. Abandoning check.", md5);
                	}
                })
                .onErrorResume(ex -> {
                		log.error("API error for MD5: {}. Abandoning check in onErrorResume.", md5);
                		return Mono.just(new CheckTransactionResponse(1));
                		}
                );		
    }
	
}
