package com.example.account.aop;

import java.lang.annotation.*;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited//상속 가능한 상태
public @interface AccountLock {
    long tryLockTime() default 5000L;
}
