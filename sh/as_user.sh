#!/bin/bash

MANDATORY=(user_name user_id group_name group_id)

function usage(){
    echo "Usage: $0 -uname <user_name> -uid <user_id> -gname <group_name> -gid <group_id> [-uhome <user_home> -cmd <commmand> -h]"
}

while [ -n "$1" ]; do
    case $1 in
        -uname)
            shift
            user_name=$1
            shift
            ;;
        -uid)
            shift
            user_id=$1
            shift
            ;;
        -uhome)
            shift
            user_home=$1
            shift
            ;;
        -gname)
            shift
            group_name=$1
            shift
            ;;
        -gid)
            shift
            group_id=$1
            shift
            ;;
        -cmd)
            shift
            cmd=$1
            shift
            ;;
        -h)
            usage
            exit 0
            ;;
        *)
            echo "Unknown arg: $1"
            usage
            exit 1
            ;;
    esac
done

for id in `seq 4`; do
    varname=${MANDATORY[id - 1]}
    if [ -z "${!varname}" ]; then
        echo "Error: ${varname} missing."
        exit 2
    fi
done

groupadd -g $group_id $group_name

if [ -n "$user_home" ]; then
    # use HOME from user_home
    useradd -g $group_name -u $user_id -M -d $user_home $user_name;
else
    # create HOME
    useradd -g $group_name -u $user_id -m $user_name;
fi

if [ -n "$cmd" ]; then
    # run command as new user
    su $user_name -c "$cmd";
else
    # run /bin/bash as new user
    su $user_name;
fi

