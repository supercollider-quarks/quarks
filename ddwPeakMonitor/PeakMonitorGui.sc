
PeakMonitorGui : ObjectGui {
	var	leftFlow, rightFlow;
	var	multiSl, clipButtons, maxRecentView;
	var	maxRecent, recentSize, maxClip;
		// workaround for crucial gui issue
		// closing a Swing window executes viewDidClose before calling remove on me
		// so the model variable is already nil and .remove doesn't clean up the model
		// I'll keep the model in a variable that crucial doesn't know about
	var	myModel;
	
	guiBody { arg lay;
		lay.startRow;
		leftFlow = FlowView(lay, Rect(0, 0, model.numChannels * 13 + 10, 210), margin: 2@2);
		multiSl = GUI.multiSliderView.new(leftFlow, Rect(0, 0, model.numChannels * 13 + 2, 200));
		
		rightFlow = FlowView(lay, Rect(0, 0,
				// ax + b(x-1) = ax + bx - b = (a+b)x - b
			max((lay.decorator.gap.x + 50) * model.numChannels - lay.decorator.gap.x + 10, 210),
			210), margin: 2@2);	// height

		clipButtons = Array.fill(model.numChannels, { arg i;
			GUI.button.new(rightFlow, Rect(0, 0, 50, 20))
				.states_([
					["ok", Color.black, Color.grey],
					["CLIP", Color.white, Color.red]
				])
				.action_({ // arg b;
					clipButtons.do({ |b, j|
						Post << "Channel " << j << " maximum clip: " << maxClip[j] << "\n";
						maxClip[j] = 0;
						b.value_(0)
					});
				});
		});
		rightFlow.startRow;
		maxRecentView = GUI.textView.new(rightFlow, Rect(0, 0, 200, 100));
		
		maxClip = 0 ! model.numChannels;
		recentSize = model.freq*2;
		maxRecent = Array.new(recentSize);
		myModel = model;
	}
	
	update {
		var newpeaks, str;
		newpeaks = model.peaks.collect({ arg p, i; 
			(p > maxClip[i]).if({ maxClip[i] = p });
			((p = p.abs) > 1).if({
				{ if(multiSl.notClosed) { clipButtons.at(i).value_(1) } }.defer;
			});
			p.clip(0,1)
		});
		// update "recent peak" display
		(maxRecent.size >= recentSize).if({ maxRecent.removeAt(0) });
		maxRecent.add(model.peaks);
		str = "Channel peaks:\n\n";
		maxRecent.flop.do({ |chan, i|
			str = str ++ format("%: % dB\n", i+1, chan.maxItem.ampdb.round(0.01));
		});
		{
			if(multiSl.notClosed) {
				multiSl.value_(newpeaks.sqrt);
				maxRecentView.setString(str, 0, maxRecentView.string.size);
			};
		}.defer;
	}
	
	remove {
		var	tempModel = myModel;
		if(myModel.notNil) {
			tempModel = myModel;
				// must do this first to prevent infinite recursion
			myModel = nil;
			tempModel.free;
		}
	}
	
}
