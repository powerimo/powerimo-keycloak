package org.powerimo.keycloak;

public class KcUtils {

    public static String loadResource(String resourceName) {
        try {
            try (var stream = KcUtils.class.getClassLoader().getResourceAsStream(resourceName)) {
                assert stream != null;
                var buffer = stream.readAllBytes();
                return new String(buffer);
            }
        } catch (Throwable ex) {
            throw new PowerimoKeycloakException("Exception on reading from the stream", ex);
        }
    }

}
