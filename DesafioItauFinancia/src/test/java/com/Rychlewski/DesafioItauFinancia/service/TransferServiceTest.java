package com.Rychlewski.DesafioItauFinancia.service;

import com.Rychlewski.DesafioItauFinancia.dto.request.CreateAccountRequest;
import com.Rychlewski.DesafioItauFinancia.dto.request.TransferRequest;
import com.Rychlewski.DesafioItauFinancia.dto.response.TransferResponse;
import com.Rychlewski.DesafioItauFinancia.entity.Account;
import com.Rychlewski.DesafioItauFinancia.exception.*;
import com.Rychlewski.DesafioItauFinancia.repository.AccountRepository;
import com.Rychlewski.DesafioItauFinancia.repository.IdempotencyRecordRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tools.jackson.databind.ObjectMapper;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class TransferServiceTest {

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private IdempotencyRecordRepository idempotencyRecordRepository;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private TransferService transferService;

    @Test
    public void deveLancarExcecaoQuandoValorForZeroOuNegativo() {
        TransferRequest request = new TransferRequest();
        request.setSourceAccountId(UUID.randomUUID());
        request.setDestinationAccountId(UUID.randomUUID());
        request.setAmount(BigDecimal.ZERO);

        assertThrows(InvalidAmountException.class, () -> {
            transferService.transferMoney(request, "qualquer-chave-123");
        });
    }

    @Test
    public void deveLancarExcecaoQuandoContaForemIguais() {
        TransferRequest request = new TransferRequest();
        UUID sourceAccount = UUID.randomUUID();
        request.setSourceAccountId(sourceAccount);
        request.setDestinationAccountId(sourceAccount);
        request.setAmount(BigDecimal.valueOf(100));

        assertThrows(SameAccountTransferException.class, () -> {
            transferService.transferMoney(request, "qualquer-chave-123");
        });
    }

    @Test
    public void deveLancarExcecaoQuandoNaoEncontrarContaDeOrigem() {
        TransferRequest request = new TransferRequest();
        when(accountRepository.findByIdForUpdate(any(UUID.class))).thenReturn(java.util.Optional.empty());
        request.setSourceAccountId(UUID.randomUUID());
        request.setDestinationAccountId(UUID.randomUUID());
        request.setAmount(BigDecimal.valueOf(100));
        assertThrows(AccountNotFoundException.class, () -> {
            transferService.transferMoney(request, "qualquer-chave-123");
        });
    }

    @Test
    public void deveLancarExcecaoQuandoContaNaoEstiverAtiva() {
        Account contaFake = new Account();
        contaFake.setId(UUID.randomUUID());
        contaFake.setCpf("12345678900");
        contaFake.setSaldo(BigDecimal.valueOf(1000));
        contaFake.setAtivo(false);
        when(accountRepository.findByIdForUpdate(any(UUID.class))).thenReturn(Optional.of(contaFake));
        TransferRequest request = new TransferRequest();
        request.setSourceAccountId(contaFake.getId());
        request.setDestinationAccountId(UUID.randomUUID());
        request.setAmount(BigDecimal.valueOf(100));

        assertThrows(InactiveAccountException.class, () -> {
            transferService.transferMoney(request, "qualquer-chave-123");
        });
    }

    @Test
    public void deveLancarExcecaoQuandoSaldoForInsuficiente() {
        Account contaFake = new Account();
        contaFake.setId(UUID.randomUUID());
        contaFake.setCpf("12345678900");
        contaFake.setSaldo(BigDecimal.valueOf(50));
        contaFake.setAtivo(true);
        when(accountRepository.findByIdForUpdate(any(UUID.class))).thenReturn(Optional.of(contaFake));
        TransferRequest request = new TransferRequest();
        request.setSourceAccountId(contaFake.getId());
        request.setDestinationAccountId(UUID.randomUUID());
        request.setAmount(BigDecimal.valueOf(100));

        assertThrows(InsufficientBalanceException.class, () -> {
            transferService.transferMoney(request, "qualquer-chave-123");
        });
    }
}
