call build-local.cmd

echo "Current directory: %cd%"

pushd keycloak-dev-stack

docker-compose down > nul
docker-compose up -d

popd
cd bin

docker ps