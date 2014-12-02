// part of wslib 2005
//
// conversion String <-> SequenceableCollection

+ SequenceableCollection {
	toString { ^this.collect(_ ? " ").join }
	asAscii {  ^this.collect({|x| {x.asInteger.asAscii}.try ? "" }).join } 
	asDigit {  ^this.collect({|x| {x.asInteger.asDigit}.try ? "" }).join }
	
}

+ String {
	digit { ^Array.fill(this.size, { |i| {this[i].digit}.try }); }
	// ascii { ^Array.fill(this.size, { |i| this[i].ascii }); } //-> remove for 3.3
	asUnicode { ^this.ascii }
}