#!/usr/bin/env bash

java -Dspring.config.additional-location=file:"${KMDAH_CONFIG_FILE}" -jar "${KMDAH_JARFILE}"
