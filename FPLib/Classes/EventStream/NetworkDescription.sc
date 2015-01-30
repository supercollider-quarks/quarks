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

EventNetwork {
	var <actuate, //IO[Unit]
	<pause, //IO[Unit]
	<finalIOES,
	<active = false;

	//networkDescription : Writer( Unit, Tuple3([IO(IO())], [EventStream[IO[Unit]]], [IO] ) )
	//                                     eventHandlers         reactimates        IOLater
	*new{ |networkDescription, disableOnCmdPeriod = false|
		var tuple = if(networkDescription.class == Writer){
			networkDescription.w
		}{
			if(networkDescription.class == ENDef){
				networkDescription.resultWriter.w
			} {
				Error("EventNetwork: networkDescription must be either of class Writer or ENDef").throw
			}

		};

		//reactimates
		var finalIOES = tuple.at2.mreduce(EventSource);
		var f  = { |v| v.unsafePerformIO };
		var doFinalIO = IO{ finalIOES.do(f) };
		var stopDoingFinalIO = IO{ finalIOES.stopDoing(f) };
        var iosLater = tuple.at3.sequence(IO);

		//inputs
		var unregister;
        var registerIO = tuple.at1.sequence(IO) >>= { |x| IO{ unregister = x;
			Unit
		  } };
        var unregisterIO = IO{
            if(unregister.notNil) {
                unregister.do(_.unsafePerformIO )
            }
        };
		var actuate = doFinalIO >>=| registerIO >>=| iosLater;
        var pause = stopDoingFinalIO >>=| unregisterIO;

		var return = super.newCopyArgs( actuate, pause, finalIOES );

		if( disableOnCmdPeriod ) {
			CmdPeriod.add(return)
		};

		^return
	}


	*returnDesc { |a| ^Writer( a, Tuple3([],[],[]) ) }
    *returnUnit { ^this.returnDesc(Unit) }
	*makePure { |a| ^this.returnDesc(a) }

	start {
		if(active.not) {
			actuate.unsafePerformIO
		};
		active = true;
	}
	stop {
		if(active) {
			pause.unsafePerformIO
		};
		active = false;
	}

	cmdPeriod { this.pauseNow }

	//pick your favorite syntax:
	run { |bool|
		if( bool ) {
			this.actuateNow
		} {
			this.pauseNow
		}
	}

	*makeES { |addAction, removeAction|
        var addHandler;
		var es = EventSource();
		addHandler = IO{
			var action = { |sl| es.fire( sl.value ) };
			addAction.(action);
			IO{ removeAction.(action) }
		};
		^Writer( es, Tuple3([addHandler],[],[]) )
    }

}

+ Object {

	sinkValue { |signal|
    	^signal.collect{ |v| IO{ defer{ this.value_(v) } } }.reactimate;
    }

	sink { |signal, method|
    	^signal.collect{ |v| IO{ defer{ this.perform(*v) } } }.reactimate;
    }

	*sink { |signal, method|
    	^signal.collect{ |v| IO{ defer{ this.perform(*v) } } }.reactimate;
    }

}