
// SC 3 version 0.3, August 2005

// (first version 0.2, July 2001)
// (c) julian rohrhuber

// distributed under the terms of the GNU General Public License 
// full notice in ProteinBioSynthesis main folder


AminoacidPattern2 : CodonPattern {

	asStream { 
		^Routine({
			var numstream, triplstream;
			
			triplstream = super.asStream;
			numstream = super.asStream.collect({ arg item;
						this.translate(item)
			});
	
			loop({
				[numstream.next, triplstream.next].yield;
			})
		})
			
	}
}

AminoacidPattern3 : AminoacidPattern {

	asStream { 
		^Routine({
			var numstream, namestream;
			numstream = super.asStream;
			namestream = AminoacidNames(numstream).asStream;
			loop({
				[numstream.next, namestream.next].yield;
			})
		})
			
	}
}
	