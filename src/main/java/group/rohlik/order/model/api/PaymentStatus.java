package group.rohlik.order.model.api;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Optional;
import java.util.stream.Stream;

public enum PaymentStatus {

    @JsonProperty("pending")
    PENDING,

    @JsonProperty("cancelled")
    CANCELLED,

    @JsonProperty("completed")
    COMPLETED;

    public static Optional<PaymentStatus> of(String paymentStatus) {
        return Stream.of(values())
                .filter(val -> val.name().equalsIgnoreCase(paymentStatus))
                .findFirst();
    }
}
