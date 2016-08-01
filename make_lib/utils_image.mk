# Building and pushing images

ifndef UTILS_PATH
$(error UTILS_PATH is not set)
endif

include $(UTILS_PATH)/utils_common.mk

ifndef SERVICE_IMAGE_TAG
$(error SERVICE_IMAGE_TAG is not set)
endif

# Image for this service
SERVICE_IMAGE_NAME = $(REGISTRY)/$(ORG_NAME)/$(SERVICE_NAME)


## Interface targets
build_image:
	$(MAKE) -s do_build_image

push_image:
	$(MAKE) -s do_push_image


## Utils
do_build_image: Dockerfile
	$(DOCKER) build --force-rm --tag $(SERVICE_IMAGE_NAME):$(SERVICE_IMAGE_TAG) . && \
	$(DOCKER) images | grep $(SERVICE_IMAGE_TAG)

do_push_image:
	if [ -n "$(SERVICE_IMAGE_PUSH_TAG)" ]; then \
	$(DOCKER) tag "$(SERVICE_IMAGE_NAME):$(SERVICE_IMAGE_TAG)" "$(SERVICE_IMAGE_NAME):$(SERVICE_IMAGE_PUSH_TAG)" && \
	$(DOCKER) push "$(SERVICE_IMAGE_NAME):$(SERVICE_IMAGE_PUSH_TAG)"; \
	else \
	echo "Error: SERVICE_IMAGE_PUSH_TAG is not set!" && exit 1; \
	fi

Dockerfile: $(call validate_templates_path) $(TEMPLATES_PATH)/Dockerfile.sh
	if [ -n "$(BASE_IMAGE_NAME)" ] && [ -n "$(BASE_IMAGE_TAG)" ]; then \
	BASE_IMAGE="$(REGISTRY)/$(ORG_NAME)/$(BASE_IMAGE_NAME):$(BASE_IMAGE_TAG)" \
	BASE_IMAGE_TAG=$(BASE_IMAGE_TAG) \
	BUILD_IMAGE_TAG=$(BUILD_IMAGE_TAG) \
	$(TEMPLATES_PATH)/Dockerfile.sh > Dockerfile; \
	else \
	echo "Error: BASE_IMAGE_NAME and BASE_IMAGE_TAG are required!" && exit 1; \
	fi

