

SynthDefGraph {

	var <>def,uv,pen;
	var font,black,h,w;

	*new { arg def,parent,bounds;
		^super.new.def_(def).gui(parent,bounds)
	}
	gui { arg parent,bounds;
		if(parent.notNil,{
			parent = parent.asFlowView(bounds);
			uv = UserView(parent,bounds ?? {parent.indentedRemaining});
		},{
			bounds = bounds ?? {Rect(0,0,800,800)};
			parent = Window(def.name,bounds,scroll: true).front;
			uv = UserView(parent,parent.bounds.moveTo(0,0).resizeTo(2000,3000));
			uv.resize = 5;
		});
		black = Color.black;
		font = Font(Font.defaultSansFace,9);
		//uv.mouseDownAction = Message(this,\mouseDownAction,[]);
		pen = GUI.pen;
		this.updateSizes;
		uv.drawFunc = {this.draw};
	}
	updateSizes {
		h = uv.bounds.height / def.children.size.asFloat;
		h = max(h,12);
		h = min(h,20);
		w = 100;
	}
	draw {
		var b,gr;
		b = uv.bounds.moveTo(0,0);
		this.updateSizes;

		def.children.do { arg ugen,i;
			var r;
			r = this.rectForUgen(ugen);
			pen.color = this.colorForClass(ugen.class);
			pen.fillRect(r);
			pen.stringInRect(ugen.class.name.asString,r.moveBy(1,1),font,black);
			pen.color = this.colorForRate(ugen.rate);
			pen.strokeRect(r);

			ugen.inputs.do { arg in,ii;
				var inr,srcr;
				inr = Rect(w + (w*ii),ugen.synthIndex * h, w - 1,h-1);
				if(in.isKindOf(UGen),{
					pen.color = this.colorForClass(in.class);

				},{
					pen.color = Color.white;
				});
				pen.fillRect(inr);
				pen.stringInRect(in.asString,inr.moveBy(1,1),font,black);
				if(in.isKindOf(UGen),{
					srcr = this.rectForUgen(in);
					pen.color = this.colorForRate(in.rate);
					pen.line(inr.center,srcr.center);
					pen.stroke;
				})
			}
		};
	}
	rectForUgen { arg ugen;
		^Rect(0,ugen.synthIndex * h, w - 1,h-1);
	}
	colorForClass { arg class;
		var nodeID = class.classIndex - UGen.classIndex;
		^Color.new255(*((nodeID) * 911640000).asDigits(256,3)).alpha_(0.5)
	}
	colorForRate { arg rate;
		if(rate == \audio,{ ^Color.red });
		if(rate == \control,{ ^Color.blue });
		^Color.black;
	}
}

