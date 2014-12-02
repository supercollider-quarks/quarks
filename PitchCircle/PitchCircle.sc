// PitchCircle 2008-2012 Tom Hall. 
// GNU licence, http://gnu.org/copyleft/
// reg /*at*/ ludions /*dot*/ com 
// Latest version usually available at www.ludions.com/sc/
// version 2012-05-05 (bugfix hlDots_ method) 

PitchCircle {

	var size, <win, <tonic, <>drawInts, <mod, <set, radius, <>lines, <offset=0, <steps, <>notes, <scs, <>integers, <>dotsColFn, <>label, text, <>labelAlign, aHLDots, <>labelSize, num, width, height, txtH, edge, <compView, usrView;
	
	*new { arg steps, tonic, mod, offset, drawInts, size, win, num;
		^super.new.initPitchCircle(steps, tonic, mod, offset, drawInts, size, win, num)
	}
	
	initPitchCircle { arg aSteps, aTonic, aMod, anOffset, aDrawInts, anSize, anWin, aNum;
		steps = aSteps ? 12;
		case {steps < 2} {"Minimum steps is 2".inform; steps=2}
			{steps > 12} 
				{"Normal operation ceases at > 12 steps".warn; 
				};
		tonic = aTonic ? 0;
		mod = aMod ? 1;
		offset = anOffset ? 0;
		drawInts = aDrawInts ? false;
		size = anSize ? 300;
		txtH = size/8;
		win = anWin;
		num = aNum ? 1;
		edge = 20;
		width= (size * num) + (edge*num);
		height= size + txtH + edge;
		aHLDots = [];
		if(win.isNil, {
			win = GUI.window.new(
				"Pitch circle", 
				Rect(rrand(0, 50), rrand(10, 500), width, height), false
			).front;
			win.view.background_(Color.new255(240, 240, 240)); // grey
			win.view.decorator=FlowLayout(win.view.bounds, (edge/2)@(edge/2), edge@(edge/4) );
		});
		
		radius = size/2.25;
		set = ([0, 2, 4, 5, 7, 9, 11]+tonic)%12; // default
		label = set;
		labelAlign = \center;
		labelSize = txtH * 0.4; // radius* 0.1;
		compView = CompositeView.new(win, Rect(0, 0, size, size +txtH));
		text = StaticText(compView, Rect(0, 0, size, txtH * 0.75)) 				.align_(labelAlign)
				.font_(Font("Helvetica", labelSize))
				.string_("");
		
		// Pen draws in here
		usrView = UserView(compView, Rect(0, txtH, size, size)).canFocus_(false);
				
		scs= Discs.new; // does the interval calcs
		dotsColFn = { Color.blue };
		lines = true;
		this.makeNotes(tonic, mod, offset, steps);
		usrView.drawFunc = { this.makeCircle };
		usrView.refresh;
	}
	
	refresh { usrView.refresh }
	
	
	makeNotes { |aTonic, aMod, anOffset, aSteps|
		tonic = aTonic ? tonic;
		mod = aMod ? mod;
		offset = anOffset ? offset;
		steps = aSteps ? steps;
		case {mod==1} 
				{ integers = scs.intsSw(tonic, steps);
				  notes = scs.namesSw(tonic, steps) }
			{mod==5} 
				{   "mod 5 not currently supported, using mod 7".inform;
					integers = scs.intsC7(tonic, steps);					notes = scs.namesC7(tonic, steps);
					 }
			{mod==7} 
				{  integers = scs.intsC7(tonic, steps);
					notes = scs.namesC7(tonic, steps);
				}
			// only works "properly" for d, not other cardinalities
			{mod==3} 
				{ integers = scs.ints3rds(tonic, steps);
				  notes = scs.names3rds(tonic, steps) };
		
		while({integers[0] != tonic}, {
				integers = integers.rotate(-1);
				notes = notes.rotate(-1);
		});
		
		if(offset !=0, {
			integers = integers.rotate(offset.neg);
			notes = notes.rotate(offset.neg);
		});
	}
	
	hlDots_ {|dots|
		if(dots.isArray.not, {
			dots = dots.bubble;
		});
		aHLDots = dots;
		this.refresh;
		^dots
	}
	
	hlDots {
		^aHLDots
	}
		
	spell {|pc, noteStr|
		var index = integers.indexOf(pc);
		notes.put(index, noteStr);
		this.refresh;
		notes; 
	}

	spellAll {|arr|
		arr.pairsDo({ arg a, b; this.spell(a, b)});
	}

	
	mod_ {|mod|
		this.makeNotes(aMod: mod);
		this.refresh;	
	}
	
	tonic_ {|tonic|
		this.makeNotes(aTonic: tonic);
		this.refresh;	
	}
	
	offset_ {|anOffset|
//		this.makeNotes(anOffset: offset);
		var tmpOffset = (offset * 1.neg) + anOffset;
		integers = integers.rotate(tmpOffset.neg);
		notes = notes.rotate(tmpOffset.neg);
		offset = anOffset;
		this.refresh;		
	}
	
	steps_ {|steps|
		this.makeNotes(aSteps: steps);
		this.refresh;	
	}
	
	setAll { |mod=1, tonic=0, offset=0, steps=12|
		this.makeNotes(tonic, mod, offset, steps);
		this.drawLabel(set);
		this.refresh;	
	}
	
	set_ {|aSet|
		scs.checkSubset(aSet, integers); // warns if needed
		set = aSet
	}

	note_ {|note|
		note = note.bubble;
		scs.checkSubset(note, integers); // warns if needed
		set = note;
		this.drawSet;
	}
	
	complement {
		^scs.complement(set, tonic, steps)
	}
	
	drawCompl {|aDotsCol, aLabel|
		var compl = this.complement;
		aLabel = aLabel ? compl;
		this.addSet(compl, aDotsCol);
		this.drawLabel(aLabel);
	}
	
	drawSet { |aSet, aLabel, aDotsCol|
		if(aSet.isNil, {
			aSet = set;
		}, {
			// makes aSet the default set
			set = aSet
		});
		aLabel = aLabel ? set;
		aDotsCol = aDotsCol ? dotsColFn.value(set);
		usrView.drawFunc = {
			this.makeCircle;
			this.makeDots(set, aDotsCol);
		};
		usrView.refresh;
		this.drawLabel(aLabel);
	}

	drawSets { |aSets, aLabel, colsArr|
		var aColor, colors;
		colors = colsArr ? [Color.blue, Color.red, Color.green, Color.yellow, Color.grey, Color.magenta, Color.cyan];
		if(aSets[0].isArray.not, {
			"Sets must be contained in an Array".error;
			^this
		});
		aLabel = aLabel ? aSets;
		usrView.drawFunc = {
			this.makeCircle;
			aSets.do({|thisset, i|
				aColor = if (i > 6, {Color.rand}, {colors[i]});
				this.makeDots(thisset, aColor);
			});
		};
		usrView.refresh;
		this.drawLabel(aLabel);
	}
	
	addSet { |aSet, aLabel, aDotsCol|
		var set2, dotsCol2;
		set2 = aSet;
		dotsCol2 = aDotsCol ? Color.red;
		aLabel = aLabel ? format("%  %", set, set2);
		usrView.drawFunc = {
			this.makeCircle;
			this.makeDots(set, dotsColFn.value(set));
			this.makeDots(set2, dotsCol2);
		};
		usrView.refresh;
		this.drawLabel(aLabel);
	}
	
	addSpacing { |num=7, aDotsCol|
		var dotsCol2;
		if(mod != 1, {
			"addSpacing only works if mod is 1".warn;
			 ^this
		});
		dotsCol2 = aDotsCol ? Color.green;
		usrView.drawFunc = {
			this.makeCircle;
			this.makeDots(set, dotsColFn.value(set));
			this.makeSpace(num, dotsCol2);
		};
		usrView.refresh;
	}
	
	// adds dots at an equal distance around the circle
	makeSpace {|num, aCol | 
		aCol = aCol ? Color.green;
		num.do({ |i|
			// Draw the dots
			Pen.addArc(Point(0, radius.neg), radius * 0.05, 0, 2pi);
			Pen.color = aCol;
			Pen.perform(\fill);
			Pen.rotate(2pi/num);				
		});
	}
		
	drawLabel {|aString, align|
		aString = aString ? label;
		align = align ? labelAlign;
		text.align = labelAlign;
		text.string = aString;
		text.font_(Font("Helvetica",  labelSize));
	}
	
	linesToggle {
		lines = if(lines, {false}, {true});
		this.refresh;
	}
	
	makeCircle {
		var noteLabel, centre;
		centre = size/2;
		// Draw the circle
		Pen.color = Color.black; 
		// *addArc(center, radius, startAngle, arcAngle)
		Pen.addArc(Point(centre, centre), radius, 2pi, 2pi);
		Pen.perform(\stroke);
		// Draw note names or digits		
		Pen.translate(centre*0.95, centre * 0.93); // adjusts for letter alignments
		Pen.font = Font( "Helvetica", radius * 0.125 );
		// leters aren't rotated in order to be horizontally aligned
		integers.do({ |i, j|  
			noteLabel = if (drawInts, { i.asString}, {notes@j });
			Pen.stringAtPoint(
				noteLabel,  
				Polar.new(radius * 0.75, 1.5pi + ((2pi/steps) * j)).asPoint 
			);
		});
		Pen.translate(centre*0.05, centre * 0.07); // adjust back to centred		// Draw the lines
		steps.do({ |i|
			Pen.moveTo(0@((radius * 0.92).neg));
			// formally 1.08, hack to allow SpatioScope to overlay
			Pen.lineTo(0@((radius * 1.06).neg)); 
			Pen.moveTo(0@0);
			Pen.color = Color.black;
			Pen.stroke;
			Pen.rotate((2pi/steps));
		});
	}	
		
	// goes only to the dots required
	makeDots { |aSet, aDotsCol|
		var setInts, rotationPt, array, circDiv;
		var positions;
		if(aSet.isEmpty, {
		//	"set is empty".warn; 
			^this
		});
		circDiv = 2pi/steps;
		array = aSet.asArray.sort;
		Pen.color = aDotsCol;
		if(this.checkSubset(array), {
			case 
				{mod==7} {array = scs.sortC7(array)}
				{mod==3} {array = scs.sort3rds(array, integers)};
			positions = this.findIntervals(array);
		}, {
			^this.makeDots(array.sect(integers), aDotsCol);
		});
		// draw lines between elements
		if(lines, {
			Pen.moveTo(Polar.new(radius, 1.5pi + ((2pi/steps) * positions[0])).asPoint);
			positions.rotate(-1).do({|el|
				Pen.lineTo(Polar.new(radius, 1.5pi + ((2pi/steps) * el)).asPoint);
			});
			Pen.perform(\stroke);
		});	
		
		// Draw the dots
		positions.do({ |el, i|
			setInts = if(i==0, {el}, {el-positions[i-1]});
			rotationPt = circDiv * setInts; 
			Pen.rotate(rotationPt);	// rotate to correct position
			Pen.addArc(Point(0, radius.neg), radius*0.05, 0, 2pi); // orig
			Pen.width = 1;
			Pen.perform(\fill);
			
			// highlight dot with extra circle
			// also adjust for any offset
			if (((aHLDots - offset)%steps).includes(el), {
				Pen.addArc(Point(0, radius.neg), radius*0.07, 0, 2pi); 
				Pen.width = if(size >= 350, {2}, {1});
				Pen.perform(\stroke);
			});
			
		});
		// return to start position
		Pen.rotate(circDiv * (steps - positions.last)); 
	}
	
	
	findIntervals {|subset|
		^scs.findIntervals(subset, integers);
	}

	checkSubset {|subset|
		^scs.checkSubset(subset, integers);
	}
	
	close {
		win.close
	}
	
	front {
		win.front;
	}
		
}
