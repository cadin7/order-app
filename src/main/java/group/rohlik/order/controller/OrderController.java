package group.rohlik.order.controller;

import group.rohlik.order.model.api.Order;
import group.rohlik.order.model.api.PaymentStatus;
import group.rohlik.order.model.mapper.OrderMapper;
import group.rohlik.order.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("orders")
public class OrderController {

    private final OrderMapper mapper;
    private final OrderService service;

    @PostMapping
    ResponseEntity<Object> createOrder(@RequestBody Order order) {
        return service.createOrder(
                mapper.toEntity(order));
    }

    @DeleteMapping("{orderId}")
    Order deleteOrder(@PathVariable String orderId) {
        return mapper.toApi(
                service.deleteOrder(orderId));
    }

    @PutMapping("{orderId}")
    Order updatePayment(@PathVariable String orderId, @RequestParam PaymentStatus paymentStatus) {
        return mapper.toApi(
                service.updatePayment(orderId, paymentStatus));
    }
}
