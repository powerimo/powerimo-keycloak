package org.powerimo.keycloak;

public interface MessageSerializer {
    String serialize(Object obj);
    <T> T deserialize(String str, Class<T> clazz);

    KcEvent deserializeEvent(String s);
}
