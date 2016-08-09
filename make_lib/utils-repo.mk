ifndef UTILS_PATH
$(error UTILS_PATH is not set)
endif
include $(UTILS_PATH)/make_lib/utils_common.mk

REPO_INIT := $(UTILS_PATH)/repo-init.sh
IMAGES_SHARED := $(shell echo "${HOME}")/.cache/$(ORGNAME)/images/shared
GITHUB_PRIVKEY ?=
GITHUB_PRIVKEY := $(shell echo $(GITHUB_PRIVKEY) | sed -e 's|%|%%|g')
GITHUB_URI_PREFIX := git+ssh://github.com

BAKKA_SU_PRIVKEY ?=
BAKKA_SU_PRIVKEY := $(shell echo $(BAKKA_SU_PRIVKEY) | sed -e 's|%|%%|g')
BAKKA_SU_URI_PREFIX := $(if $(BAKKA_SU_PRIVKEY),git+ssh,git)://git.bakka.su

# portage
$(IMAGES_SHARED)/portage/.git: .git
	$(if $(BAKKA_SU_PRIVKEY),SSH_PRIVKEY="$(BAKKA_SU_PRIVKEY)",) UTILS_PATH=$(UTILS_PATH) \
	"$(REPO_INIT)" "$(IMAGES_SHARED)/portage" "$(BAKKA_SU_URI_PREFIX)/gentoo-mirror"

# overlays
$(IMAGES_SHARED)/overlays/rbkmoney/.git: .git
	$(if $(GITHUB_PRIVKEY),SSH_PRIVKEY="$(GITHUB_PRIVKEY)",) UTILS_PATH=$(UTILS_PATH) \
	"$(REPO_INIT)" "$(IMAGES_SHARED)/overlays/rbkmoney" "$(GITHUB_URI_PREFIX)/rbkmoney/gentoo-overlay"

$(IMAGES_SHARED)/overlays/baka-bakka/.git: .git
	$(if $(BAKKA_SU_PRIVKEY),SSH_PRIVKEY="$(BAKKA_SU_PRIVKEY)",) UTILS_PATH=$(UTILS_PATH) \
	"$(REPO_INIT)" "$(IMAGES_SHARED)/overlays/baka-bakka" "$(BAKKA_SU_URI_PREFIX)/baka-bakka"

# salt
$(IMAGES_SHARED)/salt/rbkmoney/.git: .git
	$(if $(GITHUB_PRIVKEY),SSH_PRIVKEY="$(GITHUB_PRIVKEY)",) UTILS_PATH=$(UTILS_PATH) \
	"$(REPO_INIT)" "$(IMAGES_SHARED)/salt/rbkmoney" "$(GITHUB_URI_PREFIX)/rbkmoney/salt-main"

$(IMAGES_SHARED)/salt/common/.git: .git
	$(if $(BAKKA_SU_PRIVKEY),SSH_PRIVKEY="$(BAKKA_SU_PRIVKEY)",) UTILS_PATH=$(UTILS_PATH) \
	"$(REPO_INIT)" "$(IMAGES_SHARED)/salt/common" "$(BAKKA_SU_URI_PREFIX)/salt-common"
