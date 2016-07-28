include utils_common.mk

ifndef IMAGE_TAG
$(error IMAGE_TAG is not set)
endif

ifndef PUSH_IMAGE_TAG
$(error PUSH_IMAGE_TAG is not set)
endif

IMAGE_NAME = $(REGISTRY)/$(ORG_NAME)/$(SERVICE_NAME)

build_image: wc_release
ifdef BASE_IMAGE_NAME
ifdef BASE_IMAGE_TAG
BASE_IMAGE := $(REGISTRY)/$(ORG_NAME)/$(BASE_IMAGE_NAME):$(BASE_IMAGE_TAG)
	$(DOCKER) build --force-rm --tag "$(IMAGE_NAME):$(IMAGE_TAG)" .
else
	$(error BASE_IMAGE_TAG is not set)
endif #BASE_IMAGE_TAG
else
	$(error BASE_IMAGE_NAME is not set)
endif #BASE_IMAGE_NAME

push:
	$(DOCKER) tag "$(IMAGE_NAME):$(IMAGE_TAG)" "$(IMAGE_NAME):$(PUSH_IMAGE_TAG)"
	$(DOCKER) push "$(IMAGE_NAME):$(PUSH_IMAGE_TAG)"

