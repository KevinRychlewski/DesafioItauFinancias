package com.Rychlewski.DesafioItauFinancia.service;

import com.Rychlewski.DesafioItauFinancia.dto.request.TransferRequest;
import com.Rychlewski.DesafioItauFinancia.dto.response.TransferResponse;
import com.Rychlewski.DesafioItauFinancia.entity.Account;
import com.Rychlewski.DesafioItauFinancia.entity.IdempotencyRecord;
import com.Rychlewski.DesafioItauFinancia.exception.*;
import com.Rychlewski.DesafioItauFinancia.repository.AccountRepository;
import com.Rychlewski.DesafioItauFinancia.repository.IdempotencyRecordRepository;
import jakarta.transaction.Transactional;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import tools.jackson.databind.ObjectMapper;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
public class TransferService {

    private final AccountRepository accountRepository;
    private final IdempotencyRecordRepository idempotencyRecordRepository;
    private final ObjectMapper objectMapper;

    public TransferService(AccountRepository accountRepository, IdempotencyRecordRepository idempotencyRecordRepository, ObjectMapper objectMapper) {
        this.accountRepository = accountRepository;
        this.idempotencyRecordRepository = idempotencyRecordRepository;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public TransferResponse transferMoney(TransferRequest request, String idempotencyKey) {
        Optional<IdempotencyRecord> existingRecord = idempotencyRecordRepository.findByIdempotencyKey(idempotencyKey);
        if (existingRecord.isPresent()) {
            IdempotencyRecord record = existingRecord.get();
            try {
                TransferResponse response = objectMapper.readValue(record.getResponseBody(), TransferResponse.class);
                return response;
            } catch (Exception e) {
                throw new RuntimeException("Erro ao desserializar a resposta armazenada", e);
            }
        }
        if (request.getAmount() == null || request.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidAmountException("O valor da transferência deve ser maior que zero");
        }
        if (request.getSourceAccountId().equals(request.getDestinationAccountId())) {
            throw new SameAccountTransferException("A conta de origem e a conta de destino não podem ser iguais");
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
                .orElseThrow(() -> new AccountNotFoundException("Conta com id " + firstId + " não encontrada"));
        Account secondAccount = accountRepository.findByIdForUpdate(secondId)
                .orElseThrow(() -> new AccountNotFoundException("Conta com id " + secondId + " não encontrada"));
        Account sourceAccount = request.getSourceAccountId().equals(firstAccount.getId()) ? firstAccount : secondAccount;
        Account destinationAccount = request.getDestinationAccountId().equals(firstAccount.getId()) ? firstAccount : secondAccount;
        if (!sourceAccount.isAtivo())
            throw new InactiveAccountException("Conta de origem está inativa");
        if (!destinationAccount.isAtivo())
            throw new InactiveAccountException("Conta de destino está inativa");
        if (sourceAccount.getSaldo().compareTo(request.getAmount()) < 0) {
            throw new InsufficientBalanceException("Saldo insuficiente na conta de origem");
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
        try {
            String responseJson = objectMapper.writeValueAsString(response);
            IdempotencyRecord newRecord = new IdempotencyRecord();
            newRecord.setIdempotencyKey(idempotencyKey);
            newRecord.setResponseBody(responseJson);
            idempotencyRecordRepository.save(newRecord);
        } catch (DataIntegrityViolationException e) {
            IdempotencyRecord record = idempotencyRecordRepository.findByIdempotencyKey(idempotencyKey)
                    .orElseThrow(() -> new RuntimeException("Erro ao recuperar o registro de idempotência após violação de integridade"));
            try {
                return objectMapper.readValue(record.getResponseBody(), TransferResponse.class);
            }catch (Exception ex) {
                throw new RuntimeException("Erro ao desserializar a resposta armazenada", ex);
            }
         }
        return response;
    }

}
