package com.Rychlewski.DesafioItauFinancia.service;

import com.Rychlewski.DesafioItauFinancia.dto.request.TransferRequest;
import com.Rychlewski.DesafioItauFinancia.entity.Account;
import com.Rychlewski.DesafioItauFinancia.repository.AccountRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.testcontainers.utility.TestcontainersConfiguration;
import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;


@SpringBootTest
@Import(TestcontainersConfiguration.class)
class TransferServiceIntegrationTest {

    @Autowired
    private TransferService transferService;

    @Autowired
    private AccountRepository accountRepository;

    Account createAccount() {
        Account account = new Account();
        account.setCpf("cpf-" + java.util.UUID.randomUUID());
        account.setAtivo(true);
        account.setSaldo(BigDecimal.valueOf(100.00));
        return accountRepository.save(account);
    }

    Account createDestinationAccount() {
        Account account = new Account();
        account.setCpf("cpf-" + java.util.UUID.randomUUID());
        account.setAtivo(true);
        account.setSaldo(BigDecimal.ZERO);
        return accountRepository.save(account);
    }

    @Test
    public void deveGarantirQueApenasUmaTransferenciaSejaProcessadaQuandoSaldoForSuficienteParaApenasUma() throws InterruptedException {
        Account origem = createAccount();
        Account destino = createDestinationAccount();

        TransferRequest request1 = new TransferRequest();
        request1.setSourceAccountId(origem.getId());
        request1.setDestinationAccountId(destino.getId());
        request1.setAmount(BigDecimal.valueOf(100.00));

        TransferRequest request2 = new TransferRequest();
        request2.setSourceAccountId(origem.getId());
        request2.setDestinationAccountId(destino.getId());
        request2.setAmount(BigDecimal.valueOf(100.00));

        boolean[] resultados = new boolean[2];

        String chave1 = "chave-thread-1-" + java.util.UUID.randomUUID();
        String chave2 = "chave-thread-2-" + java.util.UUID.randomUUID();

        Thread thread1 = new Thread(() -> {
            try {
                transferService.transferMoney(request1, chave1);
                resultados[0] = true;
            } catch (Exception e) {
                resultados[0] = false;
            }
        });

        Thread thread2 = new Thread(() -> {
            try {
                transferService.transferMoney(request2, chave2);
                resultados[1] = true;
            } catch (Exception e) {
                resultados[1] = false;
            }
        });

        thread1.start();
        thread2.start();

        thread1.join();
        thread2.join();

        assertNotEquals(resultados[0], resultados[1], "Exatamente uma transferência deveria ter sucesso");
        Account origemAtualizada = accountRepository.findById(origem.getId()).orElseThrow();
        assertEquals(BigDecimal.ZERO, origemAtualizada.getSaldo().stripTrailingZeros());

    }


}