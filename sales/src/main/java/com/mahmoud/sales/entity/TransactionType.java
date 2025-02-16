package com.mahmoud.sales.entity;

/**
 * Enum representing the allowed transaction types.
 */
public enum TransactionType {
    INVOICE("فاتورة"),
    RETURN("مرتجع");

    // Arabic display value
    private final String arabicValue;

    TransactionType(String arabicValue) {
        this.arabicValue = arabicValue;
    }

    public String getArabicValue() {
        return arabicValue;
    }

    /**
     * Returns a TransactionType based on the Arabic value.
     * Throws an exception if no match is found.
     */
    public static TransactionType fromArabicValue(String value) {
        for (TransactionType type : TransactionType.values()) {
            if (type.getArabicValue().equals(value)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Invalid transaction type: " + value);
    }
}
