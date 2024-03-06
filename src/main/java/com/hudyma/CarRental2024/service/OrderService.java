package com.hudyma.CarRental2024.service;

import com.hudyma.CarRental2024.constants.OrderStatus;
import com.hudyma.CarRental2024.exception.CarNotAvailableException;
import com.hudyma.CarRental2024.exception.OrderPaymentFailureException;
import com.hudyma.CarRental2024.model.Car;
import com.hudyma.CarRental2024.model.Order;
import com.hudyma.CarRental2024.model.Transaction;
import com.hudyma.CarRental2024.model.User;
import com.hudyma.CarRental2024.repository.CarRepository;
import com.hudyma.CarRental2024.repository.OrderRepository;
import com.hudyma.CarRental2024.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;

import static com.hudyma.CarRental2024.controller.OrderController.ORDER;


@Service
@RequiredArgsConstructor
@Log4j2
public class OrderService {

    private static final Double CAR_DEPOSIT = 1000d, AUX_PAYMENT = 10d;
    private final CarRepository carRepository;
    private final UserRepository userRepository;
    private final OrderRepository orderRepository;
    private final TransactionService transactionService;

    @Transactional
    public boolean processOrderPayment(Order order, Long carId,
                                       Long userId, Integer paymentId,
                                       boolean auxNeeded) {
        Double orderAmount = calculateOrderAmount(order, carId);
        Transaction transaction = new Transaction();
        log.info("...calculated order amount {}", orderAmount);
        User user = userRepository.findById(userId).orElseThrow();
        Double userBalance = user.getBalance();
        Double auxPayment = auxNeeded ? estimateAuxPayment(order) : 0d;
        log.info("...calculated aux payment = {}", auxPayment);
        switch (paymentId) {
            case 30 -> {
                Double deductible = orderAmount * paymentId / 100;
                Double overallPaymentDeducted = CAR_DEPOSIT + deductible + auxPayment;
                if (checkLowBalance(userBalance, overallPaymentDeducted)) return false;
                order.setRentalPayment(doubleRound(deductible));
                log.info("....order payment registered = {}", deductible);
                Double withdrawalAmount = doubleRound(deductible + CAR_DEPOSIT);
                Double newBalanceAmount = doubleRound(userBalance - withdrawalAmount);
                user.setBalance(newBalanceAmount);
                transactionService.addTransaction(transaction, ORDER,
                        user, doubleRound(deductible + CAR_DEPOSIT));
                log.info("....user balance set = {}", withdrawalAmount);
                order.setStatus(OrderStatus.CONFIRMED);
                order.setDeposit(CAR_DEPOSIT);
                log.info("....order deposit SET {}", CAR_DEPOSIT);
                updateCarAvailability(OrderStatus.CONFIRMED, carId);
            }
            case 100 -> {
                Double overallPaymentDeducted = orderAmount + CAR_DEPOSIT / 2 + auxPayment;
                if (checkLowBalance(userBalance, overallPaymentDeducted)) return false;
                order.setRentalPayment(doubleRound(orderAmount));
                log.info("....order payment registered = {}", orderAmount);
                Double withdrawalAmount = doubleRound(orderAmount + CAR_DEPOSIT / 2);
                user.setBalance(userBalance - withdrawalAmount);
                log.info("....order deposit SET {}", CAR_DEPOSIT / 2);
                transactionService.addTransaction(transaction, ORDER,
                        user, withdrawalAmount);
                order.setStatus(OrderStatus.PAID);
                order.setDeposit(CAR_DEPOSIT / 2);
                updateCarAvailability(OrderStatus.PAID, carId);
            }
            default -> {
                log.error("...unknown paymentId parameter");
                throw new OrderPaymentFailureException();
            }
        }
        user.addTransaction(transaction);
        order.setAuxPayment(auxPayment);
        log.info("....aux payment {} set", auxPayment);
        order.setPaymentDate(LocalDateTime.now());
        return true;
    }

    public boolean estimateOrderPayment(Order order, Integer paymentId,
                                        boolean auxNeeded, Long carId, HttpServletRequest req) {
        Double orderAmount = calculateOrderAmount(order, carId);
        if (orderAmount == 0d) {
            log.error("...estimateOrderPayment :: computed amount is 0");
            return false;
        }
        log.info("...estimated order amount is {}", orderAmount);
        Double paymentDeductible = doubleRound(paymentId == 30 ? orderAmount * 0.3 : orderAmount);
        Double deposit = paymentId == 30 ? CAR_DEPOSIT : CAR_DEPOSIT / 2d;
        log.info("...user estimates {} % payment", paymentId);
        log.info("...estimated deductible is {}", paymentDeductible);
        Double auxPayment = auxNeeded ? estimateAuxPayment(order) : 0d;
        log.info("...auxNeeded is {}", auxNeeded);
        log.info("...estimated auxPayment is {}", auxPayment);
        Car car = carRepository.findById(carId).orElseThrow();
        Double price = car.getPrice();
        log.info("...setting car {} for checkout", car.getModel());
        Long duration = calculateDuration(order);
        log.info("...duration == {}" + " days", duration);
        Map.of("auxPayment", auxPayment,
                        "deposit", deposit,
                        "deductible", paymentDeductible,
                        "orderDateBegin", order.getDateBegin(),
                        "orderDateEnd", order.getDateEnd(),
                        "auxNeeded", auxNeeded,
                        "carId", carId,
                        "paymentId", paymentId,
                        "duration", duration,
                        "price", price)
                .forEach((k, v) -> req.getSession().setAttribute(k, v));
        return true;

        //"carModel", car.getModel(),
    }

