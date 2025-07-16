package com.cpsoneghett.codingtask.exception.handler;

public enum ProblemType {

    SYSTEM_ERROR("/error", "System error."),
    INVALID_PARAMETER("/invalid-parameter", "Invalid parameter."),
    INCOMPREHENSIVE_MESSAGE("/incomprehensive-message", "Incomprehensive message."),
    RESOURCE_NOT_FOUND("/resource-not-found", "Resource not found."),
    ENTITY_IN_USE("/entity-in-use", "Entity already in use."),
    BUSINESS_ERROR("/business-error", "Business rule violation.");

    private final String title;
    private final String uri;

    ProblemType(String path, String title) {
        this.uri = path;
        this.title = title;
    }

    public String getTitle() {
        return title;
    }

    public String getUri() {
        return uri;
    }
}
