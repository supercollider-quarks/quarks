NNdef : Ndef {

	classvar <>buildFRPControlNum;
	classvar <>buildFRPControlIndex;
	classvar <>buildCurrentNNdefKey;
	classvar <>eventNetworkBuilder;
	classvar <>buildControls;

	var <eventNetworks;
	var <frpControls;

	*nextControl {
		var current = NNdef.buildFRPControlNum;
		NNdef.buildFRPControlNum = current + 1;
		^"frpControl-%-%".format(buildFRPControlIndex, current).asSymbol
	}

	put { | index, obj, channelOffset = 0, extraArgs, now = true |
		var currentEN, newEN;
		NNdef.buildFRPControlNum = 0;
		NNdef.buildFRPControlIndex = index ?? 0;
		NNdef.buildCurrentNNdefKey = key;
		NNdef.buildControls = [];
		ENDef.tempBuilder = T([],[],[]);
		index = index ?? 0;
		if( eventNetworks.isNil ) { eventNetworks = Order.new };
		currentEN = eventNetworks.at(index);
		currentEN !? {
			if(currentEN.active) { currentEN.stop };
			eventNetworks.removeAt(index);
		};
		if( frpControls.isNil ) { frpControls = Order.new };
		frpControls.at(index) !? { |controls|
			controls.do{ |x|
				this.nodeMap.unset(x)
			}
		};
		/*this.nodeMap
		.keys.as(Array).select{ |n|
			"frpControl*".matchRegexp(n.asString)
		}.do{ |x| this.nodeMap.unset(x) };
		*/
		super.put(index, obj, channelOffset, extraArgs, now);
		if( obj.isFunction || (obj.isKindOf(Association)) ) {
			newEN = EventNetwork(Writer(Unit, ENDef.tempBuilder));
			eventNetworks = eventNetworks.put(index, newEN);
			newEN.start;
			frpControls.put(index, NNdef.buildControls);
		}
	}

	clear { | fadeTime = 0 |
		super.clear(fadeTime);
		eventNetworks.do( _.stop );
		eventNetworks = Order.new;
	}

}

+ FPSignal {

	enKr { |lag = 0.1, key, spec|

		var controlName = key ?? { NNdef.nextControl() };
		var thisUdef = NNdef.buildCurrentNNdefKey;
		NNdef.buildControls = NNdef.buildControls.addI(controlName);
		if(spec.notNil){
			NNdef(thisUdef).addSpec(controlName, spec);
			this.collect{ |v| IO{ NNdef(thisUdef).setUni(controlName, v) } }.enOut2;
			NNdef(thisUdef).setUni(controlName, this.now);
		}{
			this.collect{ |v| IO{ NNdef(thisUdef).set(controlName, v) } }.enOut2;
			NNdef(thisUdef).set(controlName, this.now);
		};
		^controlName.kr(this.now, lag)
	}

}


+ EventSource {

	enKr { |lag = 0.1, initialValue=0, key, spec|

		var controlName = key ?? { NNdef.nextControl() };
		var thisUdef = NNdef.buildCurrentNNdefKey;
		NNdef.buildControls = NNdef.buildControls.addI(controlName);
		if(spec.notNil){
			NNdef(thisUdef).addSpec(controlName, spec);
			this.collect{ |v| IO{ NNdef(thisUdef).setUni(controlName, v) } }.enOut;
		}{
			this.collect{ |v| IO{ NNdef(thisUdef).set(controlName, v) } }.enOut;
		};
		^controlName.kr(initialValue, lag)
	}

	enTr { |initialValue=0, key|

		var controlName = key ?? { NNdef.nextControl() };
		var thisUdef = NNdef.buildCurrentNNdefKey;
		this.collect{ |v| IO{ NNdef(thisUdef).set(controlName, v); NNdef(thisUdef).unset(controlName) } }.enOut;
		^controlName.tr(initialValue)
	}

}