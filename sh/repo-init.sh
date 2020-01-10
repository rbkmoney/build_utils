#!/bin/bash
if [ -z "${UTILS_PATH}" ]; then
    echo "Env UTILS_PATH is not defined" >&2
    exit 2
fi
set -u
source "${UTILS_PATH}/sh/functions.sh"

case $(shell uname -s) of
    Darwin)
        date_util = gdate ;;
    *)
        date_util = date ;;
esac

d_repo="$1"
remote_uri="$2"
shallow_since="${3:-$("${date_util}" "+%Y-%m-%d" -d "2 weeks ago")}"

if [[ "${remote_uri}" == "git+ssh"* ]]; then
    export GIT_SSH_COMMAND="$(which ssh) -o StrictHostKeyChecking=no -o User=git $([ -n "${SSH_PRIVKEY}" ] && echo -o IdentityFile="${SSH_PRIVKEY}")"
    einfo "GIT_SSH_COMMAND: ${GIT_SSH_COMMAND}"
fi
if [[ -d "${d_repo}/.git" ]]; then
    einfo "Syncing repository ${d_repo}"
    git -C "${d_repo}" fetch -q origin master --shallow-since="${shallow_since}" || exit $?
    git -C "${d_repo}" checkout -fq master || exit $?
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
    git clone -q --shallow-since="${shallow_since}" "${remote_uri}" "${d_repo}" || exit $?
fi

