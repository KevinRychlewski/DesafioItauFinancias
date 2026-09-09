package com.Rychlewski.DesafioItauFinancia.dto.response;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
public class TransferResponse {

    private BigDecimal amount;
    private UUID sourceAccountId;
    private UUID destinationAccountId;
    private LocalDateTime timestamp;
}
