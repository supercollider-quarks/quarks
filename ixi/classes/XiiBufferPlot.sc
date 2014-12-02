
/*

y = Buffer.read(s, "sounds/birta.aif"); // 2 channels

b.numChannels
b = Buffer.read(s,"sounds/a11wlk01.wav"); // 1 channel

b = Buffer.alloc(s, 44100 * 2.0, 2); // a four second 1 channel Buffer

a = XiiBufferPlot.new(b)

// overdub
(
SynthDef("test",{ arg out=0,bufnum=0;
	RecordBuf.ar(SinOsc.ar(2990*SinOsc.ar(MouseX.kr(1, 20)), 0, MouseY.kr(0, 1)!2), bufnum, 0, 1, 0);
//	RecordBuf.ar(LoopBuf.ar(2, y.bufnum, 1, 1, 0, 0, y.numFrames), bufnum, 0, 1, 0);
//	RecordBuf.ar(AudioIn.ar([1,2]), bufnum, 0, 1, 0); // mixes equally with existing data
}).play(s,[\out, 0, \bufnum, b.bufnum]);
)

a.redraw


x = SCWindow.new("aa", Rect(100,100, 600, 200)).front;
b = Buffer.read(s,"sounds/a11wlk01.wav"); // 1 channel

a = XiiBufferPlot.new(b, x, Rect( 100,10, 490, 170))

t = Task({20.do({a.redraw; 1.wait})}).play
t.stop
a.remove;
x.refresh;
a = XiiBufferPlot.new(b, x, Rect( 100,10, 490, 170))

x = SCWindow.new("aa", Rect(100,100, 815, 510)).front;

a = XiiBufferPlot.new(b, x, Rect( 100,10, 705, 170))
c = XiiBufferPlot.new(b, x, Rect( 100, 190, 705, 170))

{SinOsc.ar(200)}.play

*/


XiiBufferPlot {

	var plotter, txt, chanArray, unlaced, val, minval, maxval, window, thumbsize, zoom, width, 
		layout, write=false, chanPlotter, bounds;
	var bufferFloatArray, theBuffer;
	
	*new { arg buffer, window, bounds, discrete=false, numChannels = 2;
		^super.new.initXiiBufferPlot(buffer, window, bounds, discrete, numChannels);
	}
		
	initXiiBufferPlot { arg buffer, window, bounds, discrete, numChannels;
		theBuffer = buffer;
		bufferFloatArray = theBuffer.loadToFloatArray(
				action: { |array, buf| {
					this.initGUI(array, window, bounds, buf.numChannels, discrete) }.defer;
				});
	}
	
	redraw {
		bufferFloatArray = theBuffer.loadToFloatArray(
				action: { |array, buf| {
					this.replot(array, theBuffer.numChannels) }.defer;
				});
	}
	
	replot {arg array, numChannels;
		unlaced = array.unlace(numChannels);
		chanArray = Array.newClear(numChannels);
		unlaced.do({ |chan, j|
			val = Array.newClear(width);
			width.do({ arg i;
				var x;
				x = chan.blendAt(i / zoom);
				val[i] = x.linlin(-1.0, 1.0, 0.0, 1.0);
			});
			chanArray[j] = val;
		});
		numChannels.do({ |i|
			chanPlotter[i].value_(chanArray[i]);
		});
	}
	
	setIndex_{arg index;
		chanPlotter.do({arg plotter; {plotter.index_(index)}.defer;});
	}
	
	initGUI {arg array, argwindow, argbounds, numChannels, discrete;
	
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
				val[i] = x.linlin(-1.0, 1.0, 0.0, 1.0);
			};
			chanArray[j] = val;
		});
		
		window = argwindow ? GUI.window.new("ixi buffer plot", Rect(bounds.left, bounds.height, 			bounds.width+20, bounds.height+20), resizable: false);
		numChannels.do({ |i|
			chanPlotter.add(
				GUI.multiSliderView.new(window, 
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
					curval = v.currentvalue; // TEST!
				})
				.keyDownAction_({ |v, char|
					if(char === $l) { write = write.not; v.readOnly = write.not;  };
				})
				.value_(chanArray[i])
				.resize_(5)
				.elasticMode_(1)
				.showIndex_(true);

			);
				
		});
		^window.front;
	}
	
	remove {
		chanPlotter.do({arg view; view.remove });
	}
}