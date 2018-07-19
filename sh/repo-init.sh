#!/bin/bash
test -n "${UTILS_PATH}" || exit 2
source "${UTILS_PATH}/sh/functions.sh"

d_repo="$1"
remote_uri="$2"

if [[ "${remote_uri}" == "git+ssh"* ]]; then
    export GIT_SSH_COMMAND="$(which ssh) -o StrictHostKeyChecking=no -o User=git $([ -n "${SSH_PRIVKEY}" ] && echo -o IdentityFile="${SSH_PRIVKEY}")"
    einfo "GIT_SSH_COMMAND: ${GIT_SSH_COMMAND}"
fi
if [[ -d "${d_repo}/.git" ]]; then
    einfo "Syncing repository ${d_repo}"
    git -C "${d_repo}" checkout -q master || exit $?
    git -C "${d_repo}" pull -q || exit $?
else
    einfo "Initialising repository ${d_repo}"
    if [[ -d "${d_repo}" ]]; then
	ebegin "Removing directory ${d_repo}"
	rmdir "${d_repo}"
	eend $? "Failed to remove ${d_repo}, do that yourself" || exit $?
    fi
    ebegin "Creating directory ${d_repo}"
    mkdir -p "${d_repo}"
    eend $? "Failed to create directory ${d_repo}" || exit $?
    einfo "Clonning repository ${d_repo}"
    git clone -q  --shallow-since="$(date "+%Y-%m-%d" -d "2 weeks ago")" "${remote_uri}" "${d_repo}" || exit $?
fi

