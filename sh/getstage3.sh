#!/bin/bash
test -n "${UTILS_PATH}" || exit 2
source "${UTILS_PATH}/sh/functions.sh"

function VerifyHashOfStage3() {
    # First param is package tarball, 2nd is the *.DIGEST file
    test_sum=$(awk -v myvar="$1" '$2==myvar {for(i=1; i<=1; i++) { print $1; exit}}' "${2}")
    calculated_sum=$(sha512sum "${1}" | awk '{print $1}' -)
    if [[ "$test_sum" == "$calculated_sum" ]]; then
	return 0
    else
	return 1
    fi
}

function get_stage3_by_path() {
    local stage3path="${1}"
    stage3="$(basename ${stage3path})"
    ebegin "Downloading ${AUTOBUILDS}/${stage3path} and its DIGESTS"
    wget -q -c "${AUTOBUILDS}/${stage3path}" "${AUTOBUILDS}/${stage3path}.DIGESTS" || exit $?
    eend $? "Fail"
    if VerifyHashOfStage3 "${stage3}" "${stage3}.DIGESTS"; then
	einfo "DIGEST verification passed, sha512 hashes match."
    else
	eerror "DIGEST verification failed!"
	exit 1
    fi
}

function get_latest_stage3() {
    local arch="${1}" suffix="${2}" __stage3var="${3}"
    ebegin "Downloading ${AUTOBUILDS}/latest-stage3-${arch}${suffix}.txt"
    wget -q "${AUTOBUILDS}/latest-stage3-${arch}${suffix}.txt" -O "latest-stage3-${arch}${suffix}.txt" || exit $?
    eend $? "Fail"
    stage3path="$(cat latest-stage3-${arch}${suffix}.txt | tail -n 1 | cut -f 1 -d ' ')"
    stage3="$(basename ${stage3path})" || exit $?
    einfo "latest stage3: ${stage3}"
    get_stage3_by_path "${stage3path}"
    eval $__stage3var="'$stage3'"
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

DIST="http://gentoo.bakka.su"

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
	    echo "usage: ${0##*/} [+-d ARG] [+-u ARG] [+-D ARG] [+-r} [--] ARCH [SUFFIX]" 1>&2
	    exit 2
    esac
done
shift $(( OPTIND - 1 ))
OPTIND=1
if [ -z $1 ]; then
    echo "Please specify a stage architecture and an optional suffix as the last arguments" 1>&2
    exit 2
fi
arch="$1"
suffix="$2" # Optional, e.g. -hardened+nomultilib

AUTOBUILDS="${DIST}/releases/${arch}/autobuilds/"

einfo "DIST: ${DIST}"
einfo "arch: ${arch} suffix: ${suffix}"

stage3=""
get_latest_stage3 "${arch}" "${suffix}" stage3
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