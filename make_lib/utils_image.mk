# Building and pushing images

.PHONY: Dockerfile

ifndef UTILS_PATH
$(error UTILS_PATH is not set)
endif

include $(UTILS_PATH)/make_lib/utils_common.mk

ifndef SERVICE_IMAGE_TAG
$(error SERVICE_IMAGE_TAG is not set)
endif

DOCKER_BUILD_OPTIONS ?= --force-rm --no-cache

## Interface targets
build_image:
	$(MAKE) -s do_build_image

push_image:
	$(MAKE) -s do_push_image

rm_local_image:
	$(MAKE) -s do_rm_local_image

do_rm_local_image:
	$(DOCKER) rmi "$(SERVICE_IMAGE_NAME):$(SERVICE_IMAGE_PUSH_TAG)"
	if [ "$(SERVICE_IMAGE_PUSH_TAG)" != "$(SERVICE_IMAGE_TAG)" ] && [ -n "$(SERVICE_IMAGE_TAG)" ]; then \
	$(DOCKER) rmi "$(SERVICE_IMAGE_NAME):$(SERVICE_IMAGE_TAG)"; \
	fi

## Utils
do_build_image: Dockerfile
	$(DOCKER) build $(DOCKER_BUILD_OPTIONS) --tag $(SERVICE_IMAGE_NAME):$(SERVICE_IMAGE_TAG) . && \
	$(DOCKER) images | grep $(SERVICE_IMAGE_TAG)

do_push_image:
	$(if $(SERVICE_IMAGE_PUSH_TAG),,echo "SERVICE_IMAGE_PUSH_TAG is not set" ; exit 1)
	$(DOCKER) tag "$(SERVICE_IMAGE_NAME):$(SERVICE_IMAGE_TAG)" "$(SERVICE_IMAGE_NAME):$(SERVICE_IMAGE_PUSH_TAG)" && \
	$(DOCKER) push "$(SERVICE_IMAGE_NAME):$(SERVICE_IMAGE_PUSH_TAG)"

Dockerfile: $(call validate_templates_path) $(TEMPLATES_PATH)/Dockerfile.sh
	$(if $(BASE_IMAGE_NAME),,echo "BASE_IMAGE_NAME is not set" ; exit 1)
	$(if $(BASE_IMAGE_TAG),,echo "BASE_IMAGE_TAG is not set" ; exit 1)
	BASE_IMAGE="$(REGISTRY)/$(ORG_NAME)/$(BASE_IMAGE_NAME):$(BASE_IMAGE_TAG)" \
	BASE_IMAGE_NAME=$(BASE_IMAGE_NAME) \
	BASE_IMAGE_TAG=$(BASE_IMAGE_TAG) \
	BUILD_IMAGE_TAG=$(BUILD_IMAGE_TAG) \
	$(TEMPLATES_PATH)/Dockerfile.sh > Dockerfile

