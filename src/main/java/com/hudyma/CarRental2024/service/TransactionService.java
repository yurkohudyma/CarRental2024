package com.hudyma.CarRental2024.service;

import com.hudyma.CarRental2024.model.Transaction;
import com.hudyma.CarRental2024.model.User;
import com.hudyma.CarRental2024.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

@Service
@RequiredArgsConstructor
@Log4j2
public class TransactionService {

    private final TransactionRepository transactionRepository;

    public Transaction addTransaction(Transaction transaction,
                                      String action, User user, Double balance) {
        String dateTimeStamp = " ::: " + LocalDate.now()
                .format(DateTimeFormatter.ofPattern("dd.MM.yyyy")) + " ::: " + LocalTime.now()
                .format(DateTimeFormatter.ofPattern("HH:mm:ss"));
        transaction.setUser(user);
        switch (action) {
            case "top-up" -> transaction.setBody("[+ €" + balance + "] -> поповнення балансу" + dateTimeStamp);
            case "refund" -> transaction.setBody("[+ €" + balance + "] -> повернення коштів" + dateTimeStamp);
            case "refund-deposit" -> transaction.setBody("[+ €" + balance + "] ->  повернення депозиту" + dateTimeStamp);
            case "order" -> transaction.setBody("[- €" + balance + "] -> оплата замовлення" + dateTimeStamp);
            case "pay-full" -> transaction.setBody("[- €" + balance + "] -> доплата замовлення" + dateTimeStamp);
            default -> {
                log.error("...add Tx: unknown action parameter");
                throw new IllegalArgumentException();
            }
        }
        log.info("...transaction: " + action + " complete");
        transactionRepository.save(transaction);
        return transaction;
    }
}
