package com.Rychlewski.DesafioItauFinancia.controller;

import com.Rychlewski.DesafioItauFinancia.dto.request.TransferRequest;
import com.Rychlewski.DesafioItauFinancia.dto.response.TransferResponse;
import com.Rychlewski.DesafioItauFinancia.service.TransferService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/transfers")
public class TransferController {

    private final TransferService transferService;

    public TransferController(TransferService transferService) {
        this.transferService = transferService;
    }

    @PostMapping()
    public ResponseEntity<TransferResponse> transfer(@RequestBody TransferRequest request, @RequestHeader("Idempotency-Key") String idempotencyKey) {
        TransferResponse response = transferService.transferMoney(request, idempotencyKey);
        return ResponseEntity.ok(response);
    }


}
