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

    It is possible to add more type instances by adding the functions
    directly to the dict from the initClass function of the class that
    one wants to make an instance of some type class.

    Writer Monad
*/

Writer {
	var <a, <w; // ( A, W )
	//a is the main value
	//w is the annotation monoid

	*new { |a,w| ^super.newCopyArgs(a,w) }

	runWriter { ^Tuple2(a,w) }

	execWriter { ^this.runWriter.at2 }

	tell { |w2|
		^Writer(a, w |+| w2)
	}

	*tell { |w|
		^Writer( Unit, w)
	}

	printOn { arg stream;
		stream << this.class.name << "( " << a << ", " << w << " )";
	}

//Functor
	collect { |f| ^Writer( f.(a), w ) }

//Monad
	>>= { |f|
		var k = f.(a);
        if( k.class == this.class) {
            ^Writer( k.a, w |+| k.w )
        } {
            Error(">>= class mismatch: this: % that %".format(this, k) ).throw
        }
	}

    *makePure { |a,class|
        Writer(a, class !? _.zero ?? {[]}) //where to store the zero ?
    }

}

