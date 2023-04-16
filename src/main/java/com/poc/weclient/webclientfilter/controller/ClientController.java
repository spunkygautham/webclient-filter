package com.poc.weclient.webclientfilter.controller;

import com.poc.weclient.webclientfilter.model.Transaction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@RestController
public class ClientController {

    @Autowired
    private WebClient webClient;

    @PostMapping("/client")
    @ResponseBody
    private Mono<Transaction> client(@RequestBody Transaction payload) {
        return webClient.post()
                .uri(endpoint -> endpoint.path("convertToBytes").build())
                .bodyValue(payload)
                .retrieve()
                .bodyToMono(Transaction.class);
    }
}
