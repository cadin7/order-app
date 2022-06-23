package group.rohlik.order.exceptions;

public class PaymentStatusException extends RuntimeException {

    public PaymentStatusException(String message) {
        super(message);
    }
}
