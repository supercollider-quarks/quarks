/*
d = Debounce({ "on!".postln; }, { "off...".postln }, 5);
d.set(1).lastN;
d.set(0).lastN;
d.size = 8;
d.size = 5;
*/

Debounce {
	var <>onFunc, <>offFunc, <>size, <lastN, <sum, <>isOn = false;

	*new { |onFunc, offFunc, size = 5|
		^super.newCopyArgs(onFunc, offFunc, size,  []);
	}

	set { |val|
		lastN = lastN.addFirst(val).keep(size);
		sum = lastN.sum;
		this.check;
	}

	check {
		if ((sum == size) and: { isOn.not }) {
			onFunc.value;
			isOn = true;
		} {
			if ((sum == 0) and: { isOn }) {
				offFunc.value;
				isOn = false;
			}
		}
	}
}
		