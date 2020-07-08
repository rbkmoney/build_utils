ifndef THRIFT
THRIFT = $(or $(shell which thrift), $(error "`thrift' executable missing"))
endif

CALL_W_CONTAINER += java.compile java.deploy java.install java.settings

MVN = mvn --no-transfer-progress --batch-mode

ifdef SETTINGS_XML
DOCKER_RUN_OPTS += -v "$(SETTINGS_XML):$(SETTINGS_XML)" -e SETTINGS_XML
MVN += -s "$(SETTINGS_XML)"
endif

ifdef LOCAL_BUILD
DOCKER_RUN_OPTS += -v $$HOME/.m2:/home/$(UNAME)/.m2:rw
endif

COMMIT_HASH = $(shell git --no-pager log -1 --pretty=format:"%h")
NUMBER_COMMITS = $(shell git rev-list --count HEAD)
JAVA_PKG_VERSION := 1.$(NUMBER_COMMITS)-$(COMMIT_HASH)

ifdef BRANCH_NAME
ifeq "$(findstring epic,$(BRANCH_NAME))" "epic"
JAVA_PKG_VERSION := $(JAVA_PKG_VERSION)-epic
endif
endif

MVN += -Dpath_to_thrift="$(THRIFT)" -Dcommit.number="$(NUMBER_COMMITS)"

ifdef MVN_PROFILE
DOCKER_RUN_OPTS += -e MVN_PROFILE
endif

ifneq ($(and $(GNUPGHOME),$(GPG_KEYID),$(GPG_PASSPHRASE)),)
DOCKER_RUN_OPTS += -v "$(GNUPGHOME):$(GNUPGHOME):rw" -e GNUPGHOME -e GPG_KEYID -e GPG_PASSPHRASE
MVN += -Dgpg.keyname="$(GPG_KEYID)" -Dgpg.passphrase="$(GPG_PASSPHRASE)"
endif

.PHONY: java.compile java.deploy java.install java.settings

java.compile: java.settings
	$(MVN) compile

java.deploy: java.settings
	$(MVN) versions:set versions:commit -DnewVersion="$(JAVA_PKG_VERSION)" && \
	$(MVN) deploy

java.install: java.settings
	$(MVN) clean && \
	$(MVN) versions:set versions:commit -DnewVersion="$(JAVA_PKG_VERSION)" && \
	$(MVN) install

java.settings:
	$(if $(SETTINGS_XML),, echo "SETTINGS_XML not defined"; exit 1)
	$(MVN) help:all-profiles
