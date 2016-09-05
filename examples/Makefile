## Mandatory variables

# For convenience (specific for this repo)
# BASE_PATH is defined in the top Makefile
UTILS_PATH := $(BASE_PATH)
TEMPLATES_PATH := $(BASE_PATH)/examples

# Name of the service
SERVICE_NAME := build_utils_demo


## Variables required for utils_image.mk

# Service image default tag
SERVICE_IMAGE_TAG ?= $(shell git rev-parse HEAD)
# The tag for service image to be pushed with
SERVICE_IMAGE_PUSH_TAG ?= $(SERVICE_IMAGE_TAG)

# Base image for the service
BASE_IMAGE_NAME := base
BASE_IMAGE_TAG := 730ff18e1383e20e0c5f9d2c4a551c10f8f9b510


## Variables required for utils_container.mk

# Build image tag to be used
BUILD_IMAGE_TAG := 753126790c9ecd763840d9fe58507335af02b875

CALL_W_CONTAINER := all smoke_test init-repos


## Targets

.PHONY: $(CALL_W_CONTAINER)

all: smoke_test

smoke_test:
	echo OK

include $(UTILS_PATH)/make_lib/utils_container.mk
include $(UTILS_PATH)/make_lib/utils_image.mk
include $(UTILS_PATH)/make_lib/utils_repo.mk

init-repos: IMAGES_SHARED = /tmp/repo_utils
init-repos: $(IMAGES_SHARED)/overlays/rbkmoney/.git $(IMAGES_SHARED)/salt/rbkmoney/.git $(IMAGES_SHARED)/salt/common/.git

