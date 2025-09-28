#!/usr/bin/env bash
set -euo pipefail

# NOTE for PKCS12 keystores, the store pass and key pass will be the same
: "${PASSWORD:?Environment variable PASSWORD must be set}"

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_DIR="$(dirname "$SCRIPT_DIR")"

docker run --rm \
  -v "$PROJECT_DIR":"$PROJECT_DIR" \
  --workdir "$PROJECT_DIR" \
  docker.io/cimg/android:2025.09 \
  keytool -genkeypair -v \
    -keystore "$SCRIPT_DIR/hotosm.jks" \
    -alias hotosm-android \
    -keyalg RSA \
    -keysize 2048 \
    -validity 10000 \
    -storepass "$PASSWORD" \
    -keypass "$PASSWORD" \
    -dname "CN=HOT, OU=Dev, O=Humanitarian OpenStreetMap Team, L=Global, ST=Earth, C=US"
