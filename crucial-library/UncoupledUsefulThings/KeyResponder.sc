

KeyResponder : KeyCodeResponder {
	/*
		this is a a KeycodeResponder
		which uses a KeycodeResponder for special keys like arrows, del, backspace
		thus guaranteeing that it will use the correct keycodes for the GUI you are using
	*/
	classvar keycodeMap;
	var <>keycodes;

	*initKeycodeMap {
		var qt,cocoa;
		keycodeMap = IdentityDictionary.new;
		keycodeMap['qt'] = Dictionary.new;
		keycodeMap['cocoa'] = Dictionary.new;
		[
			['del',16777223, 117],
			['backspace', 16777219, 51],
			['up', 16777235, 126],
			['right', 16777236, 124],
			['down', 16777237, 125],
			['left', 16777234, 123],
			['home', 16777232, 115],
			['end', 16777233, 119],
			['pageUp', 16777238, 116],
			['pageDown', 16777239, 121],

 			['f1',16777264 , 122 ],
 			['f2',16777265 , 120 ],
 			['f3',16777266 , 99 ],
 			['f4',16777267 , 118 ],
 			['f5',16777268 , 96 ],
 			['f6',16777269 , 97 ],
 			['f7',16777270 , 98 ],
 			['f8',16777271 , 100 ],
 			['f9',16777272 , 101 ],
 			['f10',16777273 , 109 ],
 			['f11',16777274 , 103 ],
 			['f12',16777275 , 111 ],
 			['f13',16777276 , 105 ],
 			['f14',16777277 , 107 ],
 			['f15',16777278 , 113 ],
 			['f16',16777279 , 106 ],
			// Qt 42064 not assigned
			['f17', nil,64 ],
			['f18', nil,79 ],
			['f19', nil,80 ],
			['clear', 16777227  , 71],
			['esc',  16777216 , 53],
			// these will have to be more thought out
			// because modifiers change and keycodes don't
			['enter', 16777221, 76] // numerical pad
		].do { arg ll;
			keycodeMap['qt'][ll[0]] = ll[2];
			keycodeMap['cocoa'][ll[0]] = ll[2];
		};
	}
	register { arg key,shift,caps,opt,cntl,function,description;
		/*
			key may be: 
				a symbol referring to one of the special keys above
				a keycode
		*/
		if(key.class === Symbol,{
			keycodes.register(keycodeMap[GUI.id][key] ?? {Error("Invalid keyName" + key).throw},
				shift,caps,opt,cntl,function,description);
		},{
			super.register(key,shift,caps,opt,cntl,function,description)
		})
	}

	value { arg view,char,modifier,unicode,keycode;
		var r;
		r = keycodes.at(keycode);
		if(r.notNil,{
			r.value(view,keycode,modifier);
			^true
		},{
			^super.value(view,char,modifier,unicode,keycode)
		});
	}

	registerKeycode { arg keycode,shift,caps,opt,cntl,function,description;
		keycodes.register(keycode,shift,caps,opt,cntl,function,description)
	}
	++ { arg that;
		var new,keys;
		if(that.isNil,{ ^this });
		if(that.class !== this.class,{
		    ^KeyDownResponderGroup(this,that)
		});

		// that overides this
		new = this.class.new;
		new.dict = dict.copy;
		that.dict.keysValuesDo({ arg unicode,kdrstack;
			new.put(unicode, kdrstack ++ this.at(unicode))
		});
		new.keycodes = this.keycodes ++ that.keycodes;
		^new
	}
	clear {
		super.clear;
		keycodes = KeyCodeResponder.new;
		if(keycodeMap.isNil,{
			KeyResponder.initKeycodeMap;
		})
	}	
}

/*
	on cocoa if you are holding function then arrows have different keycodes:
	ur: [ a SCUserView, ,, 8388864, 63276, 116 ]
	ur: [ a SCUserView, +, 8388864, 63275, 119 ]
	ur: [ a SCUserView, -, 8388864, 63277, 121 ]
	ur: [ a SCUserView, ), 8388864, 63273, 115 ]

*/
