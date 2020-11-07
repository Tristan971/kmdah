#!/usr/bin/env bash

set -x

java \
  -Dspring.profiles.active=production \
  -Dspring.config.additional-location=file:"${KMDAH_CONFIG_DIR}" \
  -jar "${KMDAH_JARFILE}"
