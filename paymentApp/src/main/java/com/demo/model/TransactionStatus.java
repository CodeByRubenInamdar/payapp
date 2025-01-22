package com.demo.model;

public enum TransactionStatus {
    SUCCESS, FAILED, UNKNOWN;

    public String toUserFriendlyName() {
        switch (this) {
            case SUCCESS: return "Success";
            case FAILED: return "Failed";
            default: return "Unknown";
        }
    }
}

