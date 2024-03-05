package com.hudyma.CarRental2024.service;

import com.hudyma.CarRental2024.model.Transaction;
import com.hudyma.CarRental2024.model.User;
import com.hudyma.CarRental2024.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalTime;

@Service
@RequiredArgsConstructor
@Log4j2
public class TransactionService {

    private final TransactionRepository transactionRepository;

    public Transaction addTransaction (Transaction transaction,
                                       String action, User user, Double balance){
        LocalDate updateDate = user.getUpdateDate().toLocalDate();
        LocalTime localTime = user.getUpdateDate().toLocalTime();
        String updateTime = localTime.getHour() + ":"+ localTime.getMinute() + ":"+localTime.getSecond();
        String txBodyCommonPart = " ::: €" + balance + " ::: " + updateDate + " ::: " + updateTime;
        transaction.setUser(user);
        switch (action){
            case "top-up" ->  transaction.setBody("[+] (поповнення балансу)" + txBodyCommonPart);
            case "refund"-> transaction.setBody("[+] (повернення коштів)" + txBodyCommonPart);
            case "refund-deposit" -> transaction.setBody("[+] (повернення депозиту)" + txBodyCommonPart);
            case "order"  ->  transaction.setBody("[-] (оплата замовлення)" + txBodyCommonPart);
            case "pay-full" -> transaction.setBody("[-] (доплата замовлення)" + txBodyCommonPart);
            default -> {
                log.error ("...add Tx: unknown action parameter");
                throw new IllegalArgumentException();
            }
        }
        log.info("...transaction: "+action+" complete");
        transactionRepository.save(transaction);
        return transaction;
    }
}
