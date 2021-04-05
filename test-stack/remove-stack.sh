#!/bin/bash

if ! sls --version; then
  echo "Please install serverless"
  exit 1
fi

sls remove --aws-profile aws-test -v