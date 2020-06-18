#!/bin/bash

# MAVEN WRAPPER
# instead of writting help here just run with ./mvn_script --help


# Custom dirs
# dirs=$(find . -maxdepth 1 -type d -not -name '\.*' -not -name 'target')
dirs="resources remote-interfaces ca vehicle vehicle-network rsu"

# custom maven commands for certain directories
declare -A special_rules=( ["vanet-gui/"]="integration-test -Pdesktop" )

# echo function $1 = type	(type 1 = err | 2 = suc | 3 = inf)
#				$2 = operation
#				$3 = target
#				$4 = offset
# if type given is 1 pretty_echo exits stops the script
pretty_echo () {
	[[ $1 = 1 ]] && dash_size=`expr $(echo $3 | wc -c) + 10` || dash_size=`echo $3 | wc -c`
	echo -e "\n\t\033[0;3${1}m$(printf "%${4}s%${dash_size}s"|tr " " "=")\n\t$([[ $1 = 1 ]] && echo "FAILED TO ${2^^}" || echo ${2^^}): $3\n\t$(printf "%${4}s%${dash_size}s"|tr " " "=")\033[0m\n"
	[[ $1 = 1 ]] && exit 1
}

is_dir() {
	local d
	for d in $dirs ; do [[ "$d" == "$1" ]] && return 0; done
	return 1
}


# ----- Main script

[[ $# -eq 0 || "$1" = "help" || "$1" = "-help" || "$1" = "-h" ]] && echo -e "Usage ./mvn_script  [ compile | install | <maven arguments> ]  <directory> [args] \n
    - For \033[4;29minstall\033[0m, \033[4;29mcompile\033[0m and \033[4;29mclean\033[0m in case no more
        arguments are given all directories will run given command:
            ($dirs)
    - If only the directory is given it runs \033[4;29mmvn clean compile exec:java\033[0m on it.

        NOTE: Its possible to give arguments, just put them after the directory name (directory must be valid
        otherwise the last argument to this script will be used as a directory)
    Example:
        ./mvn_script exec:java vehicle VIN1 vehicle1 0,0 1,1\n" && exit

echo -e "\n\t--VANET BMSCE FINAL YEAR PROJECT--\n"

if [ "$1" = "compile" -o "$1" = "install" -o "$1" = "clean" ] && [ $# -eq 1 ]; then  # Compile or install all or specified source dir
	[[ ! -z "$2" ]] && dirs=$2
	for i in $dirs; do
		pretty_echo 3 $1 $i 8 ;
		cd $i && mvn $1 ;
		[[ $? == 0 ]] && cd .. || pretty_echo 1 $1 $i 8
		pretty_echo 2 $1 $i 8
	done
else # specific commands for specified source dir with arguments
	dir_pos=$# # fixed value to prevent errors
	for (( a=1;a < $#;a++ )) { # check wich argument is dir
		is_dir ${!a} && dir=${!a} && dir_pos=$(($a)) && break
	}
	# if dir was defined... , else define dir as the last argument
	[[ ! -z $dir ]] && args="${@:$(($dir_pos+1)):$#}" || dir="${@: -1}"

	if [ $dir_pos -eq 1 ] ; then
		commands="compile exec:java"
		for key in "${!special_rules[@]}" ; do # apply special rules
			[[ "$dir" == "$key" ]] && commands="${special_rules[$key]}" ;
		done
	else
		commands="${@:1:$(($dir_pos-1))}"
	fi
	[[ ! -z $args ]] && mvn_args="-Dexec.args=\""$args"\"" # put arguments in maven format
	pretty_echo 3 "maven" $dir 6
# eval needed to run since arguments may have quotes
	cd "$dir" && echo -e "Running: \033[1;35;40m mvn $commands \033[0m on \033[1;34;40m $dir \033[0m" `[[ ! -z $args ]] && \
	echo "with args \033[1;33;40m" $args` "\033[0m\n" && \
	eval "mvn $commands $mvn_args";
	echo " - Finished. Press any key to terminate... - " && read;
	#[[ $? == 0 ]] && cd .. || pretty_echo 1 "run" $dir 4 ;
	pretty_echo 2 "maven" $dir 6
fi

echo -e "\Installation of CA RSU VEHICLES NETWORK ENVIORNMENT IS DONE \n"

