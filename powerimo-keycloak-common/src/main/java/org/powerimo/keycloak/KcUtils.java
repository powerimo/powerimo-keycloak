package org.powerimo.keycloak;

/**
 * Additional functions
 */
public class KcUtils {

    /**
     * Load the resource from the package resources as String
     * @param resourceName name of the resource
     * @return resource as String
     */
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
