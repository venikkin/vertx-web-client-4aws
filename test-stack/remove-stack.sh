#!/bin/bash

set -e

if ! npx sls --version; then
  echo "Please install serverless"
  exit 1
fi

npx sls remove --verbose --aws-profile aws-test