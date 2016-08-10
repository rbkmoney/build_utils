# Working with docker run and docker-compose

ifndef UTILS_PATH
$(error UTILS_PATH is not set)
endif

include $(UTILS_PATH)/make_lib/utils_common.mk

ifndef BUILD_IMAGE_TAG
$(error BUILD_IMAGE_TAG is not set)
endif

# Image with build/dev environment
BUILD_IMAGE := "$(REGISTRY)/$(ORG_NAME)/build:$(BUILD_IMAGE_TAG)"

UTIL_TARGETS := wc_shell wc_% wdeps_% run_w_container_% check_w_container_%

DOCKER_RUN_PREFIX = $(DOCKER) run --rm -v $$PWD:$$PWD -v $$HOME:$$HOME:ro --workdir $$PWD
ifdef GITHUB_PRIVKEY
PRIVKEY_PATH=$(shell dirname ${GITHUB_PRIVKEY})
PRIVKEY_FILE=$(shell basename ${GITHUB_PRIVKEY})
PRIVKEY_CONT_PATH=/tmp/priv_key
DOCKER_RUN_PREFIX += -v $(PRIVKEY_PATH):$(PRIVKEY_CONT_PATH):ro --env GITHUB_PRIVKEY=$(PRIVKEY_CONT_PATH)/$(PRIVKEY_FILE)
endif

UNAME = $(shell whoami | tr '[:upper:]' '[:lower:]')
UID = $(shell id -u | tr '[:upper:]' '[:lower:]')
GNAME = $(shell id -g -n $(UNAME))
GID = $(shell id -g)
DOCKER_RUN_CMD = $(UTILS_PATH)/sh/as_user.sh -uname $(UNAME) -uid $(UID) -uhome $$HOME -gname $(GNAME) -gid $(GID)

.PHONY: gen_compose_file
## Interface targets

# Run and attach to build container
wc_shell:
	$(DOCKER_RUN_PREFIX) -it $(BUILD_IMAGE) $(DOCKER_RUN_CMD)

# Run a target in container
wc_%:
	$(MAKE) -s run_w_container_$*

# Run and attach to build container via docker-compose
wdeps_shell:
	$(MAKE) -s to_wdeps_shell

# Run a target on container with docker-compose
wdeps_%:
	$(MAKE) -s run_w_compose_$*


## Utils
to_wdeps_shell: DOCKER_COMPOSE = $(call which,docker-compose)
to_wdeps_shell: gen_compose_file
	{ \
	echo "Warning: 'make wc_shell' is the preferred way to run dev environment." ; \
	$(DOCKER_COMPOSE) up -d ; \
	$(DOCKER_COMPOSE) exec $(SERVICE_NAME) /bin/bash ; \
	$(DOCKER_COMPOSE) down ; \
	}

run_w_container_%: check_w_container_%
	{ \
	$(DOCKER_RUN_PREFIX) $(BUILD_IMAGE) $(DOCKER_RUN_CMD) -cmd 'make $*' ; \
	res=$$? ; exit $$res ; \
	}

run_w_compose_%: DOCKER_COMPOSE = $(call which,docker-compose)
run_w_compose_%: check_w_container_% gen_compose_file
	{ \
	$(DOCKER_COMPOSE) up -d ; \
	$(DOCKER_COMPOSE) exec -T $(SERVICE_NAME) make $* ; \
	res=$$? ; \
	$(DOCKER_COMPOSE) kill ; \
	$(DOCKER_COMPOSE) down ; \
	exit $$res ; \
	}

gen_compose_file: $(call validate_templates_path)
	SERVICE_NAME=$(SERVICE_NAME) \
	BUILD_IMAGE=$(BUILD_IMAGE) \
	$(TEMPLATES_PATH)/docker-compose.sh > docker-compose.yml

check_w_container_%:
	$(if $(CALL_W_CONTAINER),,echo "CALL_W_CONTAINER is not set" ; exit 1)
	$(if $(filter $*,$(CALL_W_CONTAINER)),,\
	$(error "Error: target '$*' cannot be called wc_ or wdeps_"))

