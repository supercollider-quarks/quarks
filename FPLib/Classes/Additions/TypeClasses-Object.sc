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

+ Object {

    *typeClassError { |class|
        ^Error("Class "++this.class++"is not type instance of "++class)
    }

//Functor
    //collect { }

//Applicative Functor
    //by default tries to use Monadic bind
    //if it doesn't have a bind you you'll get an error
    <*> {  |fa| ^this >>= { |g| fa.collect( g ) } }

    // a <* b : ignore value of b
	<* { |fb|
		^{|x| {|y| x } } <%> this <*> fb
	}

    // a *> b : ignire value of a
	*> { |fb|
		^{ |x| {|y| y } } <%> this <*> fb
	}

	//note: only functions with explicit variables can be curried
	// functions of type { |...args| cannot be curried }
	<%> { |a| ^a.collect( this.curried ) }
	//args is an Array or LazyList
	<%%> { |args|
        var largs = args.asLazy;
        ^largs.tail.inject(largs.head.fmap(this.curried), { |a,b| a <*> b })
    }

//Monad
    >>= { |f|  Object.typeClassError("Monad").throw }
    >>=| { |b| ^this >>= { b } }

    pure { |class| ^class.makePure(this) }
    *pure { |class| ^class.makePure(this) }

    join {
        ^this >>= { |x| x }
    }

//Monoid
	|+| { |b| Object.typeClassError("Monoid").throw }
    //args can be used for hints about the type of the zero
	*zero { |args| Object.typeClassError("Monoid").throw }

//Traverse
    traverse { |f, type| Object.typeClassError("Traverse").throw }

    sequence { |type| ^this.traverse({|x| x}, type) }

    //F[_] : Applicative, A, B,  f: A => F[Unit], g: A => B
	collectTraverse { |f, g|
		^this.traverse({ |a|
			var fa = f.(a);
			{ g.(a) }.pure(fa.getClass) <*> f.(a)
		})
	}

	//F[_] : Applicative, A, B, C, f: F[B], g: A => B => C
	disperse { |f,g|
		^this.traverse({ |a| g.curried.(a).pure(f.getClass) <*> f })
	}

//Utils

	constf { ^{ |x| this } }

    *getClass {
		^if( this.class.isMetaClass ) {
			this
		} {
			this.class
		};
	}

	getClass {
		^if( this.class.isMetaClass ) {
			this
		} {
			this.class
		};
	}


}


/*
[1,2,3,4].injectr([],{ |s,x| s++[x]})
[Some(1), Some(2), Some(3)].sequenceMonad
[Some(1), Some(2), None()].sequenceMonad
[Some(1), Some(2)].sequenceMonad
[Some(1)].sequenceMonad
[].sequenceMonad(Option)
*/

//Instances
+ Array {

//Monad
    >>= { |f| ^this.collect(f).flatten }
    *makePure { |a| [a] }


//Monoid
    |+| { |b| ^this ++ b }
    *zero { ^[] }
	mreduce { |class|
		^if(this.size == 0) {
			class.zero
		} {
			this.inject(this.at(0).class.zero, _ |+| _ )
		}
	}

//Monad related
    sequenceM { |monadClass|
        ^if(this.size == 0 ) {
            //with an empty list we need a hint in order to know which monad to use
            this.pure(monadClass)
        }{
            if(this.size == 1) {
                this.at(0).collect{ |x| [x] }
            } {
                var end = this.size-2;
                this[0..end].injectr(this.last.collect{ |x| [x] }, { |mstate,m|
                    m >>= { |x| mstate.collect{ |y| [x]++y } }
                })
            }
        }

    }

    sequenceM_ { |monadClass|
		var end;
        ^if(this.size == 0 ) {
            //with an empty list we need a hint in order to know which monad to use
            Unit.pure(monadClass)
        }{
            if(this.size == 1) {
                this.at(0).collect{ |x| [x] }
            } {
                end = this.size-2;
                this[0..end].injectr(this.last.collect{ Unit }, { |mstate,m|
                    m >>=| mstate
                })
            }
        }
    }

//Traverse
    traverse { |f, applicativeClass|
		var end;
        ^if(this.size == 0 ) {
            //with an empty list we need a hint in order to know which monad to use
            this.pure(applicativeClass)
        }{
            if(this.size == 1) {
                f.( this.at(0) ).collect{ |z| [z] }
            } {
                end = this.size-2;
                this[0..end].injectr( f.( this.last ).collect{ |z| [z] }, { |ys,v|
                    f.(v).collect({ |z| { |zs| [z]++zs } }) <*> ys
                })
            }
        }
    }


	*asDictFromTuples {
		^Dictionary.with(*this.collect{ |tup| tup.at1 -> tup.at2 })
	}
}


//(1:Some(2), 3:Some(4)).sequence
+ Dictionary {

	traverseWithKey { |f, class|
		var x = this.asSortedArray;
		var y = x.traverse( { |xs| { |b| [xs[0],b] } <%> f.(*xs) }, class);
		^{ |array| Dictionary.with(*array.collect(_->_)) } <%> y
	}

	traverse { |f, class|
		var x = this.asSortedArray;
		var y = x.traverse( { |xs| { |b| [xs[0],b] } <%> f.(xs[1]) }, class);
		^{ |array| this.class.with(*array.collect{ |xs| xs[0]->xs[1] }) } <%> y
	}

	//(1:2) |+| (3:4)
	//[(1:2), (3:4)].mreduce
	//[(1:2), ()].mreduce
	*zero{
		^this.new
	}

	|+|{ |b|
		^this.merge(b,{|x,y| x |+| y})
	}

	//(1:2,3:4).toTupleArray
	toTupleArray {
		^this.asSortedArray.collect{ |xs| T(xs[0], xs[1]) }
	}

	//Dictionary.fromTupleArray([T(1,2),T(3,4)])
	*fromTupleArray { |array|
		^this.with(*array.collect{ |tup| tup.at1 -> tup.at2 })
	}

}

+ SimpleNumber {

//Monoid
    |+| { |b|
        ^this + b
    }

    *zero { ^0 }
}

+ String {

//Monoid
    |+| { |b|
        ^this ++ b
    }

    *zero { ^"" }
}

+ Boolean {

//Monoid
    |+| { |b|
        ^this && b
    }

    *zero { ^true }
}