    public boolean setOrder(Order order, Long carId, Long userId) {
        Double setOrderAmount = calculateOrderAmount(order, carId);
        if (setOrderAmount == 0d) {
            log.error("...set order: computed amount is 0");
            return false;
        }
        order.setAmount(setOrderAmount);
        if (order.getAuxNeeded() == null) order.setAuxNeeded(false);
        if (order.getStatus() == null) order.setStatus(OrderStatus.REQUESTED);
        Car car = carRepository.findById(carId).orElseThrow();
        log.info("...setting car " + carId + " to order");
        order.setCar(car);
        User user = userRepository.findById(userId).orElseThrow();
        order.setUser(user);
        if (!user.getOrderList().contains(order)) {
            user.addOrder(order);
            log.info("...adding order to user {}", userId);
        } else {
            user.updateOrder(order);
            log.info("order {} exists, updating one", order.getId());
        }
        return true;
    }

    public void validateUserOrders(Long userId) {
        List<Order> expiredUserOrders = orderRepository.findAllExpiredOrders(userId);
        if (!expiredUserOrders.isEmpty()) {
            expiredUserOrders.forEach(order -> {
                order.setStatus(OrderStatus.DECLINED);
                orderRepository.save(order);
            });
            log.info("...expired orders found, deemed {}", OrderStatus.DECLINED);
        }
    }

    private Double estimateAuxPayment(Order order) {
        Long days = calculateDuration(order);
        if (days == null) return 0d;
        return doubleRound(AUX_PAYMENT * days);
    }

    public Double doubleRound(Double deductible) {
        return Math.round(deductible * 100d) / 100d;
    }

    public boolean checkLowBalance(Double userBalance, Double overallPaymentDeducted, HttpServletRequest req) {
        if (checkLowBalance(userBalance, overallPaymentDeducted)) {
            req.getSession().setAttribute("insufficient",
                    doubleRound(overallPaymentDeducted - userBalance));
            return true;
        }
        return false;
    }

    private boolean checkLowBalance(Double userBalance, Double overallPaymentDeducted) {
        if (userBalance < overallPaymentDeducted) {
            log.error(".... low balance = {}, while deducted to pay = {}",
                    userBalance, overallPaymentDeducted);
            return true;
        }
        return false;
    }

    @Transactional
    public void updateCarAvailability(OrderStatus status, Long carId) {
        switch (status) {
            case COMPLETE -> {
                carRepository.incrementCarAvailable(carId);
                log.info("....car {} total incremented", carId);
            }
            case PAID -> {
                checkCarAvailability(carId); // double check before going into minus
                carRepository.decrementCarAvailable(carId);
                log.info("....car {} available total decremented", carId);
            }
            default -> log.error("{}, car = {} availability NOT changed", status, carId);
        }
    }

    private void checkCarAvailability(Long carId) {
        Car car = carRepository.findById(carId).orElseThrow();
        if (car.getAvailable() == 0) {
            log.error("... car {} is not available", carId);
            throw new CarNotAvailableException("car " + carId + " is not available");
        }
    }

    public String getAllOrdersAmount() {
        Double result = orderRepository
                .findAll()
                .stream()
                .map(Order::getAmount)
                .reduce(Double::sum)
                .orElse(0d);
        double res = Math.round(result * 100d) / 100d;
        return formatDecimalNum(res);
    }

    public String formatDecimalNum(double res) {
        DecimalFormatSymbols symbols = new DecimalFormatSymbols();
        symbols.setGroupingSeparator(' ');
        DecimalFormat dfDecimal = new DecimalFormat("###########0.00###");
        dfDecimal.setDecimalFormatSymbols(symbols);
        dfDecimal.setGroupingSize(3);
        dfDecimal.setGroupingUsed(true);
        return dfDecimal.format(res);
    }

    public List<Order> getOrdersByUserId(Long id) {
        return orderRepository.findAllByUserId(id);
    }

    public List<Order> getAllOrders() {
        return orderRepository.findAll();
    }

    public List<Order> getAllOrdersSortedByFieldAsc(String sortField) {
        return orderRepository.findAll(Sort.by(
                Sort.Direction.ASC, sortField));
    }

    public List<Order> getAllOrdersSortedByFieldDesc(String sortField) {
        return orderRepository.findAll(Sort.by(
                Sort.Direction.DESC, sortField));
        //todo implement
    }


    public List<Order> getOrdersByCarId(Long id) {
        return orderRepository.findAllByCarId(id);
    }

    private Double calculateOrderAmount(Order order, Long carId) {
        Long days = calculateDuration(order);
        if (days == null) return 0d;
        order.setDuration(days);
        Double price = carRepository.findById(carId)
                .orElseThrow()
                .getPrice();
        return doubleRound(days * price);
    }

    public Long calculateDuration(Order order) {
        long days = ChronoUnit.DAYS.between(
                order.getDateBegin(),
                order.getDateEnd());
        if (days <= 0) {
            log.info("...DATES OF RENTAL DIFFER BY " + days + " days");
            return null;
        }
        return days;
    }

    public void recalculateOrdersAmountUponCarEdit(Long carId) {
        List<Order> orderListByCarId = orderRepository.findAllByCarId(carId);
        orderListByCarId.forEach(order -> {
            order.setAmount(calculateOrderAmount(
                    order, order.getCar().getId()));
            orderRepository.save(order);
        });
    }
}
