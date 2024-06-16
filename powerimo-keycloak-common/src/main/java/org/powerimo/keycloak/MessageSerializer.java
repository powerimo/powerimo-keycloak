package org.powerimo.keycloak;

/**
 * Event serializer\deserializer interface
 */
public interface MessageSerializer {

    /**
     * Serialize object to the array of bytes
     * @param obj object for the serialization
     * @return array of bytes
     */
    byte[] serialize(Object obj);

    /**
     * Deserialize String to typed object.
     * @param str source string
     * @param clazz class for deserialization
     * @return deserialized object
     * @param <T> class of deserialized object
     */
    <T> T deserialize(String str, Class<T> clazz);

    /**
     * Deserialize String to KcEvent
     * @param s source string
     * @return event
     */
    KcEvent deserializeEvent(String s);
}
