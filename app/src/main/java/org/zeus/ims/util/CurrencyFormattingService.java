package org.zeus.ims.util;

import org.springframework.stereotype.Component;

import java.text.NumberFormat;
import java.util.Currency;
import java.util.Locale;

@Component("currencyFormattingService")
public class CurrencyFormattingService {

    private static final Locale DEFAULT_LOCALE = Locale.forLanguageTag("en-IN");
    private static final Currency DEFAULT_CURRENCY = Currency.getInstance("INR");

    public String formatCurrency(Number amount) {
        NumberFormat formatter = NumberFormat.getCurrencyInstance(DEFAULT_LOCALE);
        formatter.setCurrency(DEFAULT_CURRENCY);
        Number safeAmount = amount == null ? 0 : amount;
        return formatter.format(safeAmount.doubleValue());
    }
}

