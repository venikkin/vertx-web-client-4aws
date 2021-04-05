#!/bin/bash

if ! go version; then
  echo "Please install go"
  exit 1
fi
if ! sls --version; then
  echo "Please install serverless"
  exit 1
fi

env GOOS=linux GOARCH=amd64 go build -o bin/api api/main.go

sls deploy --aws-profile aws-test -v
