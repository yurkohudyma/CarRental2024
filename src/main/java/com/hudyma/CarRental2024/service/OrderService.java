package com.hudyma.CarRental2024.service;

import com.hudyma.CarRental2024.constants.OrderStatus;
import com.hudyma.CarRental2024.exception.CarNotAvailableException;
import com.hudyma.CarRental2024.model.Car;
import com.hudyma.CarRental2024.model.Order;
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
import java.time.temporal.ChronoUnit;
import java.util.List;


@Service
@RequiredArgsConstructor
@Log4j2
public class OrderService {

    private static final Double CAR_DEPOSIT = 1000d, AUX_PAYMENT = 10d;
    private final CarRepository carRepository;
    private final UserRepository userRepository;
    private final OrderRepository orderRepository;

    @Transactional
    public boolean calculateOrderPayment(Order order, Long carId, Long userId, Integer paymentId) {
        Double orderAmount = calculateOrderAmount(order, carId);
        User user = userRepository.findById(userId).orElseThrow();
        Double userBalance = user.getBalance();
        log.info("...calculated order amount {}", orderAmount);
        switch (paymentId) {
            case 30 -> {
                Double deductible = orderAmount * paymentId / 100;
                Double overallPaymentDeducted = CAR_DEPOSIT + deductible;
                if (!checkBalance(userBalance, overallPaymentDeducted)) return false;
                order.setRentalPayment(doubleRound(deductible));
                log.info("....order payment registered = {}", deductible);
                Double withdrawalAmount = userBalance - deductible - CAR_DEPOSIT;
                user.setBalance(doubleRound(withdrawalAmount));
                log.info("....user balance set = {}", withdrawalAmount);
                order.setStatus(OrderStatus.CONFIRMED);
                order.setDeposit(CAR_DEPOSIT);
                log.info("....order deposit SET {}", CAR_DEPOSIT);
                updateCarAvailabilityNumber(OrderStatus.CONFIRMED, carId);
                log.info("....car available num decremented");
                return true;
            }
            case 100 -> {
                Double overallPaymentDeducted = orderAmount + CAR_DEPOSIT / 2;
                if (!checkBalance(userBalance, overallPaymentDeducted)) return false;
                order.setRentalPayment(doubleRound(orderAmount));
                log.info("....order payment registered = {}", orderAmount);
                user.setBalance(doubleRound(userBalance - orderAmount - CAR_DEPOSIT / 2));
                log.info("....order deposit SET {}", CAR_DEPOSIT / 2);
                order.setStatus(OrderStatus.PAID);
                order.setDeposit(CAR_DEPOSIT / 2);
                updateCarAvailabilityNumber(OrderStatus.PAID, carId);
                log.info("....car available num decremented");
                return true;
            }
            default -> log.info("...unknown paymentId parameter");
        }
        orderRepository.save(order);
        return true;
    }

    public boolean estimateOrderPayment(Order order, Integer paymentId,
                                        boolean auxNeeded, Long carId, HttpServletRequest req) {
        Double orderAmount = calculateOrderAmount(order, carId);
        if (orderAmount == 0d) {
            log.error("...set order: computed amount is 0");
            return false;
        }
        log.info("...orderService:: estimated order amount is {}", orderAmount);
        Double paymentDeductible = doubleRound(paymentId == 30 ? orderAmount * 0.3 : orderAmount);
        Double deposit = paymentId == 30 ? CAR_DEPOSIT : CAR_DEPOSIT/2d;
        log.info("...user estimates {} % payment", paymentId);
        log.info("...orderService:: estimated deductible is {}", paymentDeductible);
        Double auxPayment = auxNeeded ? estimateAuxPayment(order) : 0d;
        log.info("...orderService:: estimated auxPayment is {}", auxPayment);
        Car car = carRepository.findById(carId).orElseThrow();
        log.info("...setting car {} for checkout", car.getModel());
        req.getSession().setAttribute("auxPayment", auxPayment);
        req.getSession().setAttribute("deposit", deposit);
        req.getSession().setAttribute("deductible", paymentDeductible);
        req.getSession().setAttribute("orderDateBegin", order.getDateBegin());
        req.getSession().setAttribute("orderDateEnd", order.getDateEnd());
        req.getSession().setAttribute("auxNeeded", auxNeeded);
        req.getSession().setAttribute("carModel", car.getModel());
        req.getSession().setAttribute("paymentId", paymentId);
        return true;
    }

    private Double estimateAuxPayment(Order order) {
        long days = ChronoUnit.DAYS.between(
                order.getDateBegin(),
                order.getDateEnd());
        if (days <= 0) {
            log.info("...DATES OF RENTAL DIFFER BY " + days + " days");
            return 0d;
        }
        return doubleRound(AUX_PAYMENT * days);
    }

    private Double doubleRound(Double deductible) {
        return Math.round(deductible * 100d) / 100d;
    }

    private boolean checkBalance(Double userBalance, Double overallPaymentDeducted) {
        if (userBalance < overallPaymentDeducted) {
            log.error(".... low balance = {}, while deducted to pay = {}",
                    userBalance, overallPaymentDeducted);
            return false;
        }
        return true;
    }

    @Transactional
    public void updateCarAvailabilityNumber(OrderStatus status, Long carId) {
        switch (status) {
            case COMPLETE -> carRepository.incrementCarAvailableWhenOrderComplete(carId);
            case CONFIRMED, PAID -> {
                checkCarAvailability(carId); // double check before going into minus
                carRepository.decrementCarAvailableWhenOrderConfirmed(carId);
            }
            default -> log.error("car {} availability num NOT changed)", carId);
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

    private String formatDecimalNum(double res) {
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

    private Double calculateOrderAmount(Order order, Long carId) {
        long days = ChronoUnit.DAYS.between(
                order.getDateBegin(),
                order.getDateEnd());
        if (days <= 0) {
            log.info("...DATES OF RENTAL DIFFER BY " + days + " days");
            return 0d;
        }
        order.setDuration(days);
        Double price = carRepository.findById(carId)
                .orElseThrow()
                .getPrice();
        return doubleRound(days * price);
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
