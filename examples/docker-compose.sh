#!/bin/bash
cat <<EOF
version: '2'

services:
  ${SERVICE_NAME}:
    image: dr.rbkmoney.com/rbkmoney/build:${BUILD_IMAGE_TAG}
    volumes:
      - .:/code
    working_dir: /code
    command: /sbin/init
#    links:
#      - some_required_service
#
#  some_required_service:
#    image: dr.rbkmoney.com/rbkmoney/some_service:${SOME_SERVICE_IMAGE_TAG}
#    command: /opt/some_service/bin/some_service start

networks:
  default:
    driver: bridge
    driver_opts:
      com.docker.network.enable_ipv6: "true"
      com.docker.network.bridge.enable_ip_masquerade: "false"
EOF

