package com.bakong.chongdia.KHQR;

import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
@Configuration
public class RestTemplateConfig {

    @Bean
    RestTemplate restTemplate(RestTemplateBuilder builder) {
        // Apache HttpClient 5
        CloseableHttpClient httpClient = HttpClients.custom().build();

        HttpComponentsClientHttpRequestFactory factory = new HttpComponentsClientHttpRequestFactory();
        factory.setHttpClient(httpClient); // set HttpClient via setter
        factory.setConnectTimeout(10_000); // 10 sec
        factory.setConnectionRequestTimeout(10_000);
        factory.setReadTimeout(30_000);

        return builder
                .requestFactory(() -> factory)
                .build(); 
    }
}




//@Bean
//RestTemplate restTemplate(RestTemplateBuilder builder) {
//    return builder
//            .requestFactory(() -> {
//                HttpComponentsClientHttpRequestFactory factory = 
//                    new HttpComponentsClientHttpRequestFactory();
//                factory.setConnectTimeout(Duration.ofSeconds(10));
//                factory.setConnectionRequestTimeout(Duration.ofSeconds(10));
//                factory.setReadTimeout(Duration.ofSeconds(30));
//                return factory;
//            })
//            .build();
//}

//@Bean
//RestTemplate restTemplate(RestTemplateBuilder builder) {
//  return builder
//          .setConnectTimeout(Duration.ofSeconds(10)) //កំណត់ពេលចាំចាប់ភ្ជាប់ (connection) — ប្រសិនបើភ្ជាប់ទៅ Server មិនបានក្នុងរយៈពេល 10 វិនាទី វានឹងបោះចោល (throw timeout error)។
//          .setReadTimeout(Duration.ofSeconds(30)) //កំណត់ពេលចាំទទួល data ពី server បន្ទាប់ពីភ្ជាប់បាន។ ប្រសិនបើ server យឺតជាង 30 វិនាទី វានឹងបោះចោលដែរ។
//          .build();
//}