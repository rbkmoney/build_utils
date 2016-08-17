#!/bin/bash
cat <<EOF
FROM $BASE_IMAGE
MAINTAINER Rybek Rbkyev <r.rbkyev@rbkmoney.com>
COPY examples/dummy_service.sh /opt/dummy_service/bin/dummy_service.sh
CMD /opt/dummy_service/bin/dummy_service.sh foreground
EXPOSE 8022
LABEL com.rbkmoney.base_image_tag=$BASE_IMAGE_TAG
LABEL com.rbkmoney.build_image_tag=$BUILD_IMAGE_TAG
# A bit of magic to get a proper branch name
# even when the HEAD is detached (Hey Jenkins!
# BRANCH_NAME is available in Jenkins env).
LABEL com.rbkmoney.branch=$( \
  if [ "HEAD" != $(git rev-parse --abbrev-ref HEAD) ]; then \
    echo $(git rev-parse --abbrev-ref HEAD); \
  elif [ -n "$BRANCH_NAME" ]; then \
    echo $BRANCH_NAME; \
  else \
    echo $(git name-rev --name-only HEAD); \
  fi)
LABEL com.rbkmoney.commit=$(git rev-parse HEAD)
LABEL com.rbkmoney.commit_number=$(git rev-list --count HEAD)
WORKDIR /opt
EOF

