#!/bin/bash
function usage(){
    echo "usage: ${0##*/} [+-u uid] [+-g gid] [+-d home] [+-c cmd] [+-h} [--] <username> <groupname> [<cmd>]"
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
if [ -z "${homedir}" ]; then
    homedir="/home/${username}"
fi
test -d "${homedir}" && homedir_exists=1 || homedir_exists=0

if [ -n "${3}" -a -z "${cmd}" ]; then
    shift 2
    cmd="${*}"
fi

# User namespace docker fun
# Required for 'docker run --userns=host' when user remap enabled
chown 0:0 /bin/su

# Alpine (busybox) support
USERADD_BUSYBOX=0
USERADD="$(which useradd)"
if [ $? != 0 ]; then
    USERADD="$(which adduser)"
    if [ $? != 0 ]; then
	echo "No useradd or adduser command was found" >&2
	exit 1
    fi
    USERADD_BUSYBOX=1
fi

GROUPADD_BUSYBOX=0
GROUPADD="$(which groupadd)"
if [ $? != 0 ]; then
    GROUPADD="$(which addgroup)"
    if [ $? != 0 ]; then
	echo "No groupadd or addgroup command was found" >&2
	exit 1
    fi
    GROUPADD_BUSYBOX=1
fi    

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
    ${GROUPADD} $(test -n "${gid}" && echo "-g ${gid}") "${groupname}" || exit $?
fi

if getent passwd "${username}"; then
    echo "User '${groupname}' exists, using it instead"
    echo $(getent passwd "${username}")
else
    if [ -n "${uid}" ] && getent passwd $uid; then
	prev_userinfo="$(getent passwd "${uid}")"
	prev_username="$(echo ${prev_userinfo} | awk -F: '{ print $1; }')"
	echo "Deleting user '${prev_username}' occupying uid ${uid}"
	if [ $USERADD_BUSYBOX == 0 ]; then
	    userdel ${prev_username} || exit $?
	else
	    deluser ${prev_username} || exit $?
	fi
    fi

    echo "Creating user '${username}'"
    ${USERADD} $(test -n "$uid" && echo "-u $uid") \
	       $(if [ $USERADD_BUSYBOX == 0 ]; then
		     test $use_gid -eq 1 && echo "-g ${gid}" || echo "-g ${groupname}"
		     echo "-d \"${homedir}\""
		     test $homedir_exists -eq 1 && echo "-M" || echo "-m"
		 else
		     test $use_gid -eq 1 && echo "-G ${gid}" || echo "-G ${groupname}"
		     echo "-D"
		     echo "-h ${homedir}"
		     test $homedir_exists -eq 1 && echo "-H"
		 fi) \
		     "${username}" || exit $?
fi

export HOME="${homedir:-/home/${username}}"
chown "${username}:$(if [ $use_gid -eq 1 ]; then echo ${gid}; else echo ${groupname}; fi)" "$HOME"

# Skip -l flag for debian which brake build
distr=$(grep '^ID=' /etc/*-release | cut -d = -f 2)
case $distr in
  debian) login="" ;;
  *) login="-l" ;;
esac

if [ -n "${cmd}" ]; then
    exec su "${username}" ${login} -m -c "${cmd}";
else
    exec su "${username}" ${login} -m
fi

