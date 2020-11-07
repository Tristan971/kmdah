#!/usr/bin/env bash

VERSION=$1
if [ -z "$VERSION" ]; then
  echo "Usage: build-images [version]"
  exit 1
fi

echo "Building as $VERSION"

echo "Building parent image"
docker build --build-arg KMDAH_VERSION="$VERSION" . -f kmdah-parent.dockerfile -t "tristandeloche/kmdah-parent:$VERSION"

echo "Building operator image"
docker build . -f kmdah-operator.dockerfile -t "tristandeloche/kmdah-operator:$VERSION"

echo "Building worker image"
docker build . -f kmdah-worker.dockerfile -t "tristandeloche/kmdah-worker:$VERSION"
