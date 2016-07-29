SERVICE_NAME := build_utils_demo

# DEFAULT IMAGE_TAG
IMAGE_TAG ?= $(shell whoami_`date +'%s'`)

BASE_IMAGE_NAME := service_base
BASE_IMAGE_TAG := 6de9b3b

# For convenience (specific for this repo)
# BASE_PATH is defined in the top Makefile
UTILS_PATH := $(BASE_PATH)/make_lib
TEMPLATES_PATH := $(BASE_PATH)/examples

BUILD_IMAGE_TAG := latest
CALL_W_CONTAINER := all smoke_test

.PHONY: $(CALL_W_CONTIANER)

all: smoke_test

smoke_test:
	echo OK

include $(UTILS_PATH)/utils_container.mk
include $(UTILS_PATH)/utils_image.mk

