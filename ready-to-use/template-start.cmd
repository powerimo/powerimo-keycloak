rem Build image
docker build -t rtu-keycloak .

rem Stop and remove existing containers
docker stop rtu-keycloak-instance >> null
docker rm rtu-keycloak-instance >> null

rem Start as DEV
docker run --name=rtu-keycloak-instance ^
    -e KEYCLOAK_ADMIN=admin ^
    -e KEYCLOAK_ADMIN_PASSWORD=admin ^
    -v %cd%/mq-config:/etc/mq-sender ^
    -v %cd%/realm-import:/opt/keycloak/data/import ^
    -p 1000:8080 ^
    rtu-keycloak start-dev --import-realm
