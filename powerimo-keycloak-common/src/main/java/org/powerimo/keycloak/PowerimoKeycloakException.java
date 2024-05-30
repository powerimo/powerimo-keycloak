package org.powerimo.keycloak;

public class PowerimoKeycloakException extends RuntimeException {
    public PowerimoKeycloakException() {
        super();
    }

    public PowerimoKeycloakException(Throwable cause) {
        super(cause);
    }

    public PowerimoKeycloakException(String message) {
        super(message);
    }

    public PowerimoKeycloakException(String message, Throwable cause) {
        super(message, cause);
    }
}
