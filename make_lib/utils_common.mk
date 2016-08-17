ifndef UTILS_COMMON_MK
UTILS_COMMON_MK := defined

ifndef SERVICE_NAME
$(error SERVICE_NAME is not set)
endif

escape_percent = $(shell echo $(1) | sed -e 's|%|%%|g')
git_ssh_cmd = $(shell which ssh) -o StrictHostKeyChecking=no -o User=git $(shell [ -n "$(1)" ] && echo -o IdentityFile="$(1)")

ifdef GITHUB_PRIVKEY
GIT_SSH_COMMAND = $(call git_ssh_cmd,$(call escape_percent,$(GITHUB_PRIVKEY)))
export GIT_SSH_COMMAND
endif

REGISTRY := dr.rbkmoney.com
ORG_NAME := rbkmoney
SERVICE_IMAGE_NAME := $(REGISTRY)/$(ORG_NAME)/$(SERVICE_NAME)
export SERVICE_NAME

SHELL := /bin/bash

which = $(if $(shell which $(1) 2>/dev/null),\
	$(shell which $(1) 2>/dev/null),\
	$(error "Error: could not locate $(1)!"))

DOCKER = $(call which,docker)

validate_templates_path = $(shell \
	if [ -n "$(TEMPLATES_PATH)" ]; then \
		test ! -d "$(TEMPLATES_PATH)" && echo "Error: $(TEMPLATES_PATH) does not exist!" && exit 1; \
	else \
		echo "Error: TEMPLATES_PATH is not set!" && exit 1; \
	fi)

endif #UTILS_COMMON_MK

