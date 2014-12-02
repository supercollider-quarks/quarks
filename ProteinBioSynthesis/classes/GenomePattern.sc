
// SC 3 version 0.3, August 2005

// (first version 0.2, July 2001)
// (c) julian rohrhuber

// distributed under the terms of the GNU General Public License 
// full notice in ProteinBioSynthesis main folder




// use this to stream the letters of the bases themselves (like Pseq)


GenomePattern : ProteinBioSynthesis {
	var  <>repeats, <>offset;
	
	*new { arg genome, repeats=1, offset=0;
		^super.new(genome).repeats_(repeats).offset_(offset)
	}
	
	
	asStream { 
		^Routine.new({ arg inval;
			var item, offsetValue;
			offsetValue = offset.value;
			
				repeats.value.do({ arg j;
					size.do({ arg i;
						item = genome @@ (i + offsetValue);
						inval = item.embedInStream(inval);
					});
				});
			})
		
	}
	
	
}


// use this to stream triplets (codons)
CodonPattern : GenomePattern {
	asStream {
		^Routine({ arg inval;
			var inStream, codon, char= $t;
			inStream = super.asStream;
			loop({
				 codon = String.new;
				 3.do({
				 	char = inStream.next(inval);
				 	codon = codon ++ char.asString;
				 	});
				 if(char.notNil, { codon.asSymbol.yield }, { nil.alwaysYield });
			});
		});
	}
				
}

// use this to stream the index numbers of the aminoacids (from -1 to 19)
AminoacidPattern : CodonPattern {
	asStream {
		^super.asStream.collect({ arg item;
			this.translate(item)
		})
	}
}

/*ATest : AminoacidPattern  {
	test {
		^this.translate(\ccc)
	}

}
*/
// use this to stream the 
AminoacidNames : ProteinBioSynthesis {
	var <>pattern;
	*new { arg pattern;
		^super.new.pattern_(pattern);
	}
		 
	asStream {
		^pattern.asStream.collect({ arg item;
			aminoacids.at(item)
		})
	}
}



// test this, not reliable.
 
Ptranscribe : AminoacidPattern {
	asStream {
		^Routine({
			var inStream, codon, mode = \skip;
			inStream = super.asStream;
			loop({
				codon = inStream.next;
				if((codon == startcodon) && (mode == \skip), {  mode = \write });
				if((codon == stopcodon) && (mode == \write), {  mode = \skip });
				if(((mode == \write) && (codon != startcodon) && (codon != stopcodon)), { codon.yield });
				\test.yield;
			
			})
		})
	
	}
		

}




