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

DOCKER_RUN_PREFIX = $(DOCKER) run --rm -v $$PWD:$$PWD --workdir $$PWD
ifdef GITHUB_PRIVKEY
PRIVKEY_CONT_PATH=/tmp/github_privkey
DOCKER_RUN_PREFIX += -v `dirname $(GITHUB_PRIVKEY)`:$(PRIVKEY_CONT_PATH):ro --env GITHUB_PRIVKEY=$(PRIVKEY_CONT_PATH)/`basename $(GITHUB_PRIVKEY)`
endif

UNAME = $(shell whoami | tr '[:upper:]' '[:lower:]')
UID = $(shell id -u)
GNAME = $(shell id -g -n $(UNAME) | tr '[:upper:]' '[:lower:]')
GID = $(shell id -g)
DOCKER_RUN_CMD = $(UTILS_PATH)/sh/as_user.sh -u $(UID) -g $(GID)
DOCKER_RUN_PREFIX += -v $$HOME/.cache:/home/$(UNAME)/.cache:rw -v $$HOME/.ssh:/home/$(UNAME)/.ssh:ro
export UNAME

# Additional options can be passed to 'docker run' via DOCKER_RUN_OPTS
DOCKER_RUN_PREFIX += $(DOCKER_RUN_OPTS)

.PHONY: gen_compose_file
## Interface targets

# Run and attach to build container
wc_shell:
	$(DOCKER_RUN_PREFIX) -it $(BUILD_IMAGE) $(DOCKER_RUN_CMD) $(UNAME) $(GNAME)

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
	$(if $(DOCKER_COMPOSE_PREEXEC_HOOK),$(DOCKER_COMPOSE_PREEXEC_HOOK);,) \
	$(DOCKER_COMPOSE) exec $(SERVICE_NAME) $(DOCKER_RUN_CMD) $(UNAME) $(GNAME) ; \
	$(DOCKER_COMPOSE) down ; \
	}

run_w_container_%: check_w_container_%
	{ \
	$(DOCKER_RUN_PREFIX) $(BUILD_IMAGE) $(DOCKER_RUN_CMD) -c 'make $*' $(UNAME) $(GNAME) ; \
	res=$$? ; exit $$res ; \
	}

run_w_compose_%: DOCKER_COMPOSE = $(call which,docker-compose)
run_w_compose_%: check_w_container_% gen_compose_file
	{ \
	$(DOCKER_COMPOSE) up -d ; \
	$(if $(DOCKER_COMPOSE_PREEXEC_HOOK),$(DOCKER_COMPOSE_PREEXEC_HOOK);,) \
	$(DOCKER_COMPOSE) exec -T $(SERVICE_NAME) $(DOCKER_RUN_CMD) -c 'make $*' $(UNAME) $(GNAME) ; \
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

