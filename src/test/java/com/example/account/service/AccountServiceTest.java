package com.example.account.service;

import com.example.account.domain.Account;
import com.example.account.domain.AccountUser;
import com.example.account.dto.AccountDto;
import com.example.account.exception.AccountException;
import com.example.account.repository.AccountRepository;
import com.example.account.repository.AccountUserRepository;
import com.example.account.type.AccountStatus;
import com.example.account.type.ErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class AccountServiceTest {
    @Mock
    private AccountRepository accountRepository;

    @Mock
    private AccountUserRepository accountUserRepository;

    @InjectMocks
    private AccountService accountService;


    @Test
    void creatAccountSuccess() {
        //given
        AccountUser accountUser = AccountUser.builder()
                .name("pobi").build();
        accountUser.setId(12L);
        given(accountUserRepository.findById(anyLong()))
                .willReturn(
                        Optional.of(
                                accountUser));

        given(accountRepository.save(any())).willReturn(
                Account.builder()
                        .accountUser(accountUser)
                        .accountNumber("1000000013")
                        .build());
        ArgumentCaptor<Account> captor = ArgumentCaptor.forClass(Account.class);
        //when
        AccountDto accountDto = accountService.createAccount(1L, 1000L);

        //then
        verify(accountRepository, times(1)).save(captor.capture());
        assertEquals(accountUser.getId(), accountDto.getUserId());


    }

    @Test
    void creatFirstAccountSuccess() {
        //given
        AccountUser accountUser = AccountUser.builder()
                .name("pobi").build();
        accountUser.setId(12L);
        given(accountUserRepository.findById(anyLong()))
                .willReturn(
                        Optional.of(
                                accountUser));

        given(accountRepository.save(any())).willReturn(
                Account.builder()
                        .accountUser(accountUser)
                        .accountNumber("1000000015")
                        .build());
        ArgumentCaptor<Account> captor = ArgumentCaptor.forClass(Account.class);
        //when
        AccountDto accountDto = accountService.createAccount(1L, 1000L);

        //then
        verify(accountRepository, times(1)).save(captor.capture());
        assertEquals(accountUser.getId(), accountDto.getUserId());

    }


    @Test
    @DisplayName("해당 유저 없음 -계좌 생성 실패")
    void creattAccountUserNotFound() {
        //given
        AccountUser accountUser = AccountUser.builder()
                .name("pobi").build();
        accountUser.setId(12L);
        given(accountUserRepository.findById(anyLong()))
                .willReturn(
                        Optional.empty());

        //when
        AccountException accountException = assertThrows(AccountException.class, () ->
                accountService.createAccount(1L, 1000L));


        //then
        assertEquals(ErrorCode.USER_NOT_FOUND, accountException.getErrorCode());
    }

    @Test
    @DisplayName("유저 당 최대 계좌는 10개")
    void createAccount_maxAccountIs10() {
        //given
        AccountUser accountUser = AccountUser.builder()

                .name("pobi").build();
        accountUser.setId(12L);
        given(accountUserRepository.findById(anyLong()))
                .willReturn(Optional.of(accountUser));

        given(accountRepository.countByAccountUser(any())).willReturn(10);
        //when
        AccountException accountException = assertThrows(AccountException.class, () -> accountService.createAccount(1L, 10000L));

        //then
        assertEquals(ErrorCode.MEX_ACCOUNT_PER_USER_10, accountException.getErrorCode());
    }

    @Test
    void deleteAccountSuccess() {
        //given
        AccountUser accountUser = AccountUser.builder()
                .name("pobi").build();
        accountUser.setId(12L);
        given(accountUserRepository.findById(anyLong()))
                .willReturn(Optional.of(accountUser));

        given(accountRepository.findByAccountNumber(anyString()))
                .willReturn(Optional.of(Account
                        .builder()
                        .accountUser(accountUser)
                        .balance(0L)
                        .accountNumber("1000000012").build()));
        ArgumentCaptor<Account> captor = ArgumentCaptor.forClass(Account.class);
        //when
        AccountDto accountDto = accountService.deleteAccount(12L, "1000000012");

        //then
        verify(accountRepository, times(1)).save(captor.capture());
        assertEquals(12L, accountDto.getUserId());
        assertEquals("1000000012", captor.getValue().getAccountNumber());
        assertEquals(AccountStatus.UNREGISTERED, captor.getValue().getAccountStatus());

    }

    @Test
    @DisplayName("해당 유저 없음 -계좌 해지 실패")
    void deleteAccount_UserNotFound() {
        //given
        AccountUser accountUser = AccountUser.builder()
                .name("pobi").build();
        accountUser.setId(12L);
        given(accountUserRepository.findById(anyLong()))
                .willReturn(
                        Optional.empty());

        //when
        AccountException accountException = assertThrows(AccountException.class, () ->
                accountService.deleteAccount(1L, "1000000000"));


        //then
        assertEquals(ErrorCode.USER_NOT_FOUND, accountException.getErrorCode());
    }

    @Test
    @DisplayName("해당 계좌 없음 -계좌 해지 실패")
    void deleteAccount_AccountNotFound() {
        //given
        AccountUser accountUser = AccountUser.builder()
                .name("pobi").build();
        accountUser.setId(12L);
        given(accountUserRepository.findById(anyLong()))
                .willReturn(
                        Optional.of(accountUser));
        given(accountRepository.findByAccountNumber(any()))
                .willReturn(Optional.empty());
        //when
        AccountException accountException = assertThrows(AccountException.class, () ->
                accountService.deleteAccount(1L, "1000000000"));


        //then
        assertEquals(ErrorCode.ACCOUNT_NOT_FOUND, accountException.getErrorCode());
    }

    @Test
    @DisplayName("계좌 소유주 다름")
    void deleteAccountFailed_userUnMathch() {
        //given
        AccountUser accountUser = AccountUser.builder()
                .name("pobi").build();
        accountUser.setId(12L);
        AccountUser accountUser2 = AccountUser.builder()
                .name("ppp").build();
        accountUser2.setId(13L);
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
        AccountException accountException = assertThrows(AccountException.class, () -> accountService.deleteAccount(12L, "1000000012"));

        //then
        assertEquals(accountException.getErrorCode(), ErrorCode.USER_ACCOUNT_UN_MATCH);

    }

    @Test
    void deleteAccountFailed_NotEmpty_balance() {
        //given
        AccountUser accountUser = AccountUser.builder()
                .name("pobi").build();
        accountUser.setId(12L);
        given(accountUserRepository.findById(anyLong()))
                .willReturn(
                        Optional.of(
                                accountUser));
        given(accountRepository.findByAccountNumber(anyString()))
                .willReturn(Optional.of(
                        Account.builder()
                                .accountUser(accountUser)
                                .accountNumber("1000000002")
                                .balance(110L)
                                .build()));
        ArgumentCaptor<Account> captor = ArgumentCaptor.forClass(Account.class);
        //when
        AccountException accountException = assertThrows(AccountException.class, () -> accountService.deleteAccount(1L, "1000000002"));

        assertEquals(accountException.getErrorCode(), ErrorCode.BALANCE_NOT_EMPTY);
    }


    @Test
    @DisplayName("계좌가 이미 해지 되었습니다")
    void deleteAccountFailed_account_unregistered() {
        //given
        AccountUser accountUser = AccountUser.builder()
                .name("pobi").build();
        accountUser.setId(12L);
        given(accountUserRepository.findById(anyLong()))
                .willReturn(
                        Optional.of(
                                accountUser));
        given(accountRepository.findByAccountNumber(anyString()))
                .willReturn(Optional.of(
                        Account.builder()
                                .accountUser(accountUser)
                                .accountNumber("1000000002")
                                .accountStatus(AccountStatus.UNREGISTERED)
                                .build()));
        ArgumentCaptor<Account> captor = ArgumentCaptor.forClass(Account.class);
        //when
        AccountException accountException = assertThrows(AccountException.class, () -> accountService.deleteAccount(1L, "1000000002"));

        assertEquals(accountException.getErrorCode(), ErrorCode.ACCOUNT_ALREADY_UNREGISTERED);
    }

    @Test
    void successGetAccountsByUserId(){
        //given
        AccountUser accountUser = AccountUser.builder()
                .name("pobi").build();
        accountUser.setId(12L);
        List<Account> accounts =
                Arrays.asList(
                        Account.builder()
                                .accountUser(accountUser)
                                .accountNumber("1000000000")
                                .balance(1000L)
                                .build(),
                        Account.builder()
                                .accountUser(accountUser)
                                .accountNumber("1000000001")
                                .balance(2000L)
                                .build()
                );
        given(accountUserRepository.findById(anyLong()))
                .willReturn(
                        Optional.of(accountUser));
        given(accountRepository.findByAccountUser(any()))
                .willReturn(accounts);
        //when
        List<AccountDto> accountDtos = accountService.getAccountsByUserId(12L);
        //then
        assertEquals(accountDtos.size(),accounts.size());
        for (int i = 0; i <accounts.size(); i++) {
            assertEquals(accounts.get(i).getAccountNumber(),accountDtos.get(i).getAccountNumber());
            assertEquals(accounts.get(i).getBalance(),accountDtos.get(i).getBalance());
        }


    }
    @Test
    void failedToGetAccounts(){
        //given
        given(accountUserRepository.findById(anyLong()))
                .willReturn(Optional.empty());
        //when
        AccountException accountException =
                assertThrows(AccountException.class,
                        () -> accountService.getAccountsByUserId(1L));

        //then

        assertEquals(accountException.getErrorCode(),ErrorCode.USER_NOT_FOUND);
    }

}