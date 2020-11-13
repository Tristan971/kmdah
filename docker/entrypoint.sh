#!/usr/bin/env bash

set -x
exec java \
  "-Dspring.config.additional-location=file:$KMDAH_CONFIG_DIR" \
  "-Dspring.profiles.active=prod" \
  -jar "$JARFILE"
