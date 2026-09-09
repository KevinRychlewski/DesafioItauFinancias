package com.Rychlewski.DesafioItauFinancia.mapper;

import com.Rychlewski.DesafioItauFinancia.dto.request.CreateAccountRequest;
import com.Rychlewski.DesafioItauFinancia.dto.response.AccountResponse;
import com.Rychlewski.DesafioItauFinancia.entity.Account;

public class AccountMapper {

    public static AccountResponse toResponse(Account account) {
        AccountResponse response = new AccountResponse();
        response.setId(account.getId());
        response.setSaldo(account.getSaldo());
        response.setDataCriacao(account.getDataCriacao());
        response.setCpf(account.getCpf());
        response.setAtivo(account.isAtivo());
        return response;
    }

    public static Account toEntity(CreateAccountRequest request) {
        Account account = new Account();
        account.setSaldo(request.getSaldo());
        account.setCpf(request.getCpf());
        return account;
    }

}
