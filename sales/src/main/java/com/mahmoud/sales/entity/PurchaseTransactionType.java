package com.mahmoud.sales.entity;

/**
 * Enum representing the allowed purchase transaction types.
 */
public enum PurchaseTransactionType {
    INVOICE("فاتورة شراء"),
    RETURN("مرتجع شراء");

    // Arabic display value
    private final String arabicValue;

    PurchaseTransactionType(String arabicValue) {
        this.arabicValue = arabicValue;
    }

    public String getArabicValue() {
        return arabicValue;
    }

    /**
     * Returns a PurchaseTransactionType based on the Arabic value.
     * Throws an exception if no match is found.
     */
    public static PurchaseTransactionType fromArabicValue(String value) {
        for (PurchaseTransactionType type : PurchaseTransactionType.values()) {
            if (type.getArabicValue().equals(value)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Invalid purchase transaction type: " + value);
    }
}