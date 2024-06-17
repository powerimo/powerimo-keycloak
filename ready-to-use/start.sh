# Build image
docker build -t rtu-keycloak .

# Stop and remove existing containers
docker stop rtu-keycloak > null
docker rm rtu-keycloak > null

# Start
docker run --name=rtu-keycloak-instance rtu-keycloak start-dev
