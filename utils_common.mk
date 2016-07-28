ifndef SERVICE_NAME
$(error SERVICE_NAME is not set)
endif

ifndef BUILD_IMAGE_TAG
$(error BUILD_IMAGE_TAG is not set)
endif

REGISTRY := dr.rbkmoney.com
ORG_NAME := rbkmoney
BUILD_IMAGE := "$(REGISTRY)/$(ORG_NAME)/build:$(BUILD_IMAGE_TAG)"

SHELL := /bin/bash

which = $(if $(shell which $(1) 2>/dev/null),\
	$(shell which $(1) 2>/dev/null),\
	$(error "Error: could not locate $(1)!"))

DOCKER = $(call which,docker)

