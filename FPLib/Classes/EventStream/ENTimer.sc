/*
    FP Quark
    Copyright 2012 - 2013 Miguel Negrão.

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

    It is possible to add more type instances by adding the functions
    directly to the dict from the initClass function of the class that
    one wants to make an instance of some type class.
*/

ENTimer {
    var <delta, <maxTime;
    var <>action; //Option[Function]
    var <task, <t = 0;

    *new { |delta = 0.1, maxTime = inf|
        ^super.newCopyArgs(delta, maxTime).init;
    }

    init {
        task = Task({
            inf.do{
                delta.wait;
                if( t >= maxTime) {
                    task.stop;
                };
                t = t + delta;
                action.do{ |f| f.(t) };
            }
        });
    }

    start { |startTime=0|
		^IO{ t = startTime ? 0; task.start }
	}

	stop {
        ^IO{ task.stop };
	}

	pause {
        ^IO{ task.pause }

	}

	resume {
        ^IO{ task.resume }
	}
	actions {
        ^[ { |action| this.action_( Some(action) ) }, { this.action_(None()) } ]
	}

    asENInput {
        ^EventNetwork.makeES( *this.actions )
    }

}
