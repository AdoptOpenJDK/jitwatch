#!/bin/sh
# Displays lines with numbers from  $1 to $2 from the hotspot.log named in $3
# Possibly useful for tracking down parse errors
sed -n "$1,$2p" $3
