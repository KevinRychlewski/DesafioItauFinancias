package com.Rychlewski.DesafioItauFinancia.controller;

import com.Rychlewski.DesafioItauFinancia.dto.request.CreateAccountRequest;
import com.Rychlewski.DesafioItauFinancia.dto.response.AccountResponse;
import com.Rychlewski.DesafioItauFinancia.service.AccountService;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/accounts")
public class AccountController {

    private final AccountService accountService;

    public AccountController(AccountService accountService) {
        this.accountService = accountService;
    }

    @Operation(
            summary = "Cria uma nova conta",
            description = "Cria uma conta com um saldo inicial informado, para simular cenários de teste."
    )
    @PostMapping()
    public ResponseEntity<AccountResponse> createAccount(@RequestBody CreateAccountRequest request) {
        AccountResponse response = accountService.createAccount(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(
            summary = "Consulta uma conta pelo ID",
            description = "Retorna os detalhes da conta correspondente ao ID fornecido."
    )
    @GetMapping("/{id}")
    public ResponseEntity<AccountResponse> getAccountById(@PathVariable UUID id) {
        return ResponseEntity.ok(accountService.getAccount(id));
    }

}
