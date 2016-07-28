#!/bin/bash
cat <<EOF
FROM ${BASE_IMAGE}
MAINTAINER Rybek Rbkyev <r.rbkyev@rbkmoney.com>
COPY ./artifact/service-rel /opt/service
CMD /opt/service/bin/service start
LABEL service_version=$(git rev-parse --short HEAD)
LABEL base_image_version=${BASE_IMAGE_TAG}
LABEL build_image_version=${BUILD_IMAGE_TAG}
WORKDIR /opt/service
EOF

