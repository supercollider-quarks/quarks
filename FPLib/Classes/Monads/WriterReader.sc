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

// Inspired on http://hackage.haskell.org/packages/archive/mtl/1.1.0.2/doc/html/src/Control-Monad-Reader.html

WriterReader{
	var <func; // r -> (a,w)

	*new { |f| ^super.newCopyArgs(f) }

	*ask { |class| ^WriterReader( Tuple2(_,class.zero) ) }

	run { |...args|
		^func.value(*args)
	}

	//change the environment locally
	local { |f| ^Reader( func <> f ) }

	collect { |f|
		^WriterReader( { |r|
			var aw = func.(r);
			Tuple2(f.(aw.at1), aw.at2)
		} )
	}

	>>= { |f|
		^WriterReader( { |r|
			var aw1 = func.(r);
			var aw2 = f.(aw1.at1).run(r);
			Tuple2(aw2.at1, aw1.at2 |+| aw2.at2)
		} )
	}

    *makePure { |a|
        ^WriterReader( { Tuple2(a, [] ) } )  //which class to use for zero here ??
    }

	tell { |x|
		^Reader( { |r|
			var aw = func.(r);
			Tuple2(aw.at1, aw.at2 |+| x)
		} )
	}

}

+ Writer {

	asWriterReader {
		^WriterReader( { |x| Tuple2(a,w) } )
	}
}


/*

5.pure(WriterReader).tell(["hello"]).run(3)

WriterReader.ask(Array).run(3)
WriterReader.ask(Array).tell(["abc9"]).run(3)
WriterReader.ask(Array).tell(["abc9"]).local(_+1).run(3)


*/
