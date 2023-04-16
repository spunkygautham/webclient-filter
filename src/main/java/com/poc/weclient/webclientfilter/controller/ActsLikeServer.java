package com.poc.weclient.webclientfilter.controller;

import com.google.gson.Gson;
import com.poc.weclient.webclientfilter.model.Transaction;
import com.poc.weclient.webclientfilter.util.AesUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@Slf4j
public class ActsLikeServer {
    @PostMapping("/modifyTransaction")
    private Transaction createTransaction(@RequestBody Transaction transaction) {
        return new Transaction(transaction.getTransactionType(), transaction.getItemAmount() * 2 / 5, transaction.getItemQuantity());
    }


    @PostMapping("/convertToBytes")
    private Mono<Transaction> server(@RequestBody String payload) {
        log.info("payload = " + payload);
        String decrypted = AesUtil.decrypt(payload);
        log.info("decrypted payload = " + decrypted);
        Transaction transaction = new Gson().fromJson(decrypted, Transaction.class);
        return Mono.just(transaction);
    }
}
