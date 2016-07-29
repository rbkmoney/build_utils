# Building and pushing images

ifndef UTILS_PATH
$(error UTILS_PATH is not set)
endif

include $(UTILS_PATH)/utils_common.mk

ifndef IMAGE_TAG
$(error IMAGE_TAG is not set)
endif

IMAGE_NAME = $(REGISTRY)/$(ORG_NAME)/$(SERVICE_NAME)

build_image:
	$(MAKE) -s do_build_image

do_build_image: Dockerfile
	$(DOCKER) build --force-rm --tag $(IMAGE_NAME):$(IMAGE_TAG) . && \
	$(DOCKER) images | grep $(IMAGE_TAG)

push_image:
	$(MAKE) -s do_push_image

do_push_image:
	if [ -n "$(PUSH_IMAGE_TAG)" ]; then \
	$(DOCKER) tag "$(IMAGE_NAME):$(IMAGE_TAG)" "$(IMAGE_NAME):$(PUSH_IMAGE_TAG)" && \
	$(DOCKER) push "$(IMAGE_NAME):$(PUSH_IMAGE_TAG)"; \
	else \
	echo "Error: PUSH_IMAGE_TAG is not set!" && exit 1; \
	fi

Dockerfile: $(call validate_templates_path) $(TEMPLATES_PATH)/Dockerfile.sh
	if [ -n "$(BASE_IMAGE_NAME)" ] && [ -n "$(BASE_IMAGE_TAG)" ]; then \
	BASE_IMAGE="$(REGISTRY)/$(ORG_NAME)/$(BASE_IMAGE_NAME):$(BASE_IMAGE_TAG)" \
	BASE_IMAGE_TAG=$(BASE_IMAGE_TAG) \
	BUILD_IMAGE_TAG=$(BUILD_IMAGE_TAG) \
	$(TEMPLATES_PATH)/Dockerfile.sh > Dockerfile; \
	else \
	echo "Error: BASE_MAGE_NAME and BASE_IMAGE_TAG are required!" && exit 1; \
	fi

