package com.mycompany.perpustakaan.utils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

public class FineCalculator {

    public static final BigDecimal DEFAULT_DAILY_FINE = new BigDecimal("5000.00");

    private FineCalculator() {
    }

    public static int countLateDays(LocalDate dueDate, LocalDate returnDate) {
        if (dueDate == null || returnDate == null || !returnDate.isAfter(dueDate)) {
            return 0;
        }
        return (int) ChronoUnit.DAYS.between(dueDate, returnDate);
    }

    public static BigDecimal calculateFine(LocalDate dueDate, LocalDate returnDate) {
        return calculateFine(dueDate, returnDate, DEFAULT_DAILY_FINE);
    }

    public static BigDecimal calculateFine(LocalDate dueDate, LocalDate returnDate, BigDecimal dailyFine) {
        int lateDays = countLateDays(dueDate, returnDate);
        return dailyFine.multiply(BigDecimal.valueOf(lateDays));
    }
}
