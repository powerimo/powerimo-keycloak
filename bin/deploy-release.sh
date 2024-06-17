#!/bin/bash

revision=$1
zip_file="powerimo-keycloak-providers-$revision.zip"
files_to_zip=("../powerimo-keycloak-common/target/powerimo-keycloak-common-$revision.jar" "../powerimo-keycloak-provider/target/powerimo-keycloak-provider-$revision.jar")
repo="powerimo/powerimo-keycloak"

if [ -z "$revision" ]; then
  echo "ERROR: Version is not specified"
  exit 1
fi

cd "$(dirname "$0")"

for file in "${files_to_zip[@]}"; do
  if [ ! -f "$file" ]; then
    echo "ERROR: File $file does not exist"
    exit 1
  fi
done

zip -j $zip_file "${files_to_zip[@]}"

gh release create $revision $zip_file --repo $repo --title "Release $revision" --notes "Powerimo Keycloak Providers"
