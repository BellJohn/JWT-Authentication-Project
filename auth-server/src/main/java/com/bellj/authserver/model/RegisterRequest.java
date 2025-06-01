package com.bellj.authserver.model;

public record RegisterRequest(String username, String password, String firstname, String lastname, String phoneNumber) {
}
