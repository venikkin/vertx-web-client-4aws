#!/bin/bash

set -e

if ! go version; then
  echo "Please install go"
  exit 1
fi
if ! npx sls --version; then
  echo "Please install serverless"
  exit 1
fi

env GOOS=linux GOARCH=amd64 go build -o bin/api api/main.go

npx sls deploy --verbose --aws-profile aws-test
