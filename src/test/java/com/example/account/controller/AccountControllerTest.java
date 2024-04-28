package com.example.account.controller;

import com.example.account.domain.Account;
import com.example.account.dto.AccountDto;
import com.example.account.dto.CreateAccount;
import com.example.account.dto.DeleteAccount;
import com.example.account.service.AccountService;
import com.example.account.service.RedisTestService;
import com.example.account.type.AccountStatus;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;


import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AccountController.class)
class AccountControllerTest {

    @MockBean
    private AccountService accountService;

    @MockBean
    private RedisTestService redisTestService;

    @Autowired
    private MockMvc mvc;
    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void successCreateAccount ()throws Exception {
        //given
        given(accountService.createAccount(anyLong(),anyLong()))
                .willReturn(AccountDto.builder()
                        .userId(1L)
                        .accountNumber("1000000000")
                        .registeredAt(LocalDateTime.now())
                        .unRegisteredAt(LocalDateTime.now())
                        .build());
        //when


        //then
        mvc.perform(
                MockMvcRequestBuilders.post("/account")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(
                        new CreateAccount.Request(1L,100L)
                )))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(1L))
                .andExpect(jsonPath("$.accountNumber").value("1000000000"))
                .andDo(print());
    }

    @Test
    void successGetAccount()throws Exception{
        //given
        given(accountService.getAccount(anyLong()))
                .willReturn(Account.builder()
                        .accountNumber("1234")
                        .accountStatus(AccountStatus.IN_USE)
                        .build());
        //when

        //then
        mvc.perform(
                MockMvcRequestBuilders.get("/account/876"))
                .andDo(print())
                .andExpect(jsonPath("$.accountNumber").value("1234"))
                .andExpect(jsonPath("$.accountStatus").value("IN_USE"))
                .andExpect(status().isOk());

    }

    @Test
    void successDeleteAccount ()throws Exception {
        //given
        given(accountService.deleteAccount(anyLong(),anyString()))
                .willReturn(AccountDto.builder()
                        .userId(1L)
                        .accountNumber("1000000000")
                        .registeredAt(LocalDateTime.now())
                        .unRegisteredAt(LocalDateTime.now())
                        .build());
        //when


        //then
        mvc.perform(MockMvcRequestBuilders.delete("/account")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(
                                        new DeleteAccount.Request(1L,"1000000000")
                                )))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(1L))
                .andExpect(jsonPath("$.accountNumber").value("1000000000"))
                .andDo(print());
    }

    @Test
    void successGetAccountListByUser() throws Exception {
        //given
        List<AccountDto>accountDtos =
                Arrays.asList(
                        AccountDto.builder()
                                .accountNumber("1000000000")
                                .balance(1000L)
                                .build(),
                        AccountDto.builder()
                                .accountNumber("1000000001")
                                .balance(2000L)
                                .build()
                );
        given(accountService.getAccountsByUserId(anyLong()))
                .willReturn(accountDtos);
        //when

        //then
        mvc.perform(MockMvcRequestBuilders.get("/account?user_id=1"))
                .andDo(print())
                .andExpect(jsonPath("$[0].accountNumber").value("1000000000"))
                .andExpect(jsonPath("$[0].balance").value(1000L))
                .andExpect(jsonPath("$[1].accountNumber").value("1000000001"))
                .andExpect(jsonPath("$[1].balance").value(2000L));
    }


}