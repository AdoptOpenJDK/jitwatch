#!/bin/sh
# Displays lines in the range $1 to $2 from hotspot.log
# Possibly useful for tracking down parse errors
sed -n "$1,$2p" hotspot.log

