
/*
(
w = Window.new("test", Rect(0,0, 800, 600)).front;
w.view.decorator = FlowLayout.new(w.view.bounds);
b = {
	var spec = Spec.specs.select({|x| x.isKindOf(ControlSpec)}).keys.choose;
	MinBox.new(w, (0@0)@([rrand(20,100)@rrand(20,100)].choose))
		.mode_(MinBox.modes.choose)
		.textMode_(MinBox.textModes.choose)
		.label_(spec.asString)
		.align_(MinBox.aligns.choose)
		.spec_(Spec.specs[spec])
		.input_(1.0.rand)

}!48;
)
*/

MinBox {
	classvar <>defaultMode;
	classvar <>defaultSkin;
	classvar <modes, <textModes, <aligns;

	var <view, <bounds;
	var center, or, ir, radius, hit, typing=false, string, stringColor;
	var showLabel, showValue, labelr,valuer;
	var <value, prevValue, <mode, <textMode, <>action;
	var <>loColor, <>hiColor, <>normalColor, <>typingColor, <>lineColor, <>font, <skin;
	var <>step, <round, <>align, <spec, specStep;
	var keyString, <>editable;
	var <label;
	var centered; //ak-centered

	var <>mouseDownAction, <>mouseUpAction, <>mouseMoveAction;

	*viewClass { ^UserView }
	
	*initClass {

		defaultMode='vert';
		modes = [\vert, \horiz, \round, \blend,\clear, \button];
		textModes = [\both,\switch,\label,\value];
		aligns = [\left, \right, \center, \full];
		
		defaultSkin = if(defaultSkin.isNil) { (
			hi:	Color(0.6, 0.2, 0.0, 1.0),
			lo:	Color(0.2, 0.4, 0.6, 1.0),
			text:	Color.gray(0.9,0.8),
			type:	Color(1.0, 0.8, 0.2, 1.0),
			line:	Color.gray(0.1,0.8),
			font:	Font("Monaco", 12.0),
			defaultMode: 'vert'
		) } {
			defaultSkin
		};
		
		Class.initClassTree(GUI);	

		StartUp.add({
			GUI.skins.default.put('minbox', defaultSkin);
		});

	}

	*new { arg parent, bounds;
		^super.new.init( parent, bounds );
	}

	init { arg argParent, argBounds;
	

		argBounds = this.calcConsts(argBounds.asRect);

		view = UserView.new( argParent, argBounds );

		value = 0;
		round = 0.01;
		this.spec = \unipolar; // also sets value

		skin = if(GUI.skins.default.minbox.isNil) {
			this.class.defaultSkin
		} {
			GUI.skins.default.minbox.default 
		};

		this.oldMethodsCompat(skin);

		view.addAction({ arg view ... rest; this.mouseDown( *rest )},\mouseDownAction);
		view.addAction({ arg view ... rest; this.mouseUp( *rest )},\mouseUpAction);
		view.addAction({ arg view ... rest; this.mouseMove ( *rest )},\mouseMoveAction);
		view.addAction({ arg view ... rest; this.defaultKeyDownAction( *rest )},\keyDownAction);
		view.beginDragAction       = { value.asFloat };
		view.receiveDragHandler    = { this.valueAction(GUI.view.currentDrag) };
		view.canReceiveDragHandler = { GUI.view.currentDrag.isNumber };
		view.action = {|view| this.action.value(this) };
		
		editable = true;

		string = value.round(round).asString;
		step = 0.001;
		mode = defaultMode;
		
		// NOTE not efficient, calls calcConstants again
		this.textMode_(\value);

		align = \center;
		label = "";
				
		view.drawFunc              = { this.draw };

		^this;
	}

	calcConsts { arg rect;
		bounds = rect;
		or = ((0.5@0.5)@(rect.extent-(0.5@0.5)));
		ir = Rect.fromRect(or);
		
		if([\both,\show].includes(textMode)) {
			labelr = or.insetBy(2).height_((or.height*0.5).max(16));
			valuer = Rect.fromRect(labelr).bottom_(or.bottom-2);
		} {
			labelr = or.insetBy(2);
			valuer = labelr;
		};

		radius = (or.width + or.height)*0.5;
		center = or.center;
				
		^rect
	}

	view_ { arg v;
		view = v;
		view.onClose = { this.viewDidClose };
	}
	
	viewDidClose { view = nil }
	
	remove { 	if(view.notNil) { view.remove }	}
	
	doesNotUnderstand { arg ... args;
		var result = view.perform( *args );
		^(if( result === view) { this } { result });
	}

	bounds_ { arg rect;
		rect = this.calcConsts(rect);
		view.bounds_(rect);
	}

	mode_ { arg argMode;
		if(argMode==\default) 
		{ mode = defaultMode }
		{ mode = argMode };
		this.calcConsts(this.bounds);
	}

	textMode_ { arg argTextMode=\value;
		textMode = argTextMode;
		showLabel = [\label,\both,\show,\switch].includes(textMode);
		showValue = [\value,\both,\show,\default].includes(textMode);

		this.calcConsts(this.bounds);
	}

	draw {
		var in = this.input;
		if(bounds!=view.bounds) { this.calcConsts(view.bounds) };
		
		Pen.width = 1;
		
		
		
		switch(mode)
			{ \round } {
				Pen.fillColor = lineColor;
				Pen.fillRect(or);
				Pen.fillColor = loColor;
				Pen.addAnnularWedge(or.center, 0, radius, -1.25pi, 1.5pi);
				Pen.fill;
				Pen.strokeColor_(lineColor);
				Pen.addAnnularWedge(or.center, 0, radius, -1.25pi, 1.5pi);
				Pen.stroke;
				Pen.fillColor = hiColor;
				centered.if { //ak-centered-start
					Pen.addAnnularWedge(or.center, 0, 
						radius, -0.5pi, (in-0.5)*1.5pi);
				} { 
					Pen.addAnnularWedge(or.center, 0, 
						radius, -1.25pi, in*1.5pi);
				};//ak-centered-stop
				Pen.stroke;
				centered.if { //ak-centered-start
					Pen.addAnnularWedge(or.center, 0, 
						radius, -0.5pi, (in-0.5)*1.5pi);
				} { 
					Pen.addAnnularWedge(or.center, 0, 
						radius, -1.25pi, in*1.5pi);
				};//ak-centered-stop
				Pen.fill;
			}
			{\vert } {
				centered.if { //ak-centered-start
					ir.top_(or.height*0.5);
					ir.height_(or.height*(0.5-in));
				}{
					ir.top_(or.height*(1-in));
				};//ak-centered-stop
				Pen.color = loColor;
				Pen.fillRect(or);
				Pen.color = hiColor;
				Pen.fillRect(ir);
			
				Pen.strokeColor_(lineColor);
				Pen.addRect(or);
				Pen.line((ir.leftTop),(ir.rightTop));
				Pen.stroke;			
			}
			{\horiz } {
				centered.if { //ak-centered-start
					ir.left_(or.width*0.5);
					ir.width_(or.width*(in-0.5));
				}{
					ir.right_(or.width*in);
				};//ak-centered-stop
				Pen.color = loColor;
				Pen.fillRect(or);
				Pen.color = hiColor;
				Pen.fillRect(ir);
			
				Pen.strokeColor_(lineColor);
				Pen.addRect(or);
				Pen.line((ir.rightTop),(ir.rightBottom));
				Pen.stroke;	
			}
			{\blend} {
				Pen.fillColor = loColor.blend(hiColor, in);
				Pen.strokeColor_(lineColor);
				Pen.fillRect(or);
				Pen.strokeRect(or);				
			}
			{\button} {
				Pen.fillColor = loColor.blend(hiColor, in);
				Pen.strokeColor_(lineColor);
				Pen.strokeRect(or);
				Pen.fillRect(or);
				
			}
			{\clear} {
				Pen.fillColor = lineColor;
				Pen.fillRect(or);
			};

		Pen.color = stringColor;
		Pen.font = font;

		if(showLabel) {
			if(align==\full) 
			{ Pen.stringLeftJustIn(label, labelr) } // if full, draw left
			{ Pen.stringCenteredIn(label, labelr) }; // else centered
		};

		if(showValue) {
			switch(align)
			{ \left } { Pen.stringLeftJustIn(string, valuer) }
			{ \right } { Pen.stringRightJustIn(string, valuer) }
			{ \center } { Pen.stringCenteredIn(string, valuer) }
			{ \full } { Pen.stringRightJustIn(string, valuer) }
			{ Pen.stringCenteredIn(string, valuer) };
		};
		
	}

	mouseDown { arg x, y, modifiers, buttonNumber, clickCount;
		var in = this.input;
		var inc;

		hit = x @ y;
				
		this.mouseMove(x, y, modifiers);
		
		if(textMode==\switch) {
			showLabel = false;
			showValue = true;
		};
		
		if(mode==\button) {
			if(this.editable) {
//				inc = case
//					{ (modifiers & 262144 == 262144) } { if(in>=0.5) { 0.0 } { 1.0 } }
//					{ (modifiers & 524288 == 524288) } { (in-step).wrap(0.0, 1.0) }
//					{ (in+step).wrap(0.0, 1.0001) };
				inc = if(in>=0.5) { 0.0 } { 1.0 };

				this.valueAction = spec.map( inc ).round(round);
			}
		};

		mouseDownAction.value(this, x, y, modifiers, buttonNumber, clickCount);

	}
		
	mouseUp { arg x, y, modifiers, buttonNumber, clickCount;
		if(textMode==\switch) {
			showLabel = true;
			showValue = false;
		};
		
		this.refresh;
		mouseUpAction.value(this, x, y, modifiers, buttonNumber, clickCount);
	}
	
	mouseMove { arg x, y, modifiers;
		var mp, pt, angle, inc = 0;
		var in = this.input;

		if(this.editable) {
			inc = case
				{ (modifiers & 262144 == 262144) } { 10 }
				{ (modifiers & 524288 == 524288) } { 0.1 }
				{ 1 };
							
			if (modifiers & 1048576 != 1048576) { // we are not dragging out - apple key
				case
					{ [\vert, \blend, \clear].includes(mode) } {
						inc = inc*step*(hit.y - y);
						this.valueAction = spec.map(in + inc).round(round);
						if( inc.abs > specStep ) { hit = Point(x,y) };
					}
					{ (mode == \horiz) } { 
						inc = inc*step.neg*(hit.x - x);
						this.valueAction = spec.map(in + inc).round(round);
//						if( inc.abs > specStep ) { 
						hit = Point(x,y) 
//						};
					}
					{ mode == \round } {
						pt = center - Point(x,y);
						angle = Point(pt.y, pt.x.neg).theta;
						if ((angle >= -0.80pi) and: { angle <= 0.80pi} , {
							this.valueAction = spec.map([-0.75pi, 0.75pi].asSpec
								.unmap(angle)).round(round);
						});
					}
			};
		};

		mouseMoveAction.value(this, x, y, modifiers);	
	}	

	increment { this.valueAction = spec.map(this.input + step); }
	decrement { this.valueAction = spec.map(this.input - step); }
	
	defaultKeyDownAction { arg char, modifiers, unicode;
	
		if(this.editable) {
			
			// standard chardown
			if (unicode == 16rF700, { this.increment; ^this });
			if (unicode == 16rF703, { this.increment; ^this });
			if (unicode == 16rF701, { this.decrement; ^this });
			if (unicode == 16rF702, { this.decrement; ^this });
			if ((char == 3.asAscii) || (char == $\r) || (char == $\n), { // enter key
				if (keyString.notNil,{ // no error on repeated enter
				
					this.textMode_(textMode);

					this.valueAction_(keyString.asFloat);
				});
				^this
			});
			if (char == 127.asAscii, { // delete key
				keyString = nil;
				string = (value.round(round)).asString;
				stringColor = normalColor;

				this.textMode_(textMode);

				this.refresh;
				^this
			});
			if (char.isDecDigit || "+-.eE".includes(char), {
				if (keyString.isNil, { 
					keyString = String.new;
					stringColor = typingColor;
					
					if([\label,\switch,\none,\hide].includes(textMode)) { 
						showLabel = false;
						showValue = true 
					};
				});
				keyString = keyString.add(char);
				string = keyString;
				this.refresh;
				^this
			});
		};
		
		^nil		// bubble if it's an invalid key
	}
	
	spec_ {arg argSpec;
		spec = argSpec.asSpec;
		centered = if((spec.minval + spec.maxval)==0, true, false); //ak-centered
		specStep = (spec.step / spec.range)*0.5;

		this.value = spec.default ?? { spec.constrain(value) };
		this.refresh;
		this.changed(\spec);
	}
	
	refresh { view.refresh; ^this }

	round_{arg argRound; round = argRound; }

	input_ { arg in; ^this.value_(spec.map(in)) }
	input { ^spec.unmap(value) }

	value_ { arg val;
		keyString = nil;
		stringColor = normalColor;
		value = spec.constrain(val);
//		string = value.asString;
		string = value.round(round).asString;
		this.refresh;
		this.changed(\synch, this);		
	}

	valueAction_ { arg val;
		var prev;
		prev = value;
		this.value = val !? { spec.constrain(val) };
		if (value != prev) { this.doAction };
		
		this.refresh;
	}
	
	label_ { arg l; label = l.asString }

	skin_ { arg newskin;
		if ( newskin.notNil ) {
			skin = newskin;
			newskin.proto_( GUI.skins.default.minbox.default );
			this.oldMethodsCompat;
			this.refresh;
		}{
			format("%: skin not found.", this.class).inform;
		};
	}
	
	oldMethodsCompat {
		loColor = skin.lo;
		hiColor = skin.hi;
		typingColor = skin.type;
		normalColor = skin.text;
		lineColor = skin.line;
		font = skin.font;
		defaultMode = skin.defaultMode;
		stringColor = normalColor;
	}

	*paletteExample{arg parent, bounds;
		^this.new(parent, bounds.asRect.height@bounds.asRect.height);	
	}
	
}
       