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

FRPGUIProxy {
	//Option[QView]
	var <view, default;
	//Option[Function]
	var <f;

	*new { |view, default = 0.0|
		^super.newCopyArgs(view.asOption, default)
	}

	addAction { |g|
		f = Some(g);
		view.collect{ |x| x.action_( x.action.addFunc(g) ) }
	}

	removeAction { |g|
		f = Some(g);
		view.collect{ |x| x.action_( x.action.removeFunc(g) ) }
	}

	asENInput {
		^FRPGUICode.makeENInput(this)
	}

	view_ { |newView|
		if(newView.isNil){ Error("FRPGUIProxy#view_ : newView is nil").throw };
		{ |view, g| view.action_( view.action.removeFunc(g) ) } <%> view <*> f;
		{ |g| newView.action_( newView.action.addFunc(g) ) } <%> f;
		view = Some(newView)
	}

	removeView {
		{ |view, g| view.action_( view.action.removeFunc(g) ) } <%> view <*> f;
		view = None();
	}

	value {
		^(_.value <%> view).getOrElse(default)
	}

}

FRPGUICode {

	*makeENInput { |gui|
		var addHandler;
		var es = Var(gui.value);
		var action = { |sl| es.value_( sl.value ) };
		addHandler = IO{
			gui.addAction(action);
			IO{ gui.removeAction(action)
		} };
		^Writer( es, Tuple3([addHandler],[],[IO{ action.value(gui) }]) )
	}

	*makeENInputES { |gui|
		var addHandler;
		var es = EventSource();
		var action = { |sl| es.fire( sl.value ) };
		addHandler = IO{
			gui.addAction(action);
			IO{ gui.removeAction(action)
		} };
		^Writer( es, Tuple3([addHandler],[],[]) )
	}

}

+ Node {

	setSink { |key, signal|
		^signal.collect{ |v| this.setIO(key,v) }.reactimate
	}

	enSetSink { |key, signal|
		ENDef.appendToResult( this.setSink(key, signal) );
	}
}

+ OSCFunc {

	*asENInputES { |path, srcID, recvPort, argTemplate, dispatcher|
        var es = EventSource();
        var addHandler = IO{
            var f = { |msg| es.fire(msg) };
            var osc = OSCFunc(f, path, srcID, recvPort, argTemplate, dispatcher);
			IO{ osc.free }
		};
        ^Writer( es, Tuple3([addHandler],[],[]) )
    }

	*asENInput { |path, srcID, recvPort, argTemplate, dispatcher, initialValue|
		var es = Var(initialValue);
        var addHandler = IO{
            var f = { |msg| es.value_(msg) };
            var osc = OSCFunc(f, path, srcID, recvPort, argTemplate, dispatcher);
			IO{ osc.free }
		};
        ^Writer( es, Tuple3([addHandler],[],[]) )
    }

	*enIn { |path, srcID, recvPort, argTemplate, dispatcher, initialValue|
		^ENDef.appendToResult( this.asENInput(path, srcID, recvPort, argTemplate, dispatcher, initialValue) );
	}

	*enInES{ |path, srcID, recvPort, argTemplate, dispatcher|
		^ENDef.appendToResult( this.asENInputES(path, srcID, recvPort, argTemplate, dispatcher) );
	}

	*asENInputESFull { |path, srcID, recvPort, argTemplate, dispatcher|
        var es = EventSource();
        var addHandler = IO{
            var f = { |...args| es.fire(args) };
            var osc = OSCFunc(f, path, srcID, recvPort, argTemplate, dispatcher);
			IO{ osc.free }
		};
        ^Writer( es, Tuple3([addHandler],[],[]) )
    }

	*asENInputFull { |path, srcID, recvPort, argTemplate, dispatcher, initialValue|
		var es = Var(initialValue);
        var addHandler = IO{
            var f = { |...args| es.value_(args) };
            var osc = OSCFunc(f, path, srcID, recvPort, argTemplate, dispatcher);
			IO{ osc.free }
		};
        ^Writer( es, Tuple3([addHandler],[],[]) )
    }

	*enInFull { |path, srcID, recvPort, argTemplate, dispatcher, initialValue|
		^ENDef.appendToResult( this.asENInputFull(path, srcID, recvPort, argTemplate, dispatcher, initialValue) );
	}

	*enInESFull{ |path, srcID, recvPort, argTemplate, dispatcher|
		^ENDef.appendToResult( this.asENInputESFull(path, srcID, recvPort, argTemplate, dispatcher) );
	}
}

+ QView {

	asENInput {
		^FRPGUICode.makeENInput(this)
	}

	asENInputES {
		^FRPGUICode.makeENInputES(this)
	}
}

+ View {

	asENInput {
		^FRPGUICode.makeENInput(this)
	}

	asENInputES {
		^FRPGUICode.makeENInputES(this)
	}
}

/*+ SCView {

	asENInput {
		^FRPGUICode.makeENInput(this)
	}

}*/

+ MKtlElement {

	asENInput {
        var es = Var(this.value);
		var func = { |v| es.value_(v.value) };
		var addHandler = IO{ this.addAction(func); IO{ this.removeAction(func) } };
		^Writer( es, Tuple3([addHandler],[],[]) )
	}

	asENInputES {
		var es = EventSource();
		var func = { |v| es.fire(v.value) };
		var addHandler = IO{ this.addAction(func); IO{ this.removeAction(func) } };
		^Writer( es, Tuple3([addHandler],[],[]) )
	}

}