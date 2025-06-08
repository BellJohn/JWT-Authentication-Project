package com.bellj.authserver.model;

/** HTTP Request Object for Registration Requests */
public record RegisterRequest(
    String username, String password, String firstname, String lastname, String phoneNumber) {}
