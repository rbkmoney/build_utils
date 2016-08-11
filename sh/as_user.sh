#!/bin/bash

function usage(){
    echo "usage: ${0##*/} [+-u uid] [+-g gid] [+-d home] [+-c cmd] [+-h} [--] <username> <groupname>"
}

while getopts :u:g:d:c:h OPT; do
    case $OPT in
	u|+u)
	    uid="$OPTARG"
	    ;;
	g|+g)
	    gid="$OPTARG"
	    ;;
	d|+d)
	    homedir="$OPTARG"
	    ;;
	c|+c)
            cmd="$OPTARG"
	    ;;
	h|+h)
	    usage
	    ;;
	*)
	    usage
	    exit 2
    esac
done
shift $(( OPTIND - 1 ))
OPTIND=1

if [ -z "${1}" -o -z "${2}" ]; then
   usage
   exit 2
fi

username="${1}"
groupname="${2}"

groupadd $(test -n "$gid" && echo "-g $gid") "${groupname}" || exit $?

useradd $(test -n "$uid" && echo "-u $uid") -g "${groupname}" \
	$(test -n "$homedir" && echo "-M -d ${homedir}" || echo "-m") \
	"${username}" || exit $?

if [ -n "$cmd" ]; then
    su "${username}" -c "$cmd";
else
    su "${username}";
fi

