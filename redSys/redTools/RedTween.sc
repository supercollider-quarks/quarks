//redFrik

RedTween {
	var <>source, <>target, <curve, <>inMin, <>inMax;
	var <func;
	*new {|source= 0.0, target= 1.0, curve, inMin= 0.0, inMax= 1.0|
		^super.newCopyArgs(source, target, curve, inMin, inMax).initRedTween;
	}
	initRedTween {
		if(curve.isNil, {
			//--nil becomes linear interpolation
			func= {|t| t*(target-source)+source};
		}, {
			if(curve.isNumber, {
				//--numbers are curvatures
				func= {|t| t.lincurve(0.0, 1.0, source, target, curve, nil)};
			}, {
				//--custom functions or objects from the Ease quark
				func= {|t| curve.value(t)*(target-source)+source};
			});
		});
	}
	*value {|t ...args| ^this.new(*args).value(t)}
	*ar {|t ...args| if(t.rate=='audio', {^this.(t, *args)}, {^this.(K2A.ar(t), *args)})}
	*kr {|t ...args| ^this.(A2K.kr(t), *args)}
	value {|t|
		if(inMax==inMin, {
			^func.value(t.clip(inMin, inMax));
		}, {
			if(inMin>inMax, {
				^func.value((t.clip(inMax, inMin)-inMax)/(inMin-inMax));
			}, {
				^func.value((t.clip(inMin, inMax)-inMin)/(inMax-inMin));
			});
		});
	}
}

+SequenceableCollection {
	asRedTween {^RedTween(*this)}
}
