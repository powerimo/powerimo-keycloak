# Build image
docker build -t rtu-keycloak .

# Stop and remove existing containers
docker stop rtu-keycloak-instance >> /dev/null
docker rm rtu-keycloak-instance >> /dev/null

# Start
docker run --name=rtu-keycloak-instance \
  -e KEYCLOAK_ADMIN=admin \
  -e KEYCLOAK_ADMIN_PASSWORD=admin \
  -v $(pwd)/mq-config:/etc/mq-sender \
  -v $(pwd)/realm-import:/opt/keycloak/data/import \
  -p 1000:8080 \
  rtu-keycloak start-dev --import-realm
