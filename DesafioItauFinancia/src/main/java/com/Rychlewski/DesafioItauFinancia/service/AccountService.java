package com.Rychlewski.DesafioItauFinancia.service;

import com.Rychlewski.DesafioItauFinancia.dto.request.CreateAccountRequest;
import com.Rychlewski.DesafioItauFinancia.dto.response.AccountResponse;
import com.Rychlewski.DesafioItauFinancia.entity.Account;
import com.Rychlewski.DesafioItauFinancia.mapper.AccountMapper;
import com.Rychlewski.DesafioItauFinancia.repository.AccountRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.UUID;

@Service
public class AccountService {

    private final AccountRepository accountRepository;

    public AccountService(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    public AccountResponse createAccount(CreateAccountRequest request) {
        if (request.getSaldo() == null || request.getSaldo().compareTo(BigDecimal.ZERO) < 0) {
            throw new RuntimeException("O saldo inicial não pode ser negativo");
        }
        Account account = AccountMapper.toEntity(request);
        Account savedAccount = accountRepository.save(account);
        return AccountMapper.toResponse(savedAccount);
    }

    public AccountResponse getAccount(UUID id) {
        AccountResponse response = accountRepository.findById(id)
                .map(AccountMapper::toResponse)
                .orElseThrow(() -> new RuntimeException("Conta com id " + id + " não encontrada"));
        return response;
    }

}