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

//you can set a value as an internal change which gets propagated via the ES returned by changes
// you can acess an ES that only propagates external changes
// this allows to connect two things that will send values to each other avoiding feedback.

HideVar : Var {
	var <justValues;

	*new { |now|
		^super.new( Tuple2(\external,now) ).initFeedbackVar
	}

	initFeedbackVar {
		justValues = changes.collect(_.at2 );
	}

	value_  { |x|
		^super.value_( Tuple2( \external, x) )
	}

	changes {
		^justValues
	}

	now {
		^super.now.at2
	}

	value { 
		^super.now.at2
	}

	//must be a Tuple2
	internalValue_ { |x|
		^super.value_( Tuple2( \internal, x) )
	}

	externalChanges {
		^changes.select{ |x| x.at1 == \external }.collect( _.at2 );
	}

	internalChanges {
    	^changes.select{ |x| x.at1 == \internal }.collect( _.at2 );
    }

	do{ |f|
		^justValues.do(f)
	}

	stopDoing { |f|
		^justValues.stopDoing(f)
	}

}