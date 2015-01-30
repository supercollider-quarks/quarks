
/*

given a list of names, 
it makes a gui
click learn and set each one,
post or save

its reloadable and reditable

usage:

ccresponder = ccbank.xfader
ccresponder.function = {  arg src, chan, num, val;  };

or

ccresponder = ccbank.responder('xfader',{ arg val;   });


CCBank([
	\xfader,
	\level,
	\one,
	\two,
	\three,
	\four,
	\pressure1,
	\pressure2,
	\pressure3
	\pressure4,
	\fx3,
	\fx4,
	\fxPressure3,
	\fxPressure4
]).gui

*/



CCBank {

	var <>sets,<>src,responders,oneShot,prev;
	
	*new { arg sets,src;
		^super.newCopyArgs(sets ? [],src).init
	}
	storeArgs {
		^[sets]
	}
	init {
		sets = sets.collect({ arg na; 
								if(na.isKindOf(Association).not,{ na -> nil },{ na 	})
							});
		responders = IdentityDictionary.new;
	}
	*responderClass { ^CCResponder }	
	guiBody { arg layout;
		SaveConsole(this,nil,layout).print.save;
		sets.do { arg assc,i;
			var n,c;
			layout.startRow;
			this.guiOne(layout,assc,i);
		};
	}
	guiOne { arg layout,assc,minWidth=100;
		var tb,ne;
		tb = ToggleButton(layout,"Learn",{ arg tb,state;
				oneShot.remove;
				oneShot = nil;
				if(prev.notNil,{
					prev.toggle(false,false)
				});
				oneShot = this.class.responderClass.new({|port,chan,num,value|
						{ ne.activeValue = num; 	}.defer
					},src,nil,nil,nil,true,true);

				layout.removeOnClose(oneShot);
				prev = tb;
			},{
				this.class.responderClass.remove(oneShot);
				oneShot = nil;
				prev = nil;
			});
		ne = NumberEditor(assc.value ? 128,ControlSpec(1,128,\lin,1));
		ne.action = { arg cnum;
			var ccr;
			assc.value = if(cnum == 128,{nil},{cnum.asInteger});
			// set any currently active one
			ccr = responders[assc.key];
			// TODO different for note
			if(ccr.notNil,{
				this.setCtlnum(ccr,cnum)
			});
		};
		ne.smallGui(layout);
		CXLabel(layout,assc.key.asString,minWidth:minWidth);
		//ve = CXLabel(layout,"",minWidth:50);
	}
	add { arg key,ccnum;
		sets = sets.add( key -> ccnum )
	}
	at { arg key;
		var ccr,assc;
		^responders[key] ?? {
			assc = this.findSet(key) ?? { (key.asString + "not found in" + this).warn };
			// would be better: if nil then do not install but allow it to learn and get installed later
			ccr = this.class.responderClass.new(nil, src, nil, assc.value ? 127, nil);
			responders[key] = ccr;
			ccr
		}
	}
	isMapped { arg key; ^this.findSet(key).value.notNil }
	setCtlnum { arg ccr,ctlnum;
		var me;
		ctlnum = ctlnum.asInteger;
		// there's a bug in matchEvent for changing the ctlnum
		me = MIDIEvent(nil,ccr.matchEvent.port,ccr.matchEvent.chan,ctlnum,nil);
		ccr.remove;
		ccr.matchEvent = me;
		// in case the bug is fixed
		if((CCResponder.ccnumr[ctlnum] ? []).includes(ccr).not,{
			this.class.responderClass.add(ccr);
		})
	}
	responder { arg key, function;
		var ccr;
		ccr = this.at(key);
		ccr.function = { |src,chan,num,value| function.value(value) };
		^ccr
	}
	doesNotUnderstand { |key ... args|
		var	result,set;
		set = this.findSet(key);
		if(set.isNil,{
			(key.asString + "not found in " + this).error;
			DoesNotUnderstandError(this, key, args).throw;
		},{
			^this.at(key)
		})
	}
		
	free {
		responders.keysValuesDo { arg k,ccr;
			ccr.remove
		};
		responders = IdentityDictionary.new;
	}
	keys {
		^sets.collect(_.key)
	}
	findSet { arg key;
		^sets.detect({ arg set; set.key == key })
	}
}


NoteOnBank : CCBank {
	
	*responderClass { ^NoteOnResponder }
	
}
