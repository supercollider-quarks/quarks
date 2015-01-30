/*
Colliding, a simple SuperCollider environment for learning and live coding
(c) Gerard Roma, 2013-2014

This program is free software; you can redistribute it and/or
modify it under the terms of the GNU General Public License
as published by the Free Software Foundation; either version 2
of the License, or (at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program; if not, write to the Free Software
Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
*/

Qwerty{
	var <win, keys;
	var notes, lowest, v, h;
	var buttons,synths,tasks;

	*new{|synthdef="no synthdef"|
		^super.new.init(synthdef);
	}
	getPos{|ch|
		keys.do{|row,i|
			var idx = row.find(ch);
			if(idx.notNil){^[i,idx]};
		}
	}
	init{|synthdef|
		h = 2;
		v = 7;
		lowest = 36;
		GUI.qt;
		win = Window(synthdef, Rect(128, 320, 300, 120));
		win.layout = VLayout.new;
		keys = ["1234567890", "QWERTYUIOP", "ASDFGHJKL�", "ZXCVBNM,.-"];
		notes = Array.fill(4,{|i| Array.series(10,lowest+(i*v),h)}).reverse;
		buttons = Dictionary.new;
		synths = Dictionary.new;
		tasks = Dictionary.new;
		win.view.keyDownAction_{|view,char|
			var ch = char.toUpper.asString;
			var pos = this.getPos(ch);
			var key = notes[pos[0]][pos[1]];

			if(pos.notNil){
				buttons[ch].value=1;
				if(synths[ch].isNil){
					synths[ch] = Synth(synthdef,[\key,key,\freq,key.midicps,\gate,1]);
				}
			}
		};
		win.view.keyUpAction_{|view,char|
			var ch = char.toUpper.asString;
			if(buttons[ch].notNil){buttons[ch].value=0};
			if(tasks[ch].notNil){tasks[ch].stop};
			tasks[ch]=Task({
				0.1.wait;
				if(synths[ch].notNil){
					synths[ch].release;
					synths[ch] = nil;
				};
				nil;
			});
			tasks[ch].play;
		};

		keys.do {|row, i|
			var layout = HLayout.new;
			win.layout.add(layout);
			row.do {|key,j|
				var v = Button(win, Rect(0,0,5,5));
				v.states = [
					[key.asString,Color.black, Color.white],
					[key.asString,Color.white,Color.black ]
				];
				layout.add(v);
				buttons[key.asString] = v;
			};

		};
		win.front;
	}
}
