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

# User namespace docker fun
# Required for 'docker run --userns=host' when user remap enabled
chown 0:0 /bin/su

username="${1}"
groupname="${2}"
if [ -z "${homedir}" ]; then
    homedir="/home/${username}"
fi
test -d "${homedir}" && homedir_exists=1 || homedir_exists=0

use_gid=0

if getent group "${groupname}"; then
    echo "Group '${groupname}' exists, using it instead"
    echo $(getent group "${groupname}")
elif [ -n "${gid}" ] && getent group "${gid}"; then
    echo "Group with gid '${gid}' exists, using it instead"
    echo $(getent group "${gid}")
    use_gid=1
else
    echo "Group ${groupname} does not exist, creating it"
    groupadd $(test -n "${gid}" && echo "-g ${gid}") "${groupname}" || exit $?
fi

useradd $(test -n "$uid" && echo "-u $uid") \
	-g "$(test $use_gid -eq 1 && echo ${gid} || echo ${groupname})" \
	-d "${homedir}" $(test $homedir_exists -eq 1 && echo "-M" || echo "-m") \
	"${username}" || exit $?

export HOME="${homedir:-/home/${username}}"
chown "${username}:$(if [ $use_gid -eq 1 ]; then echo ${gid}; else echo ${groupname}; fi)" "$HOME"

# Skip -l flag for debian which brake build
distr="$(find /etc/ -maxdepth 1 -name '*-release' -print0 | xargs -0 grep '^ID=' | cut -d = -f 2)"
case $distr in
  debian) login="" ;;
  *) login="-l" ;;
esac

if [ -n "$cmd" ]; then
    su "${username}" ${login} -m -c "$cmd";
else
    su "${username}" ${login} -m;
fi
