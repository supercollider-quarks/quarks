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

class variable access is faster then array access

a = [0];
b = Point(0,0)
bench{ 10000000.do{ a.at(0) } } //24% slower
bench{ 10000000.do{ b.x } }

mutation of array is faster then constructing new Tuple


x = [1,2,3]
(
bench{
1000000.collect{
	x[1]
}
}
)
//0.27262902259827

//change
bench{
1000000.do{
	x[1] =4
}
}
//0.10011911392212




x = T(1,2,3);

//access
(
bench{
1000000.collect{
	x.at2
}
}
)
//0.18798518180847

//change
bench{
1000000.do{
	x.at2 = 4
}
}
//0.3977358341217

*/

//Immutable Tuples

//An easy to type constructor for tupples
T{

   *new{ |...args|
        var class = switch(args.size)
        {2}{Tuple2}
        {3}{Tuple3}
        {4}{Tuple4};
        ^class.new(*args)
    }
}

Tuple2{
	var <at1, <at2;

	*new { |at1, at2|
		^super.newCopyArgs(at1, at2)
	}

	== { |tuple|
        ^if(tuple.isKindOf(Tuple2) ) {
            (this.at1 == tuple.at1) && (this.at2 == tuple.at2)
        } {
            false
        }
	}

	printOn { arg stream;
		stream << "(" << at1 << ", " << at2 << ")"
	}

    at1_{ |v|
        ^Tuple2(v,this.at2)
    }

    at2_{ |v|
        ^Tuple2(this.at1,v)
    }


//Functor
	collect { |f|
		^Tuple2( f.(at1), f.(at2) )
	}

//Monoid
    |+| { |b| ^Tuple2( this.at1 |+| b.at1, this.at2 |+| b.at2 ) }
    *zero { |class1, class2| ^Tuple2( class1.zero, class2.zero) }

//Traversable
	//T(1, Some(2)).sequence
	traverse { |f|
		^{ |fa| Tuple2(this.at1, fa) } <%> f.(this.at2)
	}
	//math
	+ { |b| b = b.asTuple2; ^Tuple2( this.at1 + b.at1, this.at2 + b.at2 ) }
	- { |b| b = b.asTuple2; ^Tuple2( this.at1 - b.at1, this.at2 - b.at2 ) }
	* { |b| b = b.asTuple2; ^Tuple2( this.at1 * b.at1, this.at2 * b.at2 ) }
	/ { |b| b = b.asTuple2; ^Tuple2( this.at1 / b.at1, this.at2 / b.at2 ) }

	asTuple2{
		^this
	}
}

Tuple3{
	var <at1, <at2, <at3;

	*new { |at1, at2, at3|
		^super.newCopyArgs(at1, at2, at3)
	}

	== { |tuple|
		^if(tuple.isKindOf(Tuple3) ) {
            (this.at1 == tuple.at1) && (this.at2 == tuple.at2) && (this.at3 == tuple.at3)
        } {
            false
        }
	}

	printOn { arg stream;
		stream << "(" << at1 << ", " << at2 << ", " << at3 <<")"
	}

    at1_{ |v|
        ^Tuple3(v, this.at2, this.at3)
    }

    at2_{ |v|
        ^Tuple3(this.at1, v, this.at3)
    }

    at3_{ |v|
        ^Tuple3(this.at1, this.at2, v)
    }

//Functor
	collect { |f|
		^Tuple3( f.(at1), f.(at2), f.(at3) )
	}

//Monoid
    |+| { |b| ^Tuple3( this.at1 |+| b.at1, this.at2 |+| b.at2, this.at3 |+| b.at3 ) }
    *zero { |class1, class2, class3| ^Tuple3( class1.zero, class2.zero, class3.zero) }
}

Tuple4{
	var <at1, <at2, <at3, <at4;

	*new { |at1, at2, at3, a4|
		^super.newCopyArgs(at1, at2, at3, a4)
	}

	== { |tuple|
        ^if(tuple.isKindOf(Tuple2) ) {
            (this.at1 == tuple.at1) && (this.at2 == tuple.at2) && (this.at3 == tuple.at3) && (this.at4 == tuple.at4)
        } {
            false
        }
	}

	printOn { arg stream;
		stream << "(" << at1 << ", " << at2 << ", " << at3 << ", " << at4 <<")"
	}

//Functor
	collect { |f|
		^Tuple4( f.(at1), f.(at2), f.(at3), f.(at4) )
	}

//Monoid
    |+| { |b| ^Tuple3( this.at1 |+| b.at1, this.at2 |+| b.at2, this.at3 |+| b.at3, this.at4 |+| b.at4 ) }
    *zero { |class1, class2, class3, class4| ^Tuple4( class1.zero, class2.zero, class3.zero, class4.zero) }
}

+ SimpleNumber {

	asTuple2{
		^Tuple2(this, this)
	}

	asTuple3{
		^Tuple2(this, this, this)
	}

	asTuple4{
		^Tuple2(this, this, this, this)
	}

	asTuple5{
		^Tuple2(this, this, this, this, this)
	}

}