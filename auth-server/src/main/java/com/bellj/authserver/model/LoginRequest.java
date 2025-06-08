package com.bellj.authserver.model;

/** Http Request Object for Login Requests */
public record LoginRequest(String username, String password) {}
