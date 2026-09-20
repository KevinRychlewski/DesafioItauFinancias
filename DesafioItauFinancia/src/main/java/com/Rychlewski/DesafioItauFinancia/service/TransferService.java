package com.Rychlewski.DesafioItauFinancia.service;

import com.Rychlewski.DesafioItauFinancia.dto.request.TransferRequest;
import com.Rychlewski.DesafioItauFinancia.dto.response.TransferResponse;
import com.Rychlewski.DesafioItauFinancia.entity.Account;
import com.Rychlewski.DesafioItauFinancia.repository.AccountRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class TransferService {

    private final AccountRepository accountRepository;

    public TransferService(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    @Transactional
    public TransferResponse transferMoney(TransferRequest request) {
        if (request.getAmount() == null || request.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new RuntimeException("O valor da transferência deve ser maior que zero");
        }
        if (request.getSourceAccountId().equals(request.getDestinationAccountId())) {
            throw new RuntimeException("A conta de origem e a conta de destino não podem ser iguais");
        }
        UUID firstId;
        UUID secondId;
        if (request.getSourceAccountId().compareTo(request.getDestinationAccountId()) < 0) {
            firstId = request.getSourceAccountId();
            secondId = request.getDestinationAccountId();
        } else {
            firstId = request.getDestinationAccountId();
            secondId = request.getSourceAccountId();
        }
        Account firstAccount = accountRepository.findByIdForUpdate(firstId)
                .orElseThrow(() -> new RuntimeException("Conta com id " + firstId + " não encontrada"));
        Account secondAccount = accountRepository.findByIdForUpdate(secondId)
                .orElseThrow(() -> new RuntimeException("Conta com id " + secondId + " não encontrada"));
        Account sourceAccount = request.getSourceAccountId().equals(firstAccount.getId()) ? firstAccount : secondAccount;
        Account destinationAccount = request.getDestinationAccountId().equals(firstAccount.getId()) ? secondAccount : firstAccount;
        if (!sourceAccount.isAtivo())
            throw new RuntimeException("Conta de origem está inativa");
        if (!destinationAccount.isAtivo())
            throw new RuntimeException("Conta de destino está inativa");
        if (sourceAccount.getSaldo().compareTo(request.getAmount()) < 0) {
            throw new RuntimeException("Saldo insuficiente na conta de origem");
        }
        sourceAccount.setSaldo(sourceAccount.getSaldo().subtract(request.getAmount()));
        destinationAccount.setSaldo(destinationAccount.getSaldo().add(request.getAmount()));
        accountRepository.save(sourceAccount);
        accountRepository.save(destinationAccount);
        TransferResponse response = new TransferResponse();
        response.setSourceAccountId(sourceAccount.getId());
        response.setDestinationAccountId(destinationAccount.getId());
        response.setAmount(request.getAmount());
        response.setTimestamp(LocalDateTime.now());
        return response;
    }

}
