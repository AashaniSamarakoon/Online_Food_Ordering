//package com.example.bankingservice.model;
//
//
//public enum AccountType {
//    CHECKING,
//    SAVINGS,
//    MERCHANT_SERVICES
//}


package com.example.bankingservice.model;

public enum AccountType {
    CHECKING("Checking"),
    SAVINGS("Savings"),
    MERCHANT_SERVICES("Merchant Services");

    private final String displayName;

    AccountType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}