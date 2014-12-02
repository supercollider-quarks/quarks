

// in order to be able to ask a ProxySpace stream if it is playing or not
+ StreamControl {
	stream { "CHECK".postln; ^stream }
}

+ EventStreamPlayer {
	stream { "CHECK".postln; ^stream }
}


+ Function {

	record {arg time, bus, channels=1, ed=0;
		var foundWidget = false;
		var synth;
		
		XQ.globalWidgetList.do({|widget| 
			if(widget.isKindOf(XiiRecorder), {
				foundWidget = true;
				widget.win.front;
				widget.record(time, bus, ed);
			})
		});
		if(foundWidget == false, {
			XQ.globalWidgetList.add(XiiRecorder.new(Server.default, channels).record(time, bus, ed));
		});
		synth = this.play;
		if(time.isNil.not, {
			AppClock.sched(time, { synth.free });
		});
	}
	
}

+ SimpleNumber {
	
	// checking if a MIDI note is microtone
	midiIsMicroTone { arg tolerance = 0.01;
		if(this.frac < tolerance, {^false}, {^true});
	}
	// checking if a frequency is microtone
	freqIsMicroTone { arg tolerance = 0.01;
		if(this.cpsmidi.frac < tolerance, {^false}, {^true});
	}

	midinotename { arg sign;
		// appropriated from wouter's method, since it's not a quark...
		var out;
		if(sign.isNil) {sign = $n};
		if(sign.class == Symbol) {sign = sign.asString};
		if(sign.class == String) {sign = sign[0]};
		out = IdentityDictionary[
		$# -> ["C", "C#", "D", "D#", "E", "F", "F#", "G", "G#", "A", "A#", "B"],
		$b -> ["C", "Db", "D", "Eb", "E", "F", "Gb", "G", "Ab", "A", "Bb", "B"],
		$n -> ["C", "C#", "D", "Eb", "E", "F", "F#", "G", "G#", "A", "Bb", "B"]
		].at(sign)[this.round(1.0) % 12] ++ ((this.round(1.0) / 12).floor - 2).asInt;
		^out;
	}
	
	// - thor
/*
10.loop({arg i, break; i.postln; if(i == 3, {break.value(222)})})
*/

	loop {arg function;
		var i = 0;
		while ({ i < this }, { function.value(i, {|val| ^val }) ; i = i+1; });
	}
}

+ ArrayedCollection {

	// - thor
/*
[11,22,33,44,55,66,77].loop({arg item, i, break; item.postln; if(item == 33, {break.value(222)})})
*/

	loop {arg function;
		var i = 0; var item;
		while ({ i < this.size }, { function.value(this.at(i), i, {|val| ^val }) ; i = i+1; });
	}
}

+ Array {
	midinotename { arg sign;     
		^this.collect(_.midinotename(sign));
	}
}

+ Point {
	distanceFrom { |other|
		^sqrt(([this.x, this.y] - [other.x, other.y]).squared.sum);
	}
}

//+ SCEnvelopeView {
+ EnvelopeView {
	// an Env has times in sec for each point, an EnvView has points (x,y) in the view (0 to 1)
	// this method formats that
	
	env2viewFormat_ {arg env; // an envelope of the Env class passed in
		var times, levels, timesum, lastval; 
		times = [0.0]++env.times.normalizeSum; // add the first point (at 0)
		levels = env.levels;
		timesum = 0.0;
		lastval = 0.0;
		times = times.collect({arg item, i; lastval = item; timesum = timesum+lastval; timesum});
		[\times, times.asFloat, \levels, levels.asFloat].postln;
		this.value_([times.asFloat, levels.asFloat]);
	}
	
	view2envFormat {
		var times, levels, scale, lastval, timesum;
		times = this.value[0];
		levels = this.value[1];
		times = times.drop(1);
		timesum = 0.0;
		lastval = 0.0;
		times = times.collect({arg item, i; lastval = item; timesum = lastval-timesum; timesum});
		^[levels, times];
	}
}

