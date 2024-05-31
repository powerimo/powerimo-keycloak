FROM quay.io/keycloak/keycloak:24.0.4 as base

COPY ./powerimo-keycloak-provider/target/*.jar /opt/keycloak/providers/

EXPOSE 8080

ENV KEYCLOAK_ADMIN=admin
ENV KEYCLOAK_ADMIN_PASSWORD=admin

RUN /opt/keycloak/bin/kc.sh build

ENTRYPOINT ["/opt/keycloak/bin/kc.sh"]