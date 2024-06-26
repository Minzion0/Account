package com.example.account.controller;

import com.example.account.aop.AccountLock;
import com.example.account.dto.CancelBalance;
import com.example.account.dto.QueryTransactionResponse;
import com.example.account.dto.TransactionDto;
import com.example.account.dto.UseBalance;
import com.example.account.exception.AccountException;
import com.example.account.service.TransactionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * 잔액사용 컨트롤러
 * 1. 잔액사용
 * 2. 잔액사용 취소
 * 3. 거래 확인
 */
@Slf4j
@RestController
@RequiredArgsConstructor
public class TransactionController {
    private final TransactionService transactionService;

    @PostMapping("/transaction/use")
    @AccountLock
    public UseBalance.Response useBalance(
            @Valid @RequestBody UseBalance.Request request) {
        try {
            Thread.sleep(5000L);
            TransactionDto transactionDto =
                    transactionService.useBalance(request.getUserId(), request.getAccountNumber(), request.getAmount());

            return UseBalance.Response.from(transactionDto);
        } catch (AccountException e) {
            log.error("Failed to use balance.");

            transactionService.saveFailedUseTransaction(
                    request.getAccountNumber(),
                    request.getAmount()
            );
            throw e;
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

    }

    @PostMapping("/transaction/cancel")
    @AccountLock
    public CancelBalance.Response cancelBalance(
            @Valid @RequestBody CancelBalance.Request request) {
        try {
            TransactionDto transactionDto =
                    transactionService.cancelBalance(request.getTransactionId(), request.getAccountNumber(), request.getAmount());

            return CancelBalance.Response.from(transactionDto);
        } catch (AccountException e) {
            log.error("Failed to use balance.");

            transactionService.saveFailedCancelTransaction(
                    request.getAccountNumber(),
                    request.getAmount()
            );
            throw e;
        }

    }

    @GetMapping("/transaction/{transactionId}")
    public QueryTransactionResponse getTransactions(@PathVariable String transactionId) {
        return QueryTransactionResponse.from(
                transactionService.queryTransaction(transactionId)
        );
    }

}
