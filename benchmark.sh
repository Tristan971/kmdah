#!/usr/bin/env bash

set -x
siege -c40 -r20 -d1 -H 'Host: mangadex.network' https://localhost:8080/data/a61fa9f7f1313194787116d1357a7784/N9.jpg
