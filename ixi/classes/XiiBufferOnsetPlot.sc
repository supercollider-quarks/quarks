
/*

b = Buffer.read(s, "sounds/birta.aif"); // 2 channels
b
b.numChannels
b = Buffer.read(s,"sounds/a11wlk01.wav"); // 1 channel

b = Buffer.alloc(s, 44100 * 2.0, 2); // a four second 1 channel Buffer


b = Buffer.read(s,"sounds/break"); // 1 channel
a = XiiBufferOnsetPlot.new(b);
a.redraw(0.01);
a.drawOnsets;
fork{1.wait; b = a.getOnsetTimesList};
Post << b

// overdub
(
SynthDef("test",{ arg out=0,bufnum=0;
	RecordBuf.ar(SinOsc.ar(2990*SinOsc.ar(MouseX.kr(1, 20)), 0, MouseY.kr(0, 1)!2), bufnum, 0, 1, 0);
//	RecordBuf.ar(LoopBuf.ar(2, y.bufnum, 1, 1, 0, 0, y.numFrames), bufnum, 0, 1, 0);
//	RecordBuf.ar(AudioIn.ar([1,2]), bufnum, 0, 1, 0); // mixes equally with existing data
}).play(s,[\out, 0, \bufnum, b.bufnum]);
)

a.redraw(0.1)


x = SCWindow.new("aa", Rect(100,100, 600, 200)).front;

a = XiiBufferOnsetPlot.new(b, x, Rect( 100,10, 490, 170))

t = Task({20.do({a.redraw; 1.wait})}).play

x = SCWindow.new("aa", Rect(100,100, 815, 510)).front;

a = XiiBufferOnsetPlot.new(b, x, Rect( 100,10, 705, 170))
c = XiiBufferOnsetPlot.new(b, x, Rect( 100, 190, 705, 170))

{SinOsc.ar(200)}.play

*/


