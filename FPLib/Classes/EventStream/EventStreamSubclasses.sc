/*
    FP Quark
    Copyright 2012 Miguel Negrão.

    FP Quark: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    FP Quark is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with FP Quark.  If not, see <http://www.gnu.org/licenses/>.


	For comprehensions based on scala's for and haskell's do.

*/


EventPlayerES : EventSource {
    var <routine;
	/*
		arrays is [[time,value],...]
	
	*/
    *new{ |array, loop = true|
        ^super.new.init(array, loop)
    }

    init { |array, loop|

        var t;
        var f = {
	        t = 0;
	        array.do{ |tx|
	        	( tx[0] -t ).wait;
	        	this.fire( tx[1] );
	        	t = tx[0];
        	}
        };
        routine = fork{
	     if(loop) {
		     inf.do{ f.value };
	     } {
		     f.value
	     }        	
       }
    }

	remove {
		routine.stop;
	}
}

TimerES : EventSource {
    var <routine;

    *new{ |delta, maxTime|
        ^super.new.init(delta, maxTime ? inf)
    }

    init { |delta, maxTime|

        var t = 0;
        routine = fork{
            inf.do{
                delta.wait;
                if( t >= maxTime) {
                    routine.stop;
                };
                t = t + delta;
                this.fire(t);
            }
        }

    }

    remove {
    	routine.stop;
    }
}


WaitES : EventSource {
    var <routine;

    *new{ |waitTime, value|
        ^super.new.init(waitTime, value)
    }

    init { |waitTime, value|
	   SystemClock.sched(waitTime, { this.fire(value) })
    }
}
