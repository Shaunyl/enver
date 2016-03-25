#!/bin/ksh

function check_os {
    export OS=$(uname)
    case "$OS" in
	"HP-UX" | "Linux")
	    export dfcmd="df -kP"
	    ;;
	"SunOS")
	    export dfcmd="df -k" 
	    ;;
    esac
}

check_os

if [ ! "$1" ]; then   
   threshold="83"
else
   threshold="$1"      
fi    

if [ $2 ]; then
   pattern=$($dfcmd | egrep -h "$2|Filesystem")
else
   pattern=$($dfcmd | egrep -v nulnulnul)
fi
  
i=2
y=1
result=`echo "$pattern" | grep -v "Filesystem" | awk '{ print $5 }' | sed 's/%//g'`

for percent in $result; do
  if ((percent >= threshold))
  then
    set -- $(echo `echo "$pattern" | head -$i | tail -1` | awk '{print $3" "$4" "$6}')
    used=$(echo "scale=3;$1/1024" | bc -l)
    avail=$(echo "scale=3;$2/1024" | bc -l)
    mountpoint=$3
	mountused="$3[$used]"
    printf "  %s		%-30s		%d%%	(%f)\n" $(hostname) $mountused $percent $avail
    y=0
  fi
    let i=$i+1
done

if [ $y = "1" ]; then
  echo "  > All filesystems are subthreshold."
fi