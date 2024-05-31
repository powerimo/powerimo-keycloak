call build-local.cmd

docker stop local-keycloak > nul
docker rm local-keycloak > nul

docker run --name=local-keycloak \
    -p 1000:8080 \
    -e spi-mq-sender-config-file=/etc/keycloak-mq-config/config.yaml \
    -v dev-image:/etc/mq-sender \
    -v dev-image:/opt/keycloak/data/import \
    local-keycloak start-dev --import-realm