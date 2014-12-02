Salience {
	classvar weights;
	var <>pitches, <>freqs, <>virtual, <>spectral; 
	
	
	*initClass { weights = [1, 0.5, 0.3, 0.2, 0.1, 0.06, 0.03, 0.015, 0.0075] }
	
	*new {^super.new.init}
	
	init { pitches = (); virtual = []; spectral = []}
	
	*subharmonics{|freqs, n = 6| ^Array.series(n, 1, 2).collect{|h| freqs / h}}
	
	*calc {|freqs, n = 6| var esto, subH = Salience.subharmonics(freqs, n).flop, fw;
			esto = this.new;
			esto.freqs = freqs;
			fw = subH.collect{|h| h.collect{|h0, j| [h0, weights[j]] }};
			fw.do{|x| x.do{|y| esto.pitches[y.first] = 0}}; // init dictionary values
			fw.do{|x0| // sum of weights for each freq
				x0.do{|x1| 
					esto.pitches[x1.first] = esto.pitches[x1.first] + x1.last
				};
			}; 
			^esto.separate;
	}
						
	separate {|sort = true| var v, s, order;
			this.pitches.keysValuesDo{|k,v|
				if (this.freqs.includes(k)) { 
					this.spectral = this.spectral.add([k, v])
				}{
					this.virtual = this.virtual.add([k, v])
				};
			};
			if (sort) {
				s = this.spectral.flop; 
				order = s[1].order({|a,b| a > b});
				this.spectral = [s[0][order], s[1][order]].flop;
				if (this.virtual.isEmpty.not) {
					v = this.virtual.flop;
					order = v[1].order({|a,b| a > b});
					this.virtual = [v[0][order], v[1][order]].flop;
				};
			};
			^this
	}
	
	select {|threshold| var r, order,
			d = this.pitches.values.select{|h| h >= threshold}.removeDuplicates;
			r = d.collect{|dd| [this.pitches.findKeyForValue(dd), dd]}.flop;
			order = r[1].order({|a,b| a > b});
			^[r[0][order], r[1][order]].flop
	}
	
	
	orderV {|threshold| var pitches, order;
		pitches = this.virtual.select{|h| h[1] >= threshold};
		order = pitches.flop[0].order;
		^pitches[order]; 		
	}

	orderS {|threshold| var pitches, order;
		pitches = this.spectral.select{|h| h[1] >= threshold};
		order = pitches.flop[0].order;
		^pitches[order]; 		
	}
	
	highestVirtual {var highestSalience;
		highestSalience = this.virtual[0][1];
		^this.orderV(highestSalience);
	}
	
	
	postNew {}

	postAll {	 ^this.virtual.do{|r| [r[0].asNote, r[1]].postln} }
	
	asString { ^this.pitches.keys.asArray.asNote }
	
	/*TO DO: multiplicity and 'compositional virtual pitch' */
}



