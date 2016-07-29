#!/bin/bash
cat <<EOF
FROM $BASE_IMAGE
MAINTAINER Rybek Rbkyev <r.rbkyev@rbkmoney.com>
COPY examples/dummy_service.sh /opt/dummy_service/bin/dummy_service.sh
CMD /opt/dummy_service/bin/dummy_service.sh foreground
LABEL service_version=$(git rev-parse --short HEAD)
LABEL base_image_version=$BASE_IMAGE_TAG
LABEL build_image_version=$BUILD_IMAGE_TAG
WORKDIR /opt
EOF

