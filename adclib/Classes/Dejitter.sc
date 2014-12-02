/*
*/

Dejitter {
	var <>width, <>func, <value, <hi, <lo;

	*new { |width = 0.02, func, value|
		var res = super.newCopyArgs(width, func);
		if (value.notNil) { res.set(value) };
		^res
	}

	init {
		if (value.notNil) {
			hi = value + (width * 0.5);
			lo = value - (width * 0.5);
			func.value(value);
		}
	}

	set { |val|
		if (value.isNil) {
			value = val;
			this.init;
			^true;
		};
		case (
			{ val > hi }, {
				hi = value = val;
				lo = value - width;
				func.value(value);
				^true
			},
			{ val < lo }, {
				lo = value = val;
				hi = value + width;
				func.value(value);
				^true
		});
		^false
	}
}
