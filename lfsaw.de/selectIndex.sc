+ Collection {
	selectIndex { | function |
		^this.selectIndexAs(function, this.species);
	}

	selectIndexAs { | function, class |
		var res = class.new(this.size);
		this.do {|elem, i| if (function.value(elem, i)) { res.add(i) } }
		^res;
	}

}