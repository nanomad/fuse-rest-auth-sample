package com.redhat.consulting.demos.entity;

public class NotAuthenticatedResponseEntity {
    private final String error = "Not authenticated";

    public String getError() {
        return error;
    }
}
