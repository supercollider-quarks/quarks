#!/bin/bash

# "scpodcast" bash script - uses SuperCollider to generate sound files and then podcasts them.
# (c) 2007 Dan Stowell
# This is free software under the terms of the GNU General Public License
# See http://www.gnu.org/copyleft/gpl.html for details of your rights to redistribute
# See SCPodcast.html for some info about using the script

set -o nounset
set -o errexit


if [ $# -ne 7 ]
then
  echo "\"scpodcast\" script for automated SuperCollider scsynth podcasting"
  echo "    (c) 2007 Dan Stowell"
  echo "Usage: scpodcast readdir writedir baseurl maxentries mp3prefix oscfname itemtitleprefix"
  echo "    'readdir' should contain: the binary OSC file; and rsstop.txt"
  echo "         (the upper RSS fragment)"
  echo "    'writedir' will contain the RSS and MP3s, plus various aux files."
  echo "         (NB files will be added/DELETED from this dir; please make an"
  echo "         empty folder to use, and preferably don't delete the aux files "
  echo "         that are created.)"
  echo "    'baseurl' should be the public URL used to access 'writedir'"
  exit 10;   # $WRONG_ARGS
fi


echo " --- scpodcast started at `date`"

#### Some values which you may need to change - paths to executables
#### However, the script uses "which" to try and find the executables if they aren't at these locations
scsynth='./scsynth'
lame='/sw/bin/lame';

if [ ! -e "$lame" ]; then
	if [ -e '/opt/local/bin/lame' ]; then
		lame='/opt/local/bin/lame'
	else
		echo "Searching for lame..."
		lame=`which lame`
		if [ -z "$lame" ] || [ "${lame:0:3}" = "no " ]; then
			echo "ERROR in scpodcast.bash: Can't find \"lame\" executable."
			exit 7;
		else
			echo "...found at $lame"
		fi
	fi
fi
if [ ! -e "$scsynth" ] || [ "${scsynth:0:3}" = "no " ]; then
	echo "Searching for scsynth..."
	scsynth=`which scsynth`
	if [ -z "$scsynth" ]; then
		echo "ERROR in scpodcast.bash: Can't find \"scsynth\" executable."
		exit 8;
	else
		echo "...found at $scsynth"
	fi
fi



# Get the params into humane vars
readdir=$1
writedir=$2
baseurl=$3
let "maxentries=$4 + 1"  # Add the one now, for convenience
mp3prefix=$5
oscfname=$6
itemtitleprefix=$7

# The date is NOW
dateforfname=`date +'%Y-%m-%e_%H-%M-%S' | sed 's/ /0/g'`
dateforhuman=`date +'%a %b %e %H:%M:%S'`
dateforitempubdate=`date +'%a, %e %b %Y %H:%M:%S %z' | sed 's/  / /g'`

# Create other vars based on the params
oscpath="${readdir}/${oscfname}"
temprsspath="${writedir}/tmp.rss"
realrsspath="${writedir}/podcast.rss"
thismp3name="${mp3prefix}${dateforfname}"  # Used in a few places



################ Checking for input files' existence ################

if [ -f "$oscpath" ]; then 
  echo "OSC file:    found OK";
else
  echo "OSC file not found: $oscpath"; exit 3;
fi;


if [ -f "${readdir}/rsstop.txt" ]; then 
  echo "rsstop file: found OK";
else
  echo "rsstop file not found: ${readdir}/rsstop.txt"; exit 4;
fi;

#echo "Planning to use OSC file $oscpath to create files with prefix $thismp3name and then RSS file $temprsspath"
#echo "maxentries = $maxentries"
#exit 0;

################ Audio generation ################

# BTW should I catch errors from scsynth/lame? e.g. if scsynth can't find/execute the OSC file


# First run scsynth to generate an AIFF - NB here it's hardcoded to 44kHz stereo

#echo "$scsynth -N \"${readdir}/$oscfname\" _ \"${writedir}/tmp_new.aif\" 44100 AIFF int16 -o 2"
echo "Running scsynth"
$scsynth -N "${readdir}/$oscfname" _ "${writedir}/tmp_new.aif" 44100 AIFF int16 -o 2 || # { echo "scsynth error"; exit 1; }

# Then run LAME to convert to MP3, and delete the AIFF
#echo "$lame -V 4 --silent \"${writedir}/tmp_new.aif\" \"${writedir}/${thismp3name}.mp3"
echo "Running LAME"
$lame -V 8 -q 7 --silent "${writedir}/tmp_new.aif" "${writedir}/${thismp3name}.mp3" 

rm "${writedir}/tmp_new.aif"

# exit 0;


# At this point we also create an XML fragment representing the file's RSS entry
# NB It's still a bit tricky to find the file size
# Using ls -sk, we get filesizes in kb - maybe that's the best way?
# Using du -k    better (still need to times by 1024)
# Here's the answer: cat the file into "wc -c", gives you a number in bytes
itemfilesize=`cat "${writedir}/${thismp3name}.mp3" | wc -c`

itemfragment="<item><title>${itemtitleprefix}${dateforhuman}</title><link>${baseurl}/${thismp3name}.mp3</link><guid>${baseurl}/${thismp3name}.mp3</guid><description>Generated on $dateforhuman using SuperCollider+SCPodcast</description><enclosure url='${baseurl}/${thismp3name}.mp3' length='${itemfilesize}' type='audio/mpeg'/><pubDate>$dateforitempubdate</pubDate></item>"

# echo "$itemfragment"; exit 0


echo "$itemfragment" > "${writedir}/${thismp3name}.item.xml"



# Now check the number of MP3s and delete if we have too many
# e.g. this deletes all but the most recent SEVEN:
#    ls -t1 *.mp3 | tail -n +8 | xargs rm
echo "Deleting old files (if any):"
echo "-----------------------------------------";
ls -t1 "${writedir}"/*.mp3      | tail -n +$maxentries | sed 's/ /\\ /g' | xargs echo;
ls -t1 "${writedir}"/*.item.xml | tail -n +$maxentries | sed 's/ /\\ /g' | xargs echo;
echo "-----------------------------------------";
ls -t1 "${writedir}"/*.mp3      | tail -n +$maxentries | sed 's/ /\\ /g' | xargs rm
ls -t1 "${writedir}"/*.item.xml | tail -n +$maxentries | sed 's/ /\\ /g' | xargs rm


############### RSS feed creation ################

# RSS header
cat "${readdir}/rsstop.txt" > "$temprsspath"


feedpubdate=`date +'%a, %e %b %Y %H:%M:%S %z' | sed 's/  / /g'`

echo "<pubDate>$feedpubdate</pubDate>"  >> "$temprsspath"


# RSS items
# This lists the MP3s in descending order:
#    ls -t1 *.mp3
writediresc=`echo "$writedir" | sed 's/ /\\ /g'`
#echo "ls -t1 \"$writediresc\"/*.item.xml | sed 's/ /\\ /g'"
#for itemfname in `ls -t1 \"$writediresc\"/*.item.xml | sed 's/ /\\ /g'`; do
#for itemfname in `find "$writediresc" -name "*.item.xml" | basename | sed 's/ /\\ /g'`; do
for itemfname in `find "$writediresc" -name "*.item.xml" -exec basename {} \; | sed 's/ /\\ /g'`; do

  echo "Concatenating ${writediresc}/${itemfname}"
  echo >> "$temprsspath"
  cat "${writediresc}/${itemfname}" >> "$temprsspath"
  echo >> "$temprsspath"

done

# RSS footer
echo '</channel></rss>' >> "$temprsspath"

# Move the generated RSS file to the official location
mv "$temprsspath" "$realrsspath"

echo " --- scpodcast completed at `date`"

exit 0; # Success