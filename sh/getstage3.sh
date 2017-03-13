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

function get_stage3_by_path() {
    local stage3path="${1}"
    local stage3="${2}"
    ebegin "Downloading ${DIST}/${stage3path} and its DIGESTS"
    wget -q -c "${DIST}/${stage3path}" "${DIST}/${stage3path}.DIGESTS" || exit $?
    eend $? "Fail"
    if VerifyHashOfStage3 "${stage3}" "${stage3}.DIGESTS"; then
	einfo "DIGEST verification passed, sha512 hashes match."
    else
	eerror "DIGEST verification failed!"
	exit 1
    fi
}

function find_latest_stage3() {
    local arch="${1}" suffix="${2}"
    local build_info="latest-stage3-${arch}${suffix}.txt"
    local autobuilds="${DIST}/releases/${arch}/autobuilds"
    local stage3path=""
    ebegin "Downloading ${autobuilds}/${build_info}"
    wget -q "${autobuilds}/${build_info}" -O "${build_info}" || exit $?
    eend $? "Fail"
    stage3path="releases/${arch}/autobuilds/$(cat ${build_info} | tail -n 1 | cut -f 1 -d ' ')"
    rm "${build_info}"
    einfo "latest stage3: $(basename ${stage3path} || exit $?)"
    echo "${stage3path}"
}

function decompress_stage3() {
    local stage3="${1}" dst="${2}"
    ebegin "Decompressing ${stage3} to ${dst}"
    bzip2 -d "${stage3}" -c > "${dst}" || exit $?
    eend $? "Fail"
}

function unpack_stage3() {
    local stage3="${1}" dst="${2}"
    ebegin "Unpacking ${stage3} to ${dst}"
    test $UID -gt 0 && einfo "This will probaly fail miserably, since you are not root."
    mkdir -p "${dst}"
    bunzip2 -c "${stage3}" | tar --exclude "./etc/hosts" --exclude "./sys/*" -xf - -C "${dst}" || exit $?
    eend $? "Fail"
}

function remove_stage3() {
    local stage3="${1}"
    ebegin "Removing ${stage3}"
    rm -f "${stage3}" || exit $?
    eend $? "Fail"
}

function usage() {
    echo "usage: ${0##*/} find-latest [+-D ARG] ARCH [SUFFIX]"
    echo "       ${0##*/} get-path    [+-D ARG] [+-d ARG] [+-u ARG] [+-r} [--] STAGE_PATH"
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
        if [ -z $1 ]; then
            echo "Please specify a stage architecture and an optional suffix as the last arguments" 1>&2
            exit 2
        fi
        arch="$1"
        suffix="$2" # Optional, e.g. -hardened+nomultilib
        einfo "DIST: ${DIST}"
        einfo "arch: ${arch} suffix: ${suffix}"
        find_latest_stage3 "${arch}" "${suffix}"
        ;;
    get-path)
        while getopts :d:u:D:r OPT "${@}"; do
            case $OPT in
	            d|+d)
	                decompress_dst="${OPTARG}"
	                ;;
	            u|+u)
	                unpack_dst="${OPTARG}"
	                ;;
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
            echo "Please specify a stage path (e.g. obtained via ${0##*/} find-latest)" 1>&2
            exit 2
        fi
        stage3path="$1"
        stage3="$(basename ${stage3path})" || exit $?
        einfo "DIST: ${DIST}"
        einfo "path: ${stage3path}"
        get_stage3_by_path "${stage3path}" "${stage3}"

        if [[ -n "${unpack_dst}" ]]; then
            unpack_stage3 "${stage3}" "${unpack_dst}"
        elif [[ -n "${decompress_dst}" ]]; then
            decompress_stage3 "${stage3}" "${decompress_dst}"
        fi
        if [[ -n "${remove_afterwards}" ]]; then
            remove_stage3 "${stage3}"
        else
            echo "${stage3}"
        fi
        ;;
    *)
        usage
        exit 2
esac

