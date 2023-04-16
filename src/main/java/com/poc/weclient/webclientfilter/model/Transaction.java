package com.poc.weclient.webclientfilter.model;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor(staticName = "createTransaction")
@AllArgsConstructor(access = AccessLevel.PUBLIC)
public class Transaction {

    private String transactionType;
    private double itemAmount;
    private int itemQuantity;

}
