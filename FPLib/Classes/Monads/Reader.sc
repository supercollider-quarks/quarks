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

Reader{
	var <func; // r -> a

	run { |...args|
		^func.value(*args)
	}

	*new{ |f| ^super.newCopyArgs(f) }

	*ask { ^Reader( I.d ) }

	//change the environment locally
	local { |f| ^Reader( func <> f ) }

//Functor
	collect{ |f| ^Reader( { |r| f.( func.(r) ) } ) }

//Monad
    *makePure { |a|
        ^Reader({ a })
    }

	>>= { |f| ^Reader( { |r| f.(func.(r)).func.(r) } ) }

}

/*

f = Reader({ |x| x +1 });
g = Reader({ |y| y*2 });
(5.pure(Reader) >>=| f >>=| g).func.value

(f >>=).run(1)

*/