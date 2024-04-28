package com.example.account.service;

import com.example.account.domain.Account;
import com.example.account.domain.AccountUser;
import com.example.account.domain.Transaction;
import com.example.account.dto.TransactionDto;
import com.example.account.exception.AccountException;
import com.example.account.repository.AccountRepository;
import com.example.account.repository.AccountUserRepository;
import com.example.account.repository.TransactionRepository;
import com.example.account.type.AccountStatus;
import com.example.account.type.ErrorCode;
import com.example.account.type.TransactionResultType;
import com.example.account.type.TransactionType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class TransactionServiceTest {
    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private AccountUserRepository accountUserRepository;

    @InjectMocks
    private TransactionService transactionService;

    @Test
    void successUseBalance(){
        //given
        AccountUser accountUser = AccountUser.builder()
                .id(12L)
                .name("pobi").build();

        Account account = Account.builder()
                .accountUser(accountUser)
                .accountStatus(AccountStatus.IN_USE)
                .balance(10000L)
                .accountNumber("1000000012").build();

        given(accountUserRepository.findById(anyLong()))
                .willReturn(Optional.of(accountUser));
        given(transactionRepository.save(any()))
                .willReturn(
                        Transaction.builder()
                                .amount(5000L)
                                .account(account)
                                .balanceSnapshot(10000L-5000L)
                                .transactionType(TransactionType.USE)
                                .transactionResultType(TransactionResultType.S)
                                .transactedAt(LocalDateTime.now())
                                .build());

        given(accountRepository.findByAccountNumber(anyString()))
                .willReturn(Optional.of(Account
                        .builder()
                        .accountUser(accountUser)
                        .accountStatus(AccountStatus.IN_USE)
                        .balance(10000L)
                        .accountNumber("1000000012").build()));
        //when
        TransactionDto transactionDto = transactionService.useBalance(1L, "1000000012", 5000L);

        //then
        assertEquals(10000L-5000L,transactionDto.getBalanceSnapshot());
        assertEquals(transactionDto.getTransactionType(),TransactionType.USE);
        assertEquals(transactionDto.getTransactionResultType(),TransactionResultType.S);
        assertEquals(transactionDto.getAmount(),5000L);
    }

    @Test
    @DisplayName("해당 유저 없음 -결제 실패")
    void useBalance_UserNotFound() {
        //given
        AccountUser accountUser = AccountUser.builder()
                .id(12L)
                .name("pobi").build();
        given(accountUserRepository.findById(anyLong()))
                .willReturn(
                        Optional.empty());

        //when
        AccountException accountException = assertThrows(AccountException.class, () ->
                transactionService.useBalance(12L, "1000000000",5000L));


        //then
        assertEquals(ErrorCode.USER_NOT_FOUND, accountException.getErrorCode());
    }

    @Test
    @DisplayName("해당 계좌 없음 -결제 실패")
    void useBalance_AccountNotFound() {
        //given
        AccountUser accountUser = AccountUser.builder()
                .id(12L)
                .name("pobi").build();
        given(accountUserRepository.findById(anyLong()))
                .willReturn(
                        Optional.of(accountUser));
        given(accountRepository.findByAccountNumber(any()))
                .willReturn(Optional.empty());
        //when
        AccountException accountException = assertThrows(AccountException.class, () ->
                transactionService.useBalance(12L, "1000000000",5000L));


        //then
        assertEquals(ErrorCode.ACCOUNT_NOT_FOUND, accountException.getErrorCode());
    }

    @Test
    @DisplayName("계좌 소유주 다름")
    void useBalance_userUnMathch() {
        //given
        AccountUser accountUser = AccountUser.builder()
                .id(12L)
                .name("pobi").build();
        AccountUser accountUser2 = AccountUser.builder()
                .id(13L)
                .name("ppp").build();
        given(accountUserRepository.findById(anyLong()))
                .willReturn(Optional.of(accountUser));

        given(accountRepository.findByAccountNumber(anyString()))
                .willReturn(Optional.of(Account
                        .builder()
                        .accountUser(accountUser2)
                        .balance(0L)
                        .accountNumber("1000000012").build()));
        ArgumentCaptor<Account> captor = ArgumentCaptor.forClass(Account.class);
        //when
        AccountException accountException = assertThrows(AccountException.class,
                () -> transactionService.useBalance(12L, "1000000000",5000L));

        //then
        assertEquals(accountException.getErrorCode(), ErrorCode.USER_ACCOUNT_UN_MATCH);

    }

    @Test
    void deleteAccountFailed_NotEmpty_balance() {
        //given
        AccountUser accountUser = AccountUser.builder()
                .id(12L)
                .name("pobi").build();
        given(accountUserRepository.findById(anyLong()))
                .willReturn(
                        Optional.of(accountUser));
        given(accountRepository.findByAccountNumber(anyString()))
                .willReturn(Optional.of(
                        Account.builder()
                                .accountUser(accountUser)
                                .accountStatus(AccountStatus.IN_USE)
                                .accountNumber("1000000002")
                                .balance(110L)
                                .build()));
        ArgumentCaptor<Account> captor = ArgumentCaptor.forClass(Account.class);
        //when
        AccountException accountException = assertThrows(AccountException.class,
                () -> transactionService.useBalance(12L, "1000000000",5000L));

        assertEquals(accountException.getErrorCode(), ErrorCode.AMOUNT_EXCEED_BALANCE);
        verify(transactionRepository,times(0)).save(any());
    }

    @Test
    @DisplayName("결제 실패시 실패한 결제 정보 저장")
    void saveFailedUseTransaction(){
        //given
        AccountUser accountUser = AccountUser.builder()
                .id(12L)
                .name("pobi").build();

        Account account = Account.builder()
                .accountUser(accountUser)
                .accountStatus(AccountStatus.IN_USE)
                .registeredAt(LocalDateTime.now())
                .balance(10000L)
                .accountNumber("1000000012").build();

        given(accountRepository.findByAccountNumber(anyString()))
                .willReturn(Optional.of(account));

        given(transactionRepository.save(any()))
                .willReturn(Transaction.builder()
                                .account(account)
                                .amount(5000L)
                                .account(account)
                                .balanceSnapshot(5000L)
                                .transactionType(TransactionType.USE)
                                .transactionResultType(TransactionResultType.S)
                                .transactionId("transactionId")
                                .transactedAt(LocalDateTime.now())
                                .build());


        ArgumentCaptor<Transaction> captor = ArgumentCaptor.forClass(Transaction.class);

        //when
        transactionService.saveFailedUseTransaction("1000000012",50000L);

        //then
        verify(transactionRepository,times(1)).save(captor.capture());
        assertEquals(10000L,captor.getValue().getBalanceSnapshot());
        assertEquals(TransactionResultType.F,captor.getValue().getTransactionResultType());
    }




}