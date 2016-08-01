#!/bin/bash
cat <<EOF
FROM $BASE_IMAGE
MAINTAINER Rybek Rbkyev <r.rbkyev@rbkmoney.com>
COPY examples/dummy_service.sh /opt/dummy_service/bin/dummy_service.sh
CMD /opt/dummy_service/bin/dummy_service.sh foreground
LABEL base_image_version=$BASE_IMAGE_TAG
LABEL build_image_version=$BUILD_IMAGE_TAG
LABEL service_commit=$(git rev-parse HEAD)
LABEL service_branch=$(git rev-parse --abbrev-ref HEAD)
LABEL service_commit_number=$(git rev-list --count HEAD)
WORKDIR /opt
EOF

