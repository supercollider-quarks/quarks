/*
    Unit Library
    The Game Of Life Foundation. http://gameoflife.nl
    Copyright 2006-2011 Miguel Negrao, Wouter Snoei.

    GameOfLife Unit Library: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    GameOfLife Unit Library is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with GameOfLife Unit Library.  If not, see <http://www.gnu.org/licenses/>.
*/

UScoreList : UEvent {
    var <scores, <metaScore;

    *new { |...scores|
        ^super.new.init(scores)
    }

    init { |inScores|
        var currentEnd = inScores[0].duration;
        scores = inScores;

        metaScore = UScore(*scores.collect{ |score,i|
            if(i>0) {
                score.startTime_(currentEnd);
                currentEnd = currentEnd + score.duration;
            };
            score
        });
    }
    
      at { |...path| 
		 var out;
		 out = scores;
		 path.do({ |item|
			 out = out[ item ];
		 });
		 ^out
	}
     copySeries { |first, second, last| ^scores.copySeries( first, second, last ) }
	collect { |func|  ^scores.collect( func );  }
	do { |func| scores.do( func ); }
	last { ^scores.last }
	first { ^scores.first }
	indexOf { |obj|
		var index;
		index = scores.indexOf( obj );
		if( index.isNil ) {
			scores.do({ |item, i|
				index = [ i, item.indexOf( obj ) ];
				if( index[1].notNil ) { ^index; }
			});
			^nil;
		} {
			^index
		};
	}

    name { ^scores.collect(_.name).asString }

    waitTime { ^scores.collect(_.waitTime).sum }

    prepare { |target, startPos = 0, action|
        metaScore.prepare(target, startPos, action)
    }

    prepareAndStart{ |target, startPos = 0|
        metaScore.prepareAndStart(target, startPos)
    }

    prepareWaitAndStart { |target, startPos = 0|
        metaScore.prepareWaitAndStart(target, startPos)
    }

    start { |target, startPos, latency|
        metaScore.start(target, startPos, latency)
    }

    stop { |releaseTime, changed = true|
        metaScore.stop(releaseTime,changed)
    }

    pause { metaScore.pause }

    resume { |targets| metaScore.resume(targets) }

    release{ ^this.stop }

    gui { }
    
    storeArgs { ^scores }

}