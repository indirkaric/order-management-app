#!/bin/sh
aws s3api create-bucket --bucket oms-product-files --endpoint-url=http://aws.oms.local:4566 --region eu-west-2 \
  --create-bucket-configuration LocationConstraint=eu-west-2
