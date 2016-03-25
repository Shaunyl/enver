#!/usr/bin/ksh -x

DIR=$PWD/backup

MAIL_LIST="filippo.testino@consulenti.fastweb.it"
MAIL_FILE=backup_report_mail-`date "+%d%m%H%M"`.txt
MAIL_PATH=$DIR/mail/$MAIL_FILE
LOG_RETENTION=7
MULTI=y
hosts=1

if [ "$MULTI" = "y" ]; then
 hosts=$(cat ./config/multidb.cfg | sed '/^\s*#/d;/^\s*$/d' | wc -l | awk '{print $1}')
fi

echo "Backup Report" > $MAIL_PATH
echo "" >> $MAIL_PATH
echo "Starting MonBak on $hosts hosts at $(date '+%d-%b-%Y %X')" >> $MAIL_PATH

echo "Threshold used (inclusive) -> $THRESHOLD" >> $MAIL_PATH
echo "" >> $MAIL_PATH

java -cp lib/*:. -jar lib/enver-0.91.4-ALPHA.jar monbako -multi=$MULTI -directory=$DIR

echo "Backup check:" >> $MAIL_PATH
results=$(egrep -ih "RMAN-|ORA-" $DIR/*)
if [ ! -n "$results" ]; then
 echo "> no issues found." >> $MAIL_PATH
else
 echo "$results" >> $MAIL_PATH
fi
echo " " >> $MAIL_PATH

echo "Errors during execution:" >> $MAIL_PATH
fails=$(./grep.pl "aborted" 4 enverMONBAKO.log)
if [ ! -n "$fails" ]; then
 echo "> nope, all databases have been checked successfully.." >> $MAIL_PATH
else
 echo " " >> $MAIL_PATH
 echo "$fails" >> $MAIL_PATH
fi
echo " " >> $MAIL_PATH

echo "MonBak completed at $(date '+%d-%b-%Y %X')" >> $MAIL_PATH
(printf "%s\n" "MonBak Report in the attachment..!" ; uuencode $MAIL_PATH $MAIL_FILE) | mailx -r "oracle@intranet.it" -s "Backup Check" ${MAIL_LIST}
mv $DIR/*.txt $DIR/log
find $DIR/log/ -name *.txt -mtime "+$LOG_RETENTION" -a -exec rm -f {} \; >/dev/null
