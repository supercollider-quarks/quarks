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

Memory tests:

~consume = { |xs|
	0.01.wait;
	"ping".postln;
	~consume.( xs.tail );
};

None() of these tests should cause huge increases in memory

fork{
	~consume.( LazyList.iterate(0,{ 10000.collect{ Rect(1,1,1,1) } }) )
};

fork{
	~consume.( 0.iterate{ 10000.collect{ Rect(1,1,1,1) } }.zip(0.iterate{ 10000.collect{ Rect(2,2,2,2) } }) )
};

(
~consume2 = { |xs|
	0.1.wait;
	"ping".postln;
	~consume2.( xs.drop(10) );
};
fork{
	~consume2.( 0.iterate{ 10000.collect{ Rect(1,1,1,1) } } )
};
)

//this is blowing up
(
~consume = { |xs|
	0.1.wait;
	"ping".postln;
	~consume.( xs.tail );
};
fork{
	~consume.( 0.iterate{ 10000.collect{ Rect(1,1,1,1) } }.zip(0.iterate{ 10000.collect{ Rect(2,2,2,2) } }).value )
};
)

LazyListCons.class.findMedthod(\zip)

(
f = { |a,b|
	if( (a.isEmpty) || (b.isEmpty) ) {
		LazyListEmpty
	} {
		LazyListCons( Tuple2(a.head, b.head), { f.(a.tail, b.tail) } )
	}
};

~consume = { |xs|
	0.1.wait;
	"ping".postln;
	~consume.( xs.tail );
};
fork{
	~consume.( f.(0.iterate{ 10000.collect{ Rect(1,1,1,1) } }, 0.iterate{ 10000.collect{ Rect(2,2,2,2) } }) )
};


)

Hum, maybe should go from LazyListEmpty to LazyListEmpty() ? this way we can make methods that just use inject, collect, etc without having to add them both to LazyListEmpty and LazyListCons
*/


//note: because supercollider functions are eager, methods involving function could go
//terribly wrong.

//shortcut
LL {
    *new{ |...args|
        ^args.asLazy
    }
}

LazyList {

	isEmpty {  }

	notEmpty { ^this.isEmpty.not }

	match{ |fempty, fcons|	}

	take { |n| }

	drop { |n| }

	foldl { |start, f| }

	flatten { }

	asArray { }

	asLazy{ }

	collectAccum { |init, f|
		^this.foldl(T(init,LazyListEmpty), { |state,x|
			var at1 = state.at1;
			var at2 = state.at2;
			var y = f.(state.at1, x);
			T(y.at1, state.at2.add(y.at2) )
		})
	}

	scan { |init,f|
		^this.foldl(LazyListCons(init, LazyListEmpty), { |state,x|
			state.add( f.(state.last,x) )
		})
	}

	any { |f|
		^this.foldl(false, { |state,x|
			state || f.(x)
		})
	}

	includes { |x|
		^this.any(_ == x)
	}

	size {
		^this.foldl(0, _+1)
	}

	cycle {
		var f = { this.append({ { f.() } }) };
		^f.()
	}

	*repeat { |a|
		var g = { |a| LazyListCons(a, { this.repeat(a) }) };
		^g.(a)
	}

	*iterate { |a,f|
		var g = { |a,f| LazyListCons(a, { g.(f.(a), f) }) };
		^g.(a,f)
	}

	*replicate { |n,a|
		^LazyList.repeat(a).take(n)
	}

	*fromArray { |array|
		var l = LazyListEmpty;
		array.reverse.do{ |elem|
			l = LazyListCons(elem, l)
		};
		^l
	}
}

