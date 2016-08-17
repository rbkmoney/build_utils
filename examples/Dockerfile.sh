#!/bin/bash
cat <<EOF
FROM $BASE_IMAGE
MAINTAINER Rybek Rbkyev <r.rbkyev@rbkmoney.com>
COPY examples/dummy_service.sh /opt/dummy_service/bin/dummy_service.sh
CMD /opt/dummy_service/bin/dummy_service.sh foreground
EXPOSE 8022
# A bit of magic below to get a proper branch name
# even when the HEAD is detached (Hey Jenkins!
# BRANCH_NAME is available in Jenkins env).
LABEL com.rbkmoney.$SERVICE_NAME.parent=$BASE_IMAGE_NAME \
      com.rbkmoney.$SERVICE_NAME.parent_tag=$BASE_IMAGE_TAG \
      com.rbkmoney.$SERVICE_NAME.commit_id=$(git rev-parse HEAD) \
      com.rbkmoney.$SERVICE_NAME.commit_num=$(git rev-list --count HEAD) \
      com.rbkmoney.$SERVICE_NAME.branch=$( \
        if [ "HEAD" != $(git rev-parse --abbrev-ref HEAD) ]; then \
          echo $(git rev-parse --abbrev-ref HEAD); \
        elif [ -n "$BRANCH_NAME" ]; then \
          echo $BRANCH_NAME; \
        else \
          echo $(git name-rev --name-only HEAD); \
        fi)
WORKDIR /opt
EOF
