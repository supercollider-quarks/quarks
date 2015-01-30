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

	Validation -> Functional Error Handling
*/

// Validation[E, X]
Validation {

	match { |fsuccess, ffail| }

	isSuccess { }

	isFailure { ^this.isSuccess.not }

    //they are equal if they are both Success and a==b or if they are both Failure
    == { |that|
        ^if(that.isKindOf(Validation) ) {
            this.match(
                { |a|
                    that.match(
                        { |b|
                            a == b
                        }, {
                            false
                    })
                },{
                    that.match({ false },{true})
                }
            )
        } {
            false
        }
    }

//Functor
	collect { |f|
		^this.match( Success(_) <> f, { |e| Failure(e) } )
	}

//Monad
	>>= { |f|
		^this.match(f.(_), { |e| Failure(e) })
	}

    *makePure { |a| ^Success(a) }

//Applicative
	<*> { |x|
		^x.match({ |x|
			this.match({ |f|
				Success( f.(x) )
			}, { |e2| Failure(e2) })
		}, { |e1|
			this.match({ |f|
				Failure(e1)
			},{ |e2|
				Failure( e1 |+| e2 )
			})
		})
	}

}

Failure : Validation {
	var <e;

	*new { |e| ^super.newCopyArgs(e) }

	match { |fsuccess, ffail|
		^ffail.(e)
	}

	isSuccess { ^false }

	printOn { arg stream;
		stream << this.class.name << "( " << e << " )";
	}

    storeOn { arg stream;
		stream << this.class.name << "( " << e << " )";
	}
}

Success : Validation {
	var <x;

	*new{ |x| ^super.newCopyArgs(x) }

	match { |fsuccess, ffail|
		^fsuccess.(x)
	}

	isSuccess { ^true }

	printOn { arg stream;
		stream << this.class.name << "( " << x << " )";
	}

    storeOn { arg stream;
		stream << this.class.name << "( " << x << " )";
	}

}

+ Object {

	success {
		^Success(this)
	}

	fail {
		^Failure(this)
	}

	failLL {
		^Failure( this %% LazyListEmpty )
	}
}
