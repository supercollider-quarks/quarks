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

+ View {

	mouseClicksENInputES {
		var es = EventSource();
		var f = {
			es.fire( Unit );
		};
		var addHandler = IO{
			this.mouseUpAction = this.mouseUpAction.addFunc(f);
			IO{ this.mouseUpAction.removeFunc(f) }
		};
		^Writer( es, Tuple3([addHandler],[],[]) )
	}

	mouseClicksEnInES {
		^ENDef.appendToResult( this.mouseClicksENInputES );
	}

	mousePosENInput {
		var sig = Var(Point(0,0));
		var f = { |view, x, y, modifiers|
			sig.value_( Point(x, y) );
		};
		var addHandler = IO{
			this.mouseOverAction = this.mouseOverAction.addFunc(f);
			IO{ this.mouseOverAction.removeFunc(f) }
		};
		^Writer( sig, Tuple3([addHandler],[],[]) )
	}

	mousePosEnIn {
		^ENDef.appendToResult( this.mousePosENInput );
	}

	mouseIsDownENInput {
		var sig = Var( false);
		var fdown = { |view, x, y, modifiers|
			sig.value_( true );
		};
		var fup = { |view, x, y, modifiers|
			sig.value_( false );
		};
		var addHandler = IO{
			this.mouseDownAction = this.mouseDownAction.addFunc(fdown);
			this.mouseUpAction = this.mouseUpAction.addFunc(fup);
			IO{
				this.mouseDownAction.removeFunc(fdown);
				this.mouseUpAction.removeFunc(fup)
			}
		};
		^Writer( sig, Tuple3([addHandler],[],[]) )
	}

	mouseIsDownEnIn {
		^ENDef.appendToResult( this.mouseIsDownENInput );
	}

	keyDownENInputES {
		var es = EventSource();
		var f = { |v, char|
			es.fire( char );
		};
		var addHandler = IO{
			this.keyDownAction = this.keyDownAction.addFunc(f);
			IO{ this.keyDownAction.removeFunc(f) }
		};
		^Writer( es, Tuple3([addHandler],[],[]) )
	}

	keyDownEnInES {
		^ENDef.appendToResult( this.keyDownENInputES );
	}

	keysDownENInput {
		var sig = Var( []);
		var fdown = { |view, char|
			sig.value_( sig.value.add(char) );
		};
		var fup = { |view, char|
			var x = sig.value;
			x.remove(char);
			sig.value_( x );
		};
		var addHandler = IO{
			this.keyDownAction = this.keyDownAction.addFunc(fdown);
			this.keyUpAction = this.keyUpAction.addFunc(fup);
			IO{
				this.keyDownAction.removeFunc(fdown);
				this.keyUpAction.removeFunc(fup)
			}
		};
		^Writer( sig, Tuple3([addHandler],[],[]) )
	}

	keysDownEnIn {
		^ENDef.appendToResult( this.keysDownENInput );
	}

	//TODO, shiftDown, ctrlDown, etc

}