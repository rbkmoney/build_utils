include utils_common.mk

ifndef BASE_IMAGE_NAME
$(error BASE_IMAGE_NAME is not set)
endif

ifndef BASE_IMAGE_TAG
$(error BASE_IMAGE_TAG is not set)
endif

ifndef IMAGE_TAG
$(error IMAGE_TAG is not set)
endif

ifndef PUSH_IMAGE_TAG
$(error PUSH_IMAGE_TAG is not set)
endif

BASE_IMAGE := $(REGISTRY)/$(ORG_NAME)/$(BASE_IMAGE_NAME):$(BASE_IMAGE_TAG)
IMAGE_NAME = $(REGISTRY)/$(ORG_NAME)/$(SERVICE_NAME)

build_image: wc_release
	$(DOCKER) build --force-rm --tag "$(IMAGE_NAME):$(IMAGE_TAG)" .

push:
	$(DOCKER) tag "$(IMAGE_NAME):$(IMAGE_TAG)" "$(IMAGE_NAME):$(PUSH_IMAGE_TAG)"
	$(DOCKER) push "$(IMAGE_NAME):$(PUSH_IMAGE_TAG)"