XiiBufferOnsetPlot {

	var plotter, txt, chanArray, unlaced, val, minval, maxval, win, thumbsize, zoom, width, 
		layout, write=false, chanPlotter, tabletViews, bounds;
	var bufferFloatArray, theBuffer;
	var onsetsList, onsetTimesList, thresh, mingap;
	var chosennode;
	
	*new { arg buffer, win, bounds, discrete=false, numChannels = 2;
		^super.new.initXiiBufferOnsetPlot(buffer, win, bounds, discrete, numChannels);
	}
		
	initXiiBufferOnsetPlot { arg buffer, win, bounds, discrete, numChannels;
		theBuffer = buffer;
		bufferFloatArray = theBuffer.loadToFloatArray(
				action: { |array, buf| {
					this.initGUI(array, win, bounds, buf.numChannels, discrete) }.defer;
				});
	}
	
	initGUI {arg array, argwin, argbounds, numChannels, discrete;
		
		onsetsList = List.new;
		onsetTimesList = List.new;
		thresh = 0.3;
		
		bounds = argbounds ?  Rect(10, 5, 715, 300);
		chanPlotter = List.new;
		width = bounds.width-8;
		zoom = (width / (array.size / numChannels));
		
		if(discrete) {
			thumbsize = max(1.0, zoom);
		}{
			thumbsize = 1;
		};
		
		unlaced = array.unlace(numChannels);
		chanArray = Array.newClear(numChannels);
		unlaced.do({ |chan, j|
			val = Array.newClear(width);
			width.do { arg i;
				var x;
				x = chan.blendAt(i / zoom);
				val[i] = x.linlin(0.0, 1.0, 0.0, 1.0);
				if(val[i] > thresh, {
					onsetsList.add( Point(bounds.left+i, val[i]) );
					onsetTimesList.add( (i/zoom)/44100  ); // frames
				});
			};
			chanArray[j] = val;
		});
		
		win = argwin ? GUI.window.new("ixi buffer plot", Rect(bounds.left, bounds.height, 			bounds.width+20, bounds.height+20), resizable: false);
		numChannels.do({ |i|
			chanPlotter.add(
				GUI.multiSliderView.new(win, 
					Rect(bounds.left, bounds.top + ((bounds.height/numChannels)*i),
											 bounds.width, bounds.height/numChannels))
				.readOnly_(true)
				.drawLines_(discrete.not)
				.drawRects_(discrete)
				.canFocus_(false)
				.thumbSize_(thumbsize) 
				.valueThumbSize_(1)
				.background_(XiiColors.lightgreen)
				.colors_(XiiColors.darkgreen, Color.blue(1.0,1.0))
				.fillColor_(Color.white)
				.action_({|v| 
					var curval;
					curval = v.currentvalue;
				})
				.keyDownAction_({ |v, char|
					if(char === $l) { write = write.not; v.readOnly = write.not;  };
				})
				.value_(chanArray[i])
				.resize_(5)
				.elasticMode_(1)
				.showIndex_(true);
			);
			/*
			// not used yet - will be used when nodes can be selected
			tabletViews.add(
				SCTabletView(win, Rect(bounds.left, bounds.top + ((bounds.height/numChannels)*i),
											 bounds.width, bounds.height/numChannels))
					.canFocus_(false)
					.mouseDownAction_({arg view,x,y,pressure;
						chosennode = this.findOnsetNode(x, y);
						if(chosennode.isNil, {"no node".postln;}, {"found node".postln;});
					})
			);
			*/
		});
		^win.front;
	}

	redraw {arg thresh=0.3, mingap=5;
		bufferFloatArray = theBuffer.loadToFloatArray(
				action: { |array, buf| {
					this.replot(array, theBuffer.numChannels, thresh, mingap) }.defer;
				});
	}
	
	replot {arg array, numChannels, argthresh, argmingap;
		onsetsList = List[Point(bounds.left, 0)];
		onsetTimesList = List.new;
		thresh = argthresh;
		mingap = argmingap;
		unlaced = array.unlace(numChannels);
		chanArray = Array.newClear(numChannels);
		unlaced.do({ |chan, j|
			val = Array.newClear(width);
			width.do({ arg i;
				var x;
				x = chan.blendAt(i / zoom);
				val[i] = x.linlin(0.0, 1.0, 0.0, 1.0);
				if((val[i] > thresh) && (((i+bounds.left)-onsetsList.last.x).abs > mingap), {
					onsetsList.add(Point((3+bounds.left)+(i*1.002), (bounds.height+3)-(val[i]*bounds.height)));
					onsetTimesList.add( (i/zoom)/44100  );
				});
			});
			chanArray[j] = val;
		});
		numChannels.do({ |i|
			chanPlotter[i].value_(chanArray[i]);
		});
	}
	
	drawOnsets {
		win.drawHook_({
			Pen.width = 1;
			onsetsList.do({arg point, i;
				GUI.pen.color = Color.red.alpha_(0.2);
				GUI.pen.fillOval( Rect(point.x-3.5, point.y-3.5, 8, 8));
				GUI.pen.color = Color.black.alpha_(0.6);
				GUI.pen.strokeOval( Rect(point.x-3.5, point.y-3.5, 8, 8));
				GUI.pen.color = Color.black.alpha_(0.5);
				GUI.pen.line(Point((point.x).round(1)+0.5, bounds.height+4), Point((point.x).round(1)+0.5, point.y+5.0));
				GUI.pen.stroke;
			});
		});
		win.refresh;
	}
	
	drawFFTMushrooms {arg fftOnsets;
		win.drawHook_({
			Pen.width = 1;
			fftOnsets.do({arg point, i; 
				GUI.pen.color = Color.red.alpha_(0.2);
				GUI.pen.addAnnularWedge(point, 3, 9, pi, pi);
				GUI.pen.fill;
				GUI.pen.color = Color.black.alpha_(0.6);
				GUI.pen.addAnnularWedge(point, 3, 9, pi, pi);
				GUI.pen.stroke;
				GUI.pen.color = Color.black.alpha_(0.5);
				GUI.pen.line(Point((point.x).round(1)+0.5, bounds.height+4), Point((point.x).round(1)+0.5, point.y));
				GUI.pen.stroke;
			});
		});
		win.refresh;
	}
	
	setIndex_{arg index;
		chanPlotter.do({arg plotter; {plotter.index_(index)}.defer;});
	}
	
	remove {
		chanPlotter.do({arg view; view.remove;});
	}
	
	getOnsetTimesList {
		^onsetTimesList;
	}
	
	getOnsetsList {
		^onsetsList;
	}
	
	findOnsetNode {arg x, y;
		onsetsList.do({arg node; 
			if(Rect(node.x-3.5-bounds.left, 
					node.y-3.5-bounds.top, 8, 8).containsPoint(Point.new(x,y)), {
				^node;
			});
		});
		^nil;
	}

}