LazyListCons : LazyList {
	var <head, tailFunc, tailEvaluated;

	*new{ |head, tail|
		//if we already have a value store it in tailEvaluated otherwise store
		//the function tailFunc to be evaluated later.
		^if( tail.isKindOf( Function ) ) {
			super.newCopyArgs(head, tail)
		} {
			super.newCopyArgs(head, nil, tail)
		}
	}

	isEmpty { ^false  }

	tail {
		//memoization
		^tailEvaluated ?? { tailEvaluated = tailFunc.value; tailEvaluated }
	}

	match{ |fempty, fcons|
		^fcons.value(head, this.tail)
	}

	take { |n|
		^if(n <= 0) {
		    LazyListEmpty
		} {
			LazyListCons( head, this.tail.take(n-1) )
		}
	}

	takeWhile { |predicate|
		^if( predicate.( head ) ) {
			LazyListCons( head, this.tail.takeWhile(predicate) )
		} {
			LazyListEmpty
		}
	}

	drop { |n|
		^if(n<=0) {
			this
		} {
			this.tail.drop(n-1)
		}
	}

	dropWhile { |predicate|
		^if( predicate.( head ) ) {
			this.tail.dropWhile(predicate)
		} {
			this
		}
	}

	span { |predicate|
		^T(this.takeWhile(predicate), this.dropWhile(predicate))
	}

	ar {
	 ^this.asArray
	}

	asArray {
    	^this.prToArray([])
	}

	prToArray { |array|
		^this.tail.value.prToArray( array.add(this.head) )
	}

	//not checked for memory safety
	foldl { |start,f|
		^this.tail.foldl( f.(start, this.head), f)
	}

	//foldr tricky/impossible to implement since functions are
	//strict in SuperCollider.

	flatten {
		^this.head.append({ this.tail.flatten })
	}

	flop {
		^if( head.isEmpty ) {
			this.tail.flop
		} {
			LazyListCons( this.collect( _.head ), { this.collect( _.tail ).flop } )
		}
	}

	zip { |that|
		var f = { |a,b|
			if( (a.isEmpty) || (b.isEmpty) ) {
				LazyListEmpty
			} {
				LazyListCons( Tuple2(a.head, b.head), { f.(a.tail, b.tail) } )
			}
		};
		^f.(this, that)
	}

	*zip { |a, that|
		var f = { |a,b|
			if( (a.isEmpty) || (b.isEmpty) ) {
				LazyListEmpty
			} {
				LazyListCons( Tuple2(a.head, b.head), { f.(a.tail, b.tail) } )
			}
		};
		^f.(a, that)
	}

	collect { |f|
		^LazyListCons( f.(this.head), { this.tail.collect(f) })
	}

	do{ |f|
        f.(this.head); this.tail.do(f); ^Unit
	}

	select { |pred|
		^if( pred.(head) ) {
			LazyListCons( head, { this.tail.select(pred) } )
		} {
			this.tail.select(pred)
		}
	}

	//can't use ++ with LazyListEmpty
	append { |that|
		//append is supposed to be lazy on the list to append,
		//so either a LazyList was passed or a function was passed.
		^LazyListCons(this.head, { this.tail.append(that.value) } )
	}

	|+| { |that|
		^this.append(that)
	}

	*zero { ^LazyListEmpty }

	add { |that|
		^this.append( LazyListCons(that,LazyListEmpty) );
	}

	printOn { arg stream;
		var array = this.take(21).asArray;
		if( array.size == 21) {
			array.pop;
			stream << "LazyList[";
			array.printItemsOn(stream);
			stream << ",...]" ;
		} {
			stream << "LazyList" << array
		}
	}

}

LazyListEmpty : LazyList {

	*match{ |fempty, fcons|
		^fempty.value
	}

	*isEmpty { ^true }

	*take {}
	*asArray{ ^[] }
	*ar {
		^this.asArray
	}
	*prToArray { |array| ^array }
	*zip {}
	*drop {}
	*collect {}
	*select { }
	*append { |that|
		^that
	}
	*any { ^false }
	*includes { ^false }
	*size{ ^0 }
	*do{ ^Unit }
	|+| { |that|
		^this.append(that)
	}
	*zero { ^LazyListEmpty }
	*add { |that|
		^LazyListCons(that, LazyListEmpty)
	}

	*foldl { |start,f|
		^start
	}

	*flatten { }

	*transpose { }

}

+ Object {

	%%{ |that| ^LazyListCons(this, that) }

	//shortcuts
	repeatLL {
		^LazyList.repeat(this)
	}

	iterate { |f|
		^LazyList.iterate(this,f)
	}

	replicate { |n|
		^LazyList.replicate(n,this)
	}

}

+ Array {
	lz { ^LazyList.fromArray(this) }
    asLazy { ^LazyList.fromArray(this) }
	%% { |that| ^this.asLazyList.append(that) }
}

+ Stream {

	asLazyList {
		^LazyListCons( this.next, { this.asLazyList } )
	}

}

+ Pattern {

	asLazyList {
		^this.asStream.asLazyList
	}

}

