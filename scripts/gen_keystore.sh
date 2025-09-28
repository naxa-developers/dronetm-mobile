#!/usr/bin/env bash
set -euo pipefail

# NOTE for PKCS12 keystores, the store pass and key pass will be the same
: "${PASSWORD:?Environment variable PASSWORD must be set}"

keytool -genkeypair -v \
  -keystore scripts/hotosm.jks \
  -alias hotosm-android \
  -keyalg RSA \
  -keysize 2048 \
  -validity 10000 \
  -storepass "$PASSWORD" \
  -keypass "$PASSWORD" \
  -dname "CN=HOT, OU=Dev, O=Humanitarian OpenStreetMap Team, L=Global, ST=Earth, C=US"
