package com.example.account.service;

import com.example.account.domain.Account;
import com.example.account.domain.AccountUser;
import com.example.account.dto.AccountDto;
import com.example.account.exception.AccountException;
import com.example.account.repository.AccountRepository;
import com.example.account.repository.AccountUserRepository;
import com.example.account.type.AccountStatus;
import com.example.account.type.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.example.account.type.ErrorCode.*;

@Service
@RequiredArgsConstructor
public class AccountService {
    private final AccountRepository accountRepository;
    private final AccountUserRepository accountUserRepository;

    /**
     * //사용자가 있는지 조회
     * //계좌번호 를 생성하고
     * //계좌를 저장하고 그정보를 넘긴다.
     *
     * @param userId         유저 아이디
     * @param initialBalance 최소 생성시 입금액
     */
    @Transactional
    public AccountDto createAccount(Long userId, Long initialBalance) {
        AccountUser accountUser = getAccountUser(userId);

        //유저 계좌수 체크
        validateCreateAccount(accountUser);

        String newAccountNumber = createAccountNumber();


        return AccountDto.fromEntity(
                accountRepository.save(
                        Account.builder()
                                .accountUser(accountUser)
                                .accountNumber(newAccountNumber)
                                .accountStatus(AccountStatus.IN_USE)
                                .balance(initialBalance)
                                .registeredAt(LocalDateTime.now())
                                .build())
        );
    }

    //10자리의 계좌번호 랜덤 생성 메소드
    private String createAccountNumber() {
        boolean chackAccountNumber =true;
        String newAccountNumber=null;
        SecureRandom secureRandom = new SecureRandom();
        //Math.random 보다 강력한 난수를 생성해주는 클래스 왜? random= 48비트,SecureRandom= 최대128비트
        final int MIN_ACCOUNT_NUM = 900_000_000;
        final int MAX_ACCOUNT_NUM =1_000_000_000;

        while (chackAccountNumber){
            newAccountNumber = String.valueOf(secureRandom.nextInt(MIN_ACCOUNT_NUM) + MAX_ACCOUNT_NUM);
            Optional<Account> byAccountNumber = accountRepository.findByAccountNumber(newAccountNumber);
            if (byAccountNumber.isEmpty()){
                chackAccountNumber=false;
            }

        }
        return newAccountNumber;
    }

    private void validateCreateAccount(AccountUser accountUser) {
        if (accountRepository.countByAccountUser(accountUser) >= 10) {
            throw new AccountException(ErrorCode.MEX_ACCOUNT_PER_USER_10);
        }
    }

    @Transactional
    public Account getAccount(Long id) {
        if (id < 0) {
            throw new RuntimeException("Minus");
        }
        return accountRepository.findById(id).get();

    }

    @Transactional
    public AccountDto deleteAccount(Long userId, String accountNumber) {
        AccountUser accountUser = getAccountUser(userId);

        Account account = accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new AccountException(ErrorCode.ACCOUNT_NOT_FOUND));

        validateDeleteAccount(accountUser, account);
        account.setAccountStatus(AccountStatus.UNREGISTERED);
        account.setUnRegisteredAt(LocalDateTime.now());

        accountRepository.save(account);

        return AccountDto.fromEntity(account);

    }

    private AccountUser getAccountUser(Long userId) {
        return accountUserRepository.findById(userId)
                .orElseThrow(() -> new AccountException(USER_NOT_FOUND));
    }

    private void validateDeleteAccount(AccountUser accountUser, Account account) throws AccountException {
        if (accountUser.getId() != account.getAccountUser().getId()) {
            throw new AccountException(USER_ACCOUNT_UN_MATCH);
        }
        if (account.getAccountStatus() == AccountStatus.UNREGISTERED) {
            throw new AccountException(ACCOUNT_ALREADY_UNREGISTERED);
        }
        if (account.getBalance() > 0) {
            throw new AccountException(BALANCE_NOT_EMPTY);
        }
    }
    @Transactional
    public List<AccountDto> getAccountsByUserId(Long userId) {
        AccountUser accountUser = getAccountUser(userId);

        List<Account> accounts =
                accountRepository.findByAccountUser(accountUser);

        return accounts.stream()
                .map(AccountDto::fromEntity)
                .collect(Collectors.toList());
    }
}
