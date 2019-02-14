#!/bin/bash
test -n "${UTILS_PATH}" || exit 2
source "${UTILS_PATH}/sh/functions.sh"

function VerifyHashOfStage3() {
    # First param is package tarball, 2nd is the *.DIGEST file
    test_sum=$(awk -v myvar="$1" '$2==myvar {for(i=1; i<=1; i++) { print $1; exit}}' "${2}")
    if which sha512sum > /dev/null 2>&1; then
	calculated_sum=$(sha512sum "${1}" | awk '{print $1}' -)
    elif which shasum  > /dev/null 2>&1; then
	calculated_sum=$(shasum -a 512 "${1}" | awk '{print $1}' -)
    else
	eerror "Install sha512sum or shasum (from perl5) for verification"
	exit 1
    fi
    if [[ "$test_sum" == "$calculated_sum" ]]; then
	return 0
    else
	return 1
    fi
}

function get_iso_by_path() {
    local iso_path="${1}"
    local iso="${2}"
    ebegin "Downloading ${DIST}/${iso_path} and its DIGESTS"
    wget -q -c "${DIST}/${iso_path}" "${DIST}/${iso_path}.DIGESTS" || exit $?
    eend $? "Fail"
    if VerifyHashOfStage3 "${iso}" "${iso}.DIGESTS"; then
	einfo "DIGEST verification passed, sha512 hashes match."
    else
	eerror "DIGEST verification failed!"
	exit 1
    fi
}

function find_latest_iso() {
    local arch="${1}" suffix="${2}"
    local build_info="latest-install-${arch}${suffix}.txt"
    local autobuilds="${DIST}/releases/${arch}/autobuilds"
    local iso_path=""
    ebegin "Downloading ${autobuilds}/${build_info}"
    wget -q "${autobuilds}/${build_info}" -O "${build_info}" || exit $?
    eend $? "Fail"
    iso_path="releases/${arch}/autobuilds/$(cat ${build_info} | tail -n 1 | cut -f 1 -d ' ')"
    rm "${build_info}"
    einfo "latest iso: $(basename ${iso_path} || exit $?)"
    echo "${iso_path}"
}

function remove_iso() {
    local iso="${1}"
    ebegin "Removing ${iso}"
    rm -f "${iso}" || exit $?
    eend $? "Fail"
}

function usage() {
    echo "usage: ${0##*/} find-latest [+-D ARG] ARCH [SUFFIX]"
    echo "       ${0##*/} get-path    [+-D ARG] [+-d ARG] [+-u ARG] [+-r} [--] ISO_PATH"
}

DIST="http://gentoo.bakka.su/gentoo-distfiles"
OPTIND=2

case "$1" in
    find-latest)
        while getopts :D: OPT "${@}"; do
            case $OPT in
                D|+D)
	                DIST="${OPTARG}"
	                ;;
	            *)
                    usage
	                exit 2
            esac
        done
        shift $(( OPTIND - 1 ))
        arch="${1:-amd64}"
        suffix="${2:--minimal}" # Optional, e.g. -hardened+nomultilib
        if [ -z "${arch}" -o -z "${suffix}" ]; then
            echo "Please specify an iso architecture and an optional suffix as the last arguments" 1>&2
            exit 2
        fi
        einfo "DIST: ${DIST}"
        einfo "arch: ${arch} suffix: ${suffix}"
        find_latest_iso "${arch}" "${suffix}"
        ;;
    get-path)
        while getopts :d:u:D:r OPT "${@}"; do
            case $OPT in
	            D|+D)
	                DIST="${OPTARG}"
	                ;;
	            r|+r)
	                remove_afterwards=1
	                ;;
	            *)
                    usage
	                exit 2
            esac
        done
        shift $(( OPTIND - 1 ))
        if [ -z $1 ]; then
            echo "Please specify an iso path (e.g. obtained via ${0##*/} find-latest)" 1>&2
            exit 2
        fi
        iso_path="$1"
        iso="$(basename ${iso_path})" || exit $?
        einfo "DIST: ${DIST}"
        einfo "path: ${iso_path}"
        get_iso_by_path "${iso_path}" "${iso}"

        if [[ -n "${remove_afterwards}" ]]; then
            remove_iso "${iso}"
        else
            echo "${iso}"
        fi
        ;;
    *)
        usage
        exit 2
esac

