#!/usr/bin/env bash

VERSION=$1
if [ -z "$VERSION" ]; then
  echo "Usage: build-images [version]"
  exit 1
fi

echo "Building as $VERSION"

echo "Building parent image"
docker build . -f docker/kmdah-parent.dockerfile -t "tristandeloche/kmdah-parent:$VERSION"

echo "Building operator image"
docker build . -f docker/kmdah-operator.dockerfile -t "tristandeloche/kmdah-operator:$VERSION"

echo "Building worker image"
docker build . -f docker/kmdah-worker.dockerfile -t "tristandeloche/kmdah-worker:$VERSION"
