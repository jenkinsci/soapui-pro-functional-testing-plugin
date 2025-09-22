package com.smartbear.ready.jenkins;

public enum AuthMethod {

    API_KEY("API KEY"),
    ACCESS_FOR_EVERYONE("Access for everyone"),
    CLIENT_CREDENTIALS("Client Credentials"),
    INVALID("Invalid");

    private final String displayName;

    AuthMethod(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public static AuthMethod getValue(String displayName) {
        for (AuthMethod method : AuthMethod.values()) {
            if (method.getDisplayName().equals(displayName)) {
                return method;
            }
        }
        return INVALID;
    }

}
