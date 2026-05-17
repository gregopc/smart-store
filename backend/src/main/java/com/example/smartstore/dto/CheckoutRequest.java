package com.example.smartstore.dto;

import lombok.Data;

@Data
public class CheckoutRequest {
    private String paymentMethod;
    private ShippingAddress shippingAddress;

    @Data
    public static class ShippingAddress {
        private String street;
        private String city;
        private String state;
        private String zip;
        private String country;
    }
}