/*
+ SCSlider {
					
	defaultKeyDownAction { arg char, modifiers, unicode, keycode;
		// standard keydown
		if (char == $r, { this.valueAction = 1.0.rand; ^this });
		if (char == $n, { this.valueAction = 0.0; ^this });
		if (char == $x, { this.valueAction = 1.0; ^this });
		if (char == $c, { this.valueAction = 0.5; ^this });
		if (char == $], { this.increment; ^this });
		if (char == $[, { this.decrement; ^this });
		// [modifiers, unicode, keycode].postln;
		if(modifiers&262144==262144, { // check if Ctrl is down first
			if (unicode == 16rF700, { this.incrementCtrl; ^this });
			if (unicode == 16rF703, { this.incrementCtrl; ^this });
			if (unicode == 16rF701, { this.decrementCtrl; ^this });
			if (unicode == 16rF702, { this.decrementCtrl; ^this });
		}, { // if not, then normal
			if (unicode == 16rF700, { this.increment; ^this });
			if (unicode == 16rF703, { this.increment; ^this });
			if (unicode == 16rF701, { this.decrement; ^this });
			if (unicode == 16rF702, { this.decrement; ^this });
		});
		^nil		// bubble if it's an invalid key
	}
	
	incrementCtrl { ^this.valueAction = this.value + 0.001 }
	decrementCtrl { ^this.valueAction = this.value - 0.001 }


}
*/

/*
+ Buffer {
	crop {arg startFrame, numFrames, overwrite=false, action;
		var cond, tempbuf, tempbufnum, newbuf;
		cond = Condition.new;
		
		Routine.run {
			tempbuf = Buffer.alloc(server, numFrames, numChannels);
			server.sync(cond);
			this.copyData(tempbuf, 0, startFrame, numFrames);
			server.sync(cond);

			newbuf = Buffer.alloc(server, tempbuf.numFrames, tempbuf.numChannels, bufnum:bufnum);
			server.sync(cond);
			tempbuf.copyData(newbuf);
			server.sync(cond);

			tempbuf.free;	
			tempbuf = nil;
			
			if(overwrite, { newbuf.write(this.path, "AIFF", "int16") });
			server.sync(cond);
			this.updateInfo(action); // update so the buffer object has the new buffer info
		};
	}
}
*/

+ Buffer {
	/*
		b = Buffer.read(s, "sounds/birta.aif");
		b.updateInfo
		b.play

		b.crop(10000, 66666, false)
		b.crop(10000, 6666, false)
		b.updateInfo
		b
		
		XQ.buffers("uno")[1].numFrames
		XQ.buffers("uno")[1].play
		
		XQ.buffers("uno")[1]
		XQ.buffers("uno")[1].crop(109000, 51000, true)

a
a = Buffer.read(s, "sounds/ixiquarks/aa.aif")
a.play
a.numFrames
a.crop(10000, 44000)
a.crop(1000, 2000)
a.updateInfo

a.path_("sounds/ixiquarks/aaa.aif")

*/
	
	crop {arg startFrame, numFrames, overwrite=false, action, newpath;
		var cond, tempbuf, tempbufnum, newbuf;
		cond = Condition.new;
		Routine.run {
			tempbuf = Buffer.alloc(server, numFrames, numChannels);
			server.sync(cond);
			this.copyData(tempbuf, 0, startFrame, numFrames);
			newbuf = Buffer.alloc(server, tempbuf.numFrames, tempbuf.numChannels, bufnum:bufnum);
			server.sync(cond);
			tempbuf.copyData(newbuf);
			tempbuf.free;	
			tempbuf = nil;
			if(overwrite, { 
				newbuf.write(this.path, "AIFF", XQ.pref.bitDepth) 
			}, {
				newpath = if(newpath.isNil, {newpath = this.path.splitext[0]++"_CR.aif"});
				newbuf.write(newpath, "AIFF", XQ.pref.bitDepth);
				this.path_(newpath);
			});
			this.updateInfo(action); // update so the buffer object has the new buffer info
		};
	}
}


/*
		s.boot
		b = Buffer.read(s, "sounds/a11wlk01.wav");
		b.plot
		b.numFrames
		b.play
		b
		c= b.crop(100000, 66666, {|buf| buf.numFrames.postln;})
		c.plot
		c.numFrames
		c.play
		c
		d= c.crop(10000, 6666, {|buf| buf.numFrames.postln;})
		d.plot
		d.numFrames
		d.play
		d

+ Buffer {
	crop {arg startFrame, numFrames, action;
		^Buffer.alloc(server, numFrames, numChannels, {|buf|
			buf.updateInfo({
				this.copyData(buf, 0, startFrame, numFrames);
				buf.updateInfo(this.free(action));
			});
		});
	}
}
*/

