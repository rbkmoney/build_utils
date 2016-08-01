# Working with docker run and docker-compose

ifndef UTILS_PATH
$(error UTILS_PATH is not set)
endif

include $(UTILS_PATH)/utils_common.mk

ifndef CALL_W_CONTAINER
$(error CALL_W_CONTAINER is not set)
endif

ifndef BUILD_IMAGE_TAG
$(error BUILD_IMAGE_TAG is not set)
endif

# Image with build/dev environment
BUILD_IMAGE := "$(REGISTRY)/$(ORG_NAME)/build:$(BUILD_IMAGE_TAG)"

UTIL_TARGETS := wc_shell wc_% wdeps_% run_w_container_% check_w_container_%

# Run and attach to build container
wc_shell:
	$(DOCKER) run -it --rm -v $$PWD:$$PWD --workdir $$PWD $(BUILD_IMAGE) /bin/bash

wc_%:
	$(MAKE) -s run_w_container_$*

wdeps_%:
	$(MAKE) -s run_w_compose_$*

run_w_container_%: check_w_container_%
	{ \
	$(DOCKER) run --rm -v $$PWD:$$PWD --workdir $$PWD $(BUILD_IMAGE) /bin/bash -c 'pwd ; make $*' ; \
	res=$$? ; exit $$res ; \
	}

run_w_compose_%: DOCKER_COMPOSE = $(call which,docker-compose)
run_w_compose_%: check_w_container_% gen_compose_file
	{ \
	$(DOCKER_COMPOSE) up -d ; \
	$(DOCKER_COMPOSE) exec -T $(SERVICE_NAME) make $* ; \
	res=$$? ; \
	$(DOCKER_COMPOSE) down ; \
	exit $$res ; \
	}

gen_compose_file: $(call validate_templates_path)
	SERVICE_NAME=$(SERVICE_NAME) \
	BUILD_IMAGE=$(BUILD_IMAGE) \
	$(TEMPLATES_PATH)/docker-compose.sh > docker-compose.yml

check_w_container_%:
	$(if $(filter $*,$(CALL_W_CONTAINER)),,\
	$(error "Error: target '$*' cannot be called wc_ or wdeps_"))

