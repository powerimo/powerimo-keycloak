package org.powerimo.keycloak.provider;

public class PowerimoKeycloakProviderException extends RuntimeException{
    public PowerimoKeycloakProviderException() {
        super();
    }

    public PowerimoKeycloakProviderException(Throwable cause) {
        super(cause);
    }

    public PowerimoKeycloakProviderException(String message) {
        super(message);
    }

    public PowerimoKeycloakProviderException(String message, Throwable cause) {
        super(message, cause);
    }
}
