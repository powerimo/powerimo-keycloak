# Interim image for using `wget`
FROM alpine:3 AS downloader

# Installing wget and unzip
RUN apk add --no-cache wget unzip

# Download release and unzip
RUN mkdir -p /tmp/providers \
    && wget -O /tmp/powerimo-providers.zip https://github.com/powerimo/powerimo-keycloak/releases/download/1.1.0/powerimo-keycloak-providers-1.1.0.zip \
    && unzip /tmp/powerimo-providers.zip -d /tmp/providers

# The base image of keycloak
FROM quay.io/keycloak/keycloak:24.0.4 AS base

# Copy provider files from the interim container to Keycloak
COPY --from=downloader /tmp/providers/* /opt/keycloak/providers/

EXPOSE 8080

RUN /opt/keycloak/bin/kc.sh build

ENTRYPOINT ["/opt/keycloak/bin/kc.sh"]
