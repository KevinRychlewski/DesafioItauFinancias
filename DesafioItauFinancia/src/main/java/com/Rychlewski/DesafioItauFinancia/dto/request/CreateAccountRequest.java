package com.Rychlewski.DesafioItauFinancia.dto.request;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class CreateAccountRequest {

    private String cpf;
    private BigDecimal saldo;

}
