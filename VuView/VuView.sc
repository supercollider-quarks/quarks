// 06/2006 blackrain at realizedsound dot net
// 03.10.2008:
//	- relative origin
//	- A subclass of SCViewHolder
// 2008-04-12
//  - extended Tom Hall 
// 	- needleColor

VuView : SCViewHolder {
	var <value=0, <>needleColor;
	*new { arg parent, bounds;
		^super.new.init(parent, bounds);
	}
	init { arg parent, bounds;
		var area;
		needleColor = Color.black(0.8, 0.8);
		this.view_(GUI.userView.new(parent, bounds))
			.relativeOrigin_(true);
		
		this.view.drawFunc_({
			var bounds;
			bounds = Rect(0, 0, this.view.bounds.width, this.view.bounds.height);
			// frame
			Color.black.alpha_(0.4).set;
			GUI.pen.width = 2;
			GUI.pen.moveTo(bounds.left @ (bounds.top + bounds.height));
			GUI.pen.lineTo(bounds.left @ bounds.top);
			GUI.pen.lineTo((bounds.left + bounds.width) @ bounds.top);
			GUI.pen.stroke;
	
			Color.white.alpha_(0.4).set;
			GUI.pen.moveTo(bounds.left @ (bounds.top + bounds.height));
			GUI.pen.lineTo((bounds.left + bounds.width) @ (bounds.top +
				bounds.height));
			GUI.pen.lineTo((bounds.left + bounds.width) @ bounds.top);
			GUI.pen.stroke;
	
			// center
			Color.black.alpha_(0.2).set;
			GUI.pen.addWedge(bounds.center.x @ (bounds.top + bounds.height - 1), 
				bounds.height * 0.20, 0, -pi);
			GUI.pen.perform(\fill);
	
			// scale
			Color.black.alpha_(0.2).set;
			GUI.pen.addAnnularWedge(bounds.center.x @
				(bounds.top + bounds.height - 1), 
				bounds.height * 0.8, bounds.height * 0.95, -0.75pi, 0.5pi);
			GUI.pen.perform(\fill);
	
			// dial
			needleColor.set;
			GUI.pen.width = 1;
			GUI.pen.moveTo(bounds.center.x @ (bounds.top + bounds.height - 1));
			GUI.pen.lineTo(Polar.new(bounds.height * 0.95, 
				[-0.75pi, -0.25pi, \linear].asSpec.map(value)).asPoint +
					(bounds.center.x @ (bounds.top + bounds.height)));
			GUI.pen.stroke;
			
			// eye candy needle dot
			Color.black.set;
			GUI.pen.addWedge(bounds.center.x @ (bounds.top + bounds.height - 1), 
				bounds.height * 0.04, 0, -pi);
			GUI.pen.perform(\fill);

		});
	}

	value_ { arg val;
		value = val;
		this.refresh;
	}	
}



/*
(
var txt;
w = SCWindow("Meter", Rect(300,400,300,120));
w.view.decorator = FlowLayout(w.view.bounds);
w.front;
m = VuView(w, 120@80);
k = Knob(w, 32@32)
	.action = {|v|
		m.value = \amp.asSpec.map(v.value);
		txt.string = format("val: % dB", m.value.ampdb.round(0.01));
	};
txt = SCStaticText(w, 200@14);
)

// needle color changes, evaluate a few times
(
var signal, needleColor;
signal = rrand(0.3, 1.6); // possibly distorted signal
if(signal> 1.0, {
	needleColor=Color.red(1.0, 0.8);
	format("Signal value distorted: %", signal).postln; 
}, {
	needleColor=Color.black(0.8, 0.8);
});
m.needleColor_(needleColor); // doesn't update until VuView value changes
k.valueAction_(signal);
signal
)


*/
