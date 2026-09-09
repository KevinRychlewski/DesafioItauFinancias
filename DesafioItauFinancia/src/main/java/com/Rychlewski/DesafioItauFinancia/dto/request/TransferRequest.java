package com.Rychlewski.DesafioItauFinancia.dto.request;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Setter
public class TransferRequest {

    private UUID sourceAccountId;
    private UUID destinationAccountId;
    private BigDecimal amount;

}
