ifndef UTILS_PATH
$(error UTILS_PATH is not set)
endif
include $(UTILS_PATH)/make_lib/utils_common.mk

REPO_INIT := $(UTILS_PATH)/sh/repo-init.sh
IMAGES_SHARED := $(shell echo "${HOME}")/.cache/$(ORG_NAME)/images/shared
GITHUB_PRIVKEY ?=
GITHUB_URI_PREFIX := git+ssh://github.com

BAKKA_SU_PRIVKEY ?=
BAKKA_SU_URI_PREFIX := $(if $(BAKKA_SU_PRIVKEY),git+ssh,git)://git.bakka.su

UNAME_S := $(shell uname -s)
ifeq ($(UNAME_S),Darwin)
	DATE := gdate
else
	DATE := date
endif

REPO_SHALLOW_SINCE ?= $(shell $(DATE) "+%Y-%m-%d" -d "12 months ago")

# portage
$(IMAGES_SHARED)/portage/.git: .git
	$(if $(BAKKA_SU_PRIVKEY),SSH_PRIVKEY=$(call escape_percent,$(BAKKA_SU_PRIVKEY)),) \
	UTILS_PATH=$(UTILS_PATH) "$(REPO_INIT)" "$(IMAGES_SHARED)/portage" \
	"$(BAKKA_SU_URI_PREFIX)/gentoo-mirror" "$(REPO_SHALLOW_SINCE)"

portage: $(IMAGES_SHARED)/portage/.git
	mkdir -p $@
	git --git-dir "$<" -C $@ checkout -q -f $(PORTAGE_REF)

# overlays
$(IMAGES_SHARED)/overlays/rbkmoney/.git: .git
	$(if $(GITHUB_PRIVKEY),SSH_PRIVKEY=$(call escape_percent,$(GITHUB_PRIVKEY)),) \
	UTILS_PATH=$(UTILS_PATH) "$(REPO_INIT)" "$(IMAGES_SHARED)/overlays/rbkmoney" \
	"$(GITHUB_URI_PREFIX)/rbkmoney/gentoo-overlay" "$(REPO_SHALLOW_SINCE)"

overlays/rbkmoney: $(IMAGES_SHARED)/overlays/rbkmoney/.git
	mkdir -p $@
	git --git-dir "$<" -C $@ checkout -q -f $(OVERLAYS_RBKMONEY_REF)

$(IMAGES_SHARED)/overlays/baka-bakka/.git: .git
	$(if $(BAKKA_SU_PRIVKEY),SSH_PRIVKEY=$(call escape_percent,$(BAKKA_SU_PRIVKEY)),) \
	UTILS_PATH=$(UTILS_PATH) "$(REPO_INIT)" "$(IMAGES_SHARED)/overlays/baka-bakka" \
	"$(BAKKA_SU_URI_PREFIX)/baka-bakka" "$(REPO_SHALLOW_SINCE)"

overlays/baka-bakka: $(IMAGES_SHARED)/overlays/baka-bakka/.git
	mkdir -p $@
	git --git-dir "$<" -C $@ checkout -q -f $(OVERLAYS_BAKKA_REF)

# salt
$(IMAGES_SHARED)/salt/rbkmoney/.git: .git
	$(if $(GITHUB_PRIVKEY),SSH_PRIVKEY=$(call escape_percent,$(GITHUB_PRIVKEY)),) \
	UTILS_PATH=$(UTILS_PATH) "$(REPO_INIT)" "$(IMAGES_SHARED)/salt/rbkmoney" \
	"$(GITHUB_URI_PREFIX)/rbkmoney/salt-main" "$(REPO_SHALLOW_SINCE)"

$(IMAGES_SHARED)/salt/common/.git: .git
	$(if $(BAKKA_SU_PRIVKEY),SSH_PRIVKEY=$(call escape_percent,$(BAKKA_SU_PRIVKEY)),) \
	UTILS_PATH=$(UTILS_PATH) "$(REPO_INIT)" "$(IMAGES_SHARED)/salt/common" \
	"$(BAKKA_SU_URI_PREFIX)/salt-common" "$(REPO_SHALLOW_SINCE)"