+ ArrayedCollection {
	
	ixiplot { arg name, bounds, discrete=false, numChannels = 1, minval, maxval, parent, labels=true, filled=true, color=XiiColors.lightgreen, step=0.001;	
	
		var plotter, txt, chanArray, unlaced, val, window, thumbsize, zoom, width, 
			layout, write=false, msresize, gui;
			
		gui = GUI.current;
		
		
		bounds = bounds ?? { parent.notNil.if({
				if(parent.respondsTo(\view)){
					parent.view.bounds
				}{
					parent.bounds
				}
			}, {
				Rect(200 ,140, 705, 410);
 			});
 		};
			
		width = bounds.width-8;
		
		name = name ? "plot";
		
		unlaced = this.unlace(numChannels);
		minval = if(minval.isArray, {
			minval.collect({|oneminval, index| oneminval ?? { unlaced[index].minItem } })
		}, {
			{minval ?? { this.minItem }}.dup(numChannels);
		});
		maxval = if(maxval.isArray, {
			maxval.collect({|onemaxval, index| onemaxval ?? { unlaced[index].maxItem } })
		}, {
			{maxval ?? { this.maxItem }}.dup(numChannels);
		});
		
		chanArray = Array.newClear(numChannels);
		if( discrete, {
			zoom = 1;
			thumbsize = max(1.0, width / (this.size / numChannels));
			unlaced.do({ |chan, j|
				chanArray[j] = chan.linlin( minval[j], maxval[j], 0.0, 1.0 );
			});
		}, {
			zoom = (width / (this.size / numChannels));
			thumbsize = 1;
			unlaced.do({ |chan, j|
				val = Array.newClear(width);
				width.do { arg i;
					var x;
					x = chan.blendAt(i / zoom);
					val[i] = x.linlin(minval[j], maxval[j], 0.0, 1.0);
				};
				chanArray[j] = val;
			});
		});
		window = parent ?? { gui.window.new( name, bounds )};

		layout = gui.vLayoutView.new( window, parent.notNil.if({
			Rect(bounds.left+4, bounds.top+4, bounds.width-10, bounds.height-10);
		}, {
			Rect(4, 4, bounds.width - 10, bounds.height - 10); 
		})).resize_(5);
		
		if(labels){
			txt = gui.staticText.new(layout, Rect( 8, 0, width, 18))
					.string_(" values: " ++ this.asString);
		};

		numChannels.do({ |i|
			plotter = gui.multiSliderView.new(layout, Rect(0, 0, 
					layout.bounds.width, layout.bounds.height - if(labels, {26}, {0}))) // compensate for the text
				.readOnly_(false)
				.drawLines_(discrete.not)
				.drawRects_(discrete)
				.isFilled_(filled)
				.indexThumbSize_(thumbsize) 
				.valueThumbSize_(1)
				.step_(step)
				.background_(Color.white)
				.colors_(Color.black, color)
				.action_({|v| 
					var curval;
					curval = v.currentvalue.linlin(0.0, 1.0, minval[i], maxval[i]);
					
					if(labels){
						txt.string_("index: " ++ (v.index / zoom).roundUp(0.01).asString ++ 
						", values: " ++ this);
					};
					if(write) { this[(v.index / zoom).asInteger * numChannels + i ]  = curval };
				})
				.keyDownAction_({ |v, char|
					if(char === $l) { write = write.not; v.readOnly = write.not;  };
				})
				.value_(chanArray[i])
				.elasticMode_(1);
			(numChannels > 1).if({ // check if there is more then 1 channel
				plotter.resize_(5);
			});
		});
		
		^window.tryPerform(\front) ?? { window }
		
	}
}

/*
(
// e = Env.new([0, 1, 0.3, 0.8, 0], [1, 3, 1, 4],'linear').plot;
 e = Env.new([0.5, 1, 0.6, 0.6, 0], [0.1, 0.3, 0.81, 0.2],'linear').plot;

//e = Env.triangle(1, 1);
//e = Env.adsr(0.02, 0.2, 0.25, 1, 1, -4);

a = SCWindow("envelope", Rect(200 , 450, 250, 100));
a.view.decorator =  FlowLayout(a.view.bounds);

b = SCEnvelopeView(a, Rect(0, 0, 230, 80))
	.drawLines_(true)
	.selectionColor_(Color.red)
	.drawRects_(true)
	.resize_(5)
	.action_({arg b; [b.index,b.value].postln})
	.thumbSize_(5)
	.env2viewFormat_(e);
a.front;


)
b.view2envFormat

*/
