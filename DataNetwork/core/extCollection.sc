+ Collection{

	selectIndex { | function |
		^this.selectIndexAs(function, this.species);
	}

	selectIndexAs { | function, class |
		var res = class.new(this.size);
		this.do {|elem, i| if (function.value(elem, i)) { res = res.add(i) } }
		^res;
	}
}

/*
detectIndex { | function |
		this.do {|elem, i| if (function.value(elem, i)) { ^i } }
		^nil;
	}
*/