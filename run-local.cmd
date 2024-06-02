call build-local.cmd

pushd keycloak-dev-stack

docker-compose down > nul

docker-compose up -d

popd

docker ps