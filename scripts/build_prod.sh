#!/usr/bin/env bash
set -euo pipefail

: "${PASSWORD:?Environment variable PASSWORD must be set}"

# Get absolute directory of this script
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
KEYSTORE_PATH="${SCRIPT_DIR}/hotosm.jks"

./gradlew assembleDebug

./gradlew assembleRelease \
  -Pandroid.injected.signing.store.file="${KEYSTORE_PATH}" \
  -Pandroid.injected.signing.store.password="${PASSWORD}" \
  -Pandroid.injected.signing.key.alias=hotosm-android \
  -Pandroid.injected.signing.key.password="${PASSWORD}"
