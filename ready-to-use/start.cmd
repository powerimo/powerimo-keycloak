rem Build image
docker build -t rtu-keycloak .

rem Stop and remove existing containers
docker stop rtu-keycloak-instance > null
docker rm rtu-keycloak-instance > null

rem Start as DEV
docker run --name=rtu-keycloak-instance rtu-keycloak start-dev
