package com.example.backend.payment.util;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

public final

class StripeUtils {

private StripeUtils() {}

public static long amountToCents(BigDecimal amount) {
        Objects.requireNonNull(amount, "amount must not be null");
        BigDecimal cents = amount.setScale(2, RoundingMode.HALF_UP).multiply(new BigDecimal(100));
        return cents.longValueExact();
    }
}
