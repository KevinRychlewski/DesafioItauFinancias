package com.Rychlewski.DesafioItauFinancia.dto.response;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
public class AccountResponse {

    private UUID id;
    private BigDecimal saldo;
    private LocalDateTime dataCriacao;
    private String cpf;
    private boolean ativo;

}
