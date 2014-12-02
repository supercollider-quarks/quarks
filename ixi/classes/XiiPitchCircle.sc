// PitchCircle 2008-2009 Tom Hall. 
// GNU licence, http://gnu.org/copyleft/
// reg /*at*/ ludions /*dot*/ com 
// Latest version available at www.ludions.com/sc/
// version 2009-04-15 


// messed around with by ixi

XiiPitchCircle {

	var size, <win, <tonic, <>drawInts, <mod, <set, radius, <>lines, <offset=0, <steps, <>notes, <scs, <>integers, <>dotsColFn, <>label, text, <>labelAlign, aHLDots, <>labelSize, num, width, height, txtH, edge, <compView, usrView;
	
	*new { arg steps, tonic, mod, offset, drawInts, size, win, num;
		^super.new.initXiiPitchCircle(steps, tonic, mod, offset, drawInts, size, win, num)
	}
	
	initXiiPitchCircle { arg aSteps, aTonic, aMod, anOffset, aDrawInts, anSize, anWin, aNum;
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
		compView = CompositeView.new(win, Rect(810, 0, size, size +txtH));
		text = StaticText(compView, Rect(0, 0, size, txtH))
				.align_(labelAlign)
				.font_(Font("Helvetica", labelSize))
				.string_("");
		
		// Pen draws in here
		usrView = UserView(compView, Rect(0, txtH, size, size)).canFocus_(false);
				
		scs= XiiDiscs.new; // does the interval calcs
		dotsColFn = { Color.black };
		lines = true;
		this.makeNotes(tonic, mod, offset, steps);
		usrView.drawFunc = { this.makeCircle };
		usrView.refresh;
	}
	
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
			integers = integers.rotate(offset);
			notes = notes.rotate(offset);
		});
	}
	
	hlDots_ {|dots|
		if(dots.isArray.not, {
			dots = dots.bubble;
		});
		^aHLDots = dots;
	}
	
	hlDots {
		^aHLDots
	}
	
	
	mod_ {|mod|
		this.makeNotes(aMod: mod);
	}
	
	tonic_ {|tonic|
		this.makeNotes(aTonic: tonic);
	}
	
	offset_ {|offset|
		this.makeNotes(anOffset: offset);
	}
	
	steps_ {|steps|
		this.makeNotes(aSteps: steps);
	}
	
	setAll { |mod=1, tonic=0, offset=0, steps=12|
		this.makeNotes(tonic, mod, offset, steps)
	}
	
	set_ {|aSet|
		scs.checkSubset(aSet, integers); // warns if needed
		set = aSet
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
	
	drawSet { |aSet, aOffset=0, aLabel, aDotsCol|
		if(aSet.isNil, {
			aSet = set;
		}, {
			// makes aSet the default set
			set = aSet
		});
		aLabel = aLabel ? set;
		aDotsCol = aDotsCol ? dotsColFn.value(set);
		usrView.drawFunc = {
			this.makeCircle(aOffset);
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
		dotsCol2 = aDotsCol ? Color.green;
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
	
	makeCircle {arg offset=0;
		var noteLabel, adjustedOffset, centre;
		notes = [ "C", "C#", "D", "D#", "E", "F", "F#", "G", "G#", "A", "A#", "B" ];
		notes = notes.rotate(offset.neg);
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
			noteLabel = notes@j;
			Pen.stringAtPoint(
				noteLabel,  
				Polar.new(radius * 0.75, 1.5pi + ((2pi/steps) * j)).asPoint 
			);
		});
		Pen.translate(centre*0.05, centre * 0.07); // adjust back to centred		// Draw the lines
		steps.do({ |i|
			Pen.moveTo(0@((radius * 0.96).neg));
			Pen.lineTo(0@((radius * 1.04).neg));
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
			if (aHLDots.includes(el), {
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



XiiDiscs {

	var <sharpsArr, <flatsArr, <c7Arr, <sharpsC7, <flatsC7, <d3rds;
	
	*new { ^super.new.makeLists }
	
	makeLists {
		sharpsArr = ["C", "C#", "D", "D#", "E", "F", "F#", "G", "G#", "A", "A#", "B"];
		flatsArr = ["C", "Db", "D", "Eb", "E", "F", "Gb", "G", "Ab", "A", "Bb", "B"]; 
		c7Arr = (0..(11)).collect({|el| if(el.odd, {(el+6)%12}, {el})});
		sharpsC7 = c7Arr.collect({|el, i| sharpsArr[el] });
		flatsC7 = c7Arr.collect({|el, i| flatsArr[el] });
	}
	
	indexOfC7 {|int|
		^c7Arr.detectIndex({|el| el==int})
	}
	
	nameNonClem { |tonic|
		^[0, 7, 2, 9, 4, 11].includes(tonic) // sharp keys, else flat
	}
	
	namesC7 {|tonic=0, card=7|
		var notes;
		notes = if(this.nameNonClem(tonic), {sharpsC7}, {flatsC7});
		if(tonic==6, {notes.put(5, "Cb")}); // hack to have correct note names for Gb maj
		^notes.rotate(13-this.indexOfC7(tonic)).keep(card)
	}
	
	intsC7 {|tonic=0, card=7|
		^c7Arr.rotate(13-this.indexOfC7(tonic)).keep(card)
	}

	intsSw {|tonic=0, card=7|
		var ints, index;
		ints = this.intsC7(tonic, card).sort;
		index = ints.detectIndex({|el| el==tonic});
		^ints.rotate(index.neg);
	}
	
	ints3rds { |tonic=0, card=7|
		var arr, rtnArr = []; 
		arr = this.intsC7(tonic, card);
		arr = arr.rotate(-1).extend(12,  nil).clump(4);
		arr[0].do{|i, j| rtnArr = rtnArr ++ [i, arr[1][j], arr[2][j] ] };
		^rtnArr = rtnArr.reject({|i| i.isNil});	
	}
	
	names3rds {|tonic=0, card=7|
		var notes;
		notes = this.noteNames(tonic, card);
		^this.ints3rds(tonic, card).collect({|el, i| notes[el]})
	}
	
	noteNames { |tonic=0, card=7|
		var	notes = if(this.nameNonClem(tonic), {sharpsArr}, {flatsArr});
		if(tonic==6, {notes.put(11, "Cb")}); // hack to have correct note names
		^notes
	}
	
	namesSw {|tonic=0, card=7|
		var notes;
		notes = this.noteNames(tonic, card);
		^this.intsSw(tonic, card).collect({|el, i| notes[el]})
	}
	
		
	sortC7 { |arr|
		^arr.sort({ arg a, b; 
			c7Arr.detectIndex({|el| el==a}) <= c7Arr.detectIndex({|el| el==b}) 
		});
	}

	// superset is the reference set in 3rds against which to sort
	sort3rds { | arr, superset | 
		^arr.sort({ arg a, b; 
			superset.detectIndex({|el| el==a}) <= superset.detectIndex({|el| el==b}) });
	}

	complement { |aSet, superTonic=0, superCard=12|
		if(aSet.any({|i| i.isString}), {
			"Set must be in integer format".error; // for now			^nil	
		});
		^symmetricDifference(aSet, this.intsC7(superTonic, superCard))
	}

	findIntervals { |subset, superset|
		^subset.collect({|pc| 
				superset.detectIndex({|el| el==pc}) 
		});
	}

	checkSubset { |subset, superset|
		if(subset.isSubsetOf(superset), {
			^true
		}, {
			format("Subset element(s) not included in superset: %", subset.difference(superset)).error;
			^false;
		});
	}
	
}


/*
a = Discs.new

a.namesC7(0, 12);
a.namesC7(0, 10);

a.namesC7(6, 12);
a.namesSw(6, 9)
a.intsC7(3)
a.intsC7(4)
a.intsSw(3)
a.namesSw(3, 7, false)
a.namesSw(4, 8)
a.namesC7(4, 8);
a.namesSw(11);
a.intsC7(11);
b = a.intsSw(11);
// puts a set into c7 order
a.sortC7([2, 7, 0, 3]) // (may be not rotated to match d)

b = a.intsC7(3)
a.sortC7(b)
a.complement(b)
h = a.namesSw(3, 7) //names
a.complement(h) // error warning
h = a.intsSw(3)
a.complement(h) //OK

a= Discs.new
a.checkSubset([0, 1], Set[0, 2, 4, 6])
a.namesC7(0, card:7);
b = a.intsC7(0, card:7); // integers
a.findIntervals([0, 1, 11], b)

a.findIntervals([0, 2, 11], b) // => [1, 3, 6]
// ie [0, 2, 11] have positions [1, 3, 6] in [ 5, 0, 7, 2, 9, 4, 11 ]

a.c7Arr
a.namesC7(0, 12);
a.namesC7(0, 10);

a = Discs.new
a.ints3rds(0, 7)
a.names3rds(0, 7)
a.names3rds(0, 8) // 3rds not really possible

*/
