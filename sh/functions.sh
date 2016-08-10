#!/bin/bash
# No colors for now

function ebegin() {
    local message=$1
    echo -n "-=- ${message} " 1>&2;    
}

function eend() {
    local retcode=$1 fail_message=$2
    if [ $retcode -gt 0 ]; then
	echo "[ !! ]" 1>&2
	eerror "${fail_message}"
	return $retcode
    else
	echo "[ ok ]" 1>&2; return $retcode
    fi
}

function einfo() {
    local message=$1
    echo "-I- ${message}" 1>&2;
}

function ewarn() {
    local message=$1
    echo "-!- ${message}" 1>&2;
}

function eerror() {
    local message=$1
    echo "-!! ${message}" 1>&2;
}
