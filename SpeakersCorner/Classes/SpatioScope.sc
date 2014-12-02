SpatioScope { 
	
	var <locations, <server,  <bounds, <>background, <>foreground; 
	var <numChannels, <offset = 0; 
	var <proxy, <resp, <skipjack;
	var <parent, <startBtn, <stopBtn, <ampViews, <magSlider, <>redLevel=0.95, <>magnify=1;

	var <rate;

	*new { arg locations, server, parent, bounds;
		locations = locations ?? { [(-0.5 @ -0.5), (0.5 @ -0.5), (0.5@0.5), (-0.5@0.5) ] }; 
		server = server ? Server.default; 
		
		^super.newCopyArgs(locations, server)
			.init(bounds)
			.gui(parent)
			// .start
	}
	
	init { |argBounds|
		bounds = argBounds ?? { Rect(0,0,410,410) };
		numChannels = locations.size; 
		
		proxy = proxy ?? {  NodeProxy.control(server, this.numChannels) };
		proxy.source = {
			Amplitude.kr(InFeedback.ar(this.offset, this.numChannels), 0, 0.5)
		};

		rate = \audio;

		resp.remove;
		resp = OSCresponderNode(server.addr, '/c_set', { arg time, r, msg; 
			var amps;
			// check if this reply message is for this spatioscope
			if ( msg[1] == this.proxy.index ){
				amps = msg.copyToEnd(1).clump(2).flop[1];
				{  this.amps_(amps * (magnify ? 1)); }.defer;
			};
		}); 
		
		skipjack = SkipJack( 
			{ proxy.wakeUp; this.updateViews; }, 
			0.2, 
			{ parent.isClosed; },
			this.class.name, 
			autostart: false
		);
	}

	maxBusNum {
		^if ( rate == \audio ){
			server.options.numAudioBusChannels
		} {
			server.options.numControlBusChannels
		} - this.numChannels;
	}

	offset_ { |inChan=0|
		if (inChan.inclusivelyBetween(0, this.maxBusNum ) ) { 
			offset = inChan; 
			if (skipjack.task.isPlaying) { this.stop.start };
		}{
			"new offset out of range of valid busnumbers!".warn;
		};
	}
	
	gui { |argParent| 
		var butWidth = 38; 
		background = background ?? { Color(0, 0, 0.15) }; // dark blue
		foreground = foreground ?? { Color(0.5, 0.5, 1.0) }; // light blue
		
		parent = argParent ?? { 
			Window(this.class.name, bounds.moveBy(200, 200).resizeBy(10, 30)).front;
		};
		parent.view.background_(background); 
		parent.addFlowLayout;
		
		#startBtn, stopBtn = [ \start, \stop ].collect { |name, i| 
			Button(parent, Rect(i * (butWidth + 2) + 2, 2, butWidth, 20))
				.states_([[name, Color.white, Color.clear], 
					[name, Color.white, Color(0,0,0.8)]])
				.action_({ this.perform(name); });
		};
	
		magSlider = EZSlider(parent, 
			(bounds.width - (butWidth * 2) - 10) @ 20, 
			\magnify, 
			[1, 10, \exp], 
			{ |sl| magnify = sl.value }, magnify, 
			labelWidth: 45, numberWidth: 30);
		magSlider.labelView.stringColor_(foreground);
		magSlider.numberView.background_(foreground);
//		magSlider.numberView.resize_(3);
//		magSlider.sliderView.resize_(2);
		
		this.showLocs;
		this.stop.start;
	}
	
	showLocs { 
		var ampCont = CompositeView(parent, bounds).background_(Color.clear);
		var center = bounds.center; 
		var size = bounds.center.x * 0.2; 
		
		ampViews = locations.collect { |point, i| 
			var left = point.x + 1 - 0.125 * center.x; 
			var top = point.y + 1 - 0.125 * center.y; 
			StaticText(ampCont, Rect(left, top, size, size))
			.string_((i + 1).asString).align_(\center)
				.stringColor_(foreground)
			.background_(Color.black);
		};
	}

	*ringPos { |num=6, radius=0.7, angleOffset=0| 
		var angles = { |i| (2pi * (i / num)) + angleOffset }.dup(num); 
		^angles.collect { |angle| Polar(radius, angle).asPoint };
	}

	*ring { |num=6, radius=0.7, angleOffset=0, server, parent, bounds|
		var locs = this.ringPos(num, radius, angleOffset);
		^this.new(locs, server, parent, bounds);
	}
	
	
	*rings { |nums, radii, angleOffsets = 0, server, parent, bounds|
		var locs; 
		var numrings = nums.size; 
			// earlier circles are outer 
			// (assumes dome shape, low channels on bottom, as in IEM CUBE)
		radii = radii ?? { (numrings .. 1) / (numrings + 1) };
		angleOffsets = angleOffsets ?? 0;
		locs = [ nums, radii, angleOffsets ].flop.collect { |list| this.ringPos(*list) }.flat; 
		
		^this.new(locs, server, parent, bounds);
	}

	// grid speaker positions
	*gridPos{ |numx = 2, numy = 6|
		
		^Array.series( numy, 1/(2*numy), 1/numy ).collect{ |y|
			Array.series( numx, 1/(2*numx), 1/numx ).collect{ |x|
				Point(x*2-1,y*2-1);
			}
		}.flatten;
	}

	*grid { |numx=2, numy=6, server, parent, bounds|
		var locs = this.gridPos(numx, numy);
		^this.new(locs, server, parent, bounds);
	}


	// listen to control buses
	krListen{
		rate = \control;
		proxy.source = {
			Amplitude.kr(In.kr(this.offset, this.numChannels), 0, 0.5)
		};
	}

	// listen to audio rate buses
	arListen{
		rate = \audio;
		proxy.source = {
			Amplitude.kr(InFeedback.ar(this.offset, this.numChannels), 0, 0.5)
		};
	}
			
	start {
		if(server.serverRunning.not) { 
			"SpatioScope: server not running.".warn; 
			skipjack.stop; 
			this.updateViews;
			^this
		};
		proxy.rebuild;
		fork { 0.4.wait; proxy.send };
		skipjack.start; 
		resp.remove.add;
		this.updateViews;
	}

	updateViews {
		var isOn = skipjack.task.isPlaying.binaryValue;
		if (parent.isClosed.not) { 
			startBtn.value_(isOn);
			stopBtn.value_(1 - isOn);
			server.listSendMsg(["/c_get"] ++ ((_ + proxy.index) ! proxy.numChannels));
		};
	}
	
	stop { 
		skipjack.stop;
		proxy.free;
		resp.remove;
		this.updateViews;
		this.amps_([]);
	}
	
	amps_ { arg vals;
		var amp, col;
		// "amps coming in: %\n".postf(vals);
		if (parent.isClosed.not) { 
			ampViews.do { |el, i|  
				amp = (vals[i] ? 0).sqrt; 
				col = if (amp > redLevel, { Color.red }, { Color.yellow( amp ) });
				el.background_(col) 
			} 
		};
	}
}