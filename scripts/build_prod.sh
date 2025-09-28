#!/usr/bin/env bash
set -euo pipefail

: "${PASSWORD:?Environment variable PASSWORD must be set}"

# Resolve dirs
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_DIR="$(dirname "$SCRIPT_DIR")"
KEYSTORE_PATH="${SCRIPT_DIR}/hotosm.jks"

docker run --rm \
  -v "$PROJECT_DIR":"$PROJECT_DIR" \
  --workdir "$PROJECT_DIR" \
  docker.io/cimg/android:2025.09 \
  bash -c "
    ./gradlew assembleDebug &&
    ./gradlew assembleRelease \
      -Pandroid.injected.signing.store.file='${KEYSTORE_PATH}' \
      -Pandroid.injected.signing.store.password='${PASSWORD}' \
      -Pandroid.injected.signing.key.alias=hotosm-android \
      -Pandroid.injected.signing.key.password='${PASSWORD}'
  "
