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
*/


ST {
  	var <f;

  	//f: S => (S, A)
  	*new { |f| ^super.newCopyArgs(f) }

  	*put { |newState|
  		^ST({ |oldState| T(newState,Unit) })
  	}

    *get { ^ST({ |s| T(s,s) }) }

  	runState { |s| ^f.(s) }
  	evalState { |s| ^this.runState(s).at2 } //return the result
  	execState { |s|^this.runState(s).at1 } //return the state

	//g:S => S
	withState { |g| ^ST(f <> g )  }

//Functor
    collect { |f|
        ^ST({ |s|
            var stuple = this.f.(s);
            T( stuple.at1, f.(stuple.at2) )
        })
    }

//Monad
    >>= { |f|
        ^ST({ |s|
            var satuple = this.f.(s);
            f.(satuple.at2).runState(satuple.at1)
        })
    }

    *makePure { |r|
        ^ST({ |s| T(s,r) })
    }


}
