#!/bin/bash

# get terminal emulator application
if hash konsole 2>/dev/null ; then
	vterm="konsole --geometry 630x300"
elif hash gnome-terminal 2>/dev/null ; then
	vterm="gnome-terminal --geometry 74x10"
else
	vterm=xterm
fi


if [[ ! -z "$1" ]] ; then
	file="$1"
else
	file="profile_default.txt"
fi

function ctrl_c() {
	echo -e "\nDetected ctrl-c. Killing launched processes..."
	kill $(jobs -p)
	echo -e "\nRemoving revoked certificates generated..."
	git clean -f -q -- ca/cert/revoked/*
	exit
}

trap ctrl_c INT

echo -e "[+] Launching CA"
$vterm -e "./mvn_script.sh ca" >/dev/null 2>&1 &

echo "[+] Launching VANET"
$vterm -e "./mvn_script.sh vehicle-network" >/dev/null 2>&1 &

echo "[*] Press enter when both are running to launch RSU"
read

echo "[+] Launching RSU"
$vterm -e "./mvn_script.sh rsu" >/dev/null 2>&1 &

echo "[*] Press enter when rsu is running"
read

while echo -en "\r[*] Write arguments to launch vehicle with: " && read args; do
	if [[ ! "${args:0:1}" == "#" ]] ; then
		echo -e "\n[+] Launching vehicle with: $args"
		$vterm -e "./mvn_script.sh vehicle $args" >/dev/null 2>&1 &
		sleep 1 # maybe prevent some weirdness
	fi
done < <(cat $file -)
