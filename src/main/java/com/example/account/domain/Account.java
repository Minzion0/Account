package com.example.account.domain;

import com.example.account.exception.AccountException;
import com.example.account.type.AccountStatus;
import com.example.account.type.ErrorCode;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
@Entity
public class Account {
    @Id
    @GeneratedValue
    private Long id;

    @ManyToOne
    private AccountUser accountUser;

    private String accountNumber;

    private Long balance;

    @Enumerated(EnumType.STRING)//Enum을 문자 그대로 db에 저장할수 있도록 설정
    private AccountStatus accountStatus;

    private LocalDateTime registeredAt;
    private LocalDateTime unRegisteredAt;

    @CreatedDate
    private LocalDateTime createdAt;
    @LastModifiedDate
    private LocalDateTime updatedAt;

    public void useBalance(Long amount){
        if (amount> this.balance){
            throw new AccountException(ErrorCode.AMOUNT_EXCEED_BALANCE);
        }
        balance-=amount;
    }
    public void cancelBalance(Long amount){
        if (amount< 0){
            throw new AccountException(ErrorCode.INVALID_REQUEST);
        }
        balance+=amount;
    }

}
