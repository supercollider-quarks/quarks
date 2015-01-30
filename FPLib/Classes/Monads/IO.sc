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

IO{
	classvar <>environmentVarForResult;

	var <func;
	*new{ |func| ^super.newCopyArgs(func) }

//Functor
    collect { |f| ^IO{ f.(this.value) } }

//Monad
    >>= { |f| ^IO{ f.(this.value).value } }
    *makePure{ |a| ^IO{ a } }

	unsafePerformIO{ ^func.value }
	value{ ^func.value }

//like ghci, calls unsafePerformIO on any IO returned
	*activate {

		thisProcess.interpreter.codeDump = { |str, val, func|
			//[str, val, func].postcs;
			//"environmentVarForResult is %".format(environmentVarForResult).postln;
			if( val.isKindOf(IO) ) {
				if( environmentVarForResult.notNil ) {
					if( environmentVarForResult[0] == $~ ) {
						topEnvironment.put(environmentVarForResult[1..].asSymbol, val.unsafePerformIO )
					} {
						if( environmentVarForResult.size == 1 ) {
							thisProcess.interpreter.perform( (environmentVarForResult++"_").asSymbol, val.unsafePerformIO)
						} {
							val.unsafePerformIO
						}
					}
				} {
					val.unsafePerformIO
				}
			}
		};
	}

	*deactivate {
		thisProcess.interpreter.codeDump = nil;
	}



}


+ Object {

	putStrLn { ^IO{ postln(this) } }

}

+ Window {
	frontIO {
		^IO{ this.front }
	}

	closeIO {
		^IO{ this.close }
	}

	setPropIO { |...args| //selector, args
		^IO{ this.performMsg(args) }
	}
}

+ View {
	frontIO {
		^IO{ this.front }
	}

	closeIO {
		^IO{ this.close }
	}

	setPropIO { |...args| //selector, args
		^IO{ this.performList(*args) }
	}
}

+ String {
    unixCmdIO {
        ^IO{ this.unixCmd }
    }
}


+ NetAddr {

    sendMsgIO { |...args|
        ^IO{ this.sendMsg(*args) }
    }

	sendBundleIO { |...args|
		^IO{ this.sendBundle(*args) };
	}
}

+ Node {
	setIO { arg ... args;
		^server.addr.sendMsgIO(15, nodeID, *(args.asOSCArgArray));  //"/n_set"
	}
}


