
/*
// Influxdefs - named variants

// InfluxBase -> IBdef
a = IBdef(\a, 4); // other args ignored for now.
IBdef(\a) === a
a.postv;
a.set(\x, 0.5);
a.set(\y, 0.2);
a.set(\zzz, 0.5);
a.postv;
a.inValDict;

// InfluxMix -> IMdef
b = IMdef(\a, 3);
IMdef(\a, 2); // other args ignored for now.
IMdef(\a) === b
b.postv;
b.dump
b.influence(\srcA, \x, 0.5).postv;
b.influence(\srcB, \y, 0).postv;
b.influence(\srcC, \x, -0.25, \y, 1, \z, 0.25).postv;


b = Idef(\a, 3, 8);
Idef(\a, 2); // other args ignored for now.
Idef(\a) === a;
Idef(\a) === b;
b.set(\x, 0.5);
b.postv;

d = ISdef(\a, 2);
ISdef(\a, 2); // other args ignored for now.
ISdef(\a) === a
ISdef(\a) === d
d.dump

a = IBdef(\b, 4);
a.key;
a.dump
a.inValDict;
a.postv;


*/

IBdef : InfluxBase {
	classvar <all;
	var <key;

	*initClass { all = (); }

	*at { |key| ^this.all[key] }
	prAdd { |argKey| key = argKey; all.put(key, this) }

	// inNames = outNames
	*new { |key, inNames, inValDict|
		var res = this.at(key);
		if (res.notNil) {
			if ([inNames, inValDict].any(_.notNil)) {
				"% found, ignoring other args.\n".postf(res);
			};
			^res
		} {
			res = super.newCopyArgs(inNames, nil, inValDict).init;
			res.prAdd(key);
		};
		^res
	}

	storeArgs { ^[key] }
}

IMdef : InfluxMix {
	classvar <all;
	var <key;

	*initClass { all = (); }

	*at { |key| ^this.all[key] }
	prAdd { |argKey| key = argKey; all.put(key, this) }

	// inNames = outNames
	*new { |key, inNames, inValDict|
		var res = this.at(key);
		if (res.notNil) {
			if ([inNames, inValDict].any(_.notNil)) {
				"% found, ignoring other args.\n".postf(res);
			};
			^res
		} {
			res = super.newCopyArgs(inNames, nil, inValDict).init;
			res.prAdd(key);
		};
		^res
	}

	storeArgs { ^[key] }
}

ISdef : InfluxSpread {
	classvar <all;
	var <key;

	*initClass { all = (); }

	*at { |key| ^this.all[key] }
	prAdd { |argKey| key = argKey; all.put(key, this) }

	*new { |key, inNames, outNames, inValDict|
		var res = this.at(key);
		if (res.notNil) {
			if ([inNames, outNames, inValDict].any(_.notNil)) {
				"% found, ignoring other args.\n".postf(res);
			};
			^res
		} {
			res = super.newCopyArgs(key, inNames, outNames, inValDict).init;
			res.prAdd(key);
		};
		^res
	}

	storeArgs { ^[key] }
}
Idef : Influx {
	classvar <all;
	var <key;

	*initClass { all = (); }

	*at { |key| ^this.all[key] }
	prAdd { |argKey| key = argKey; all.put(key, this) }

	*new { |key, inNames, outNames, inValDict|
		var res = this.at(key);
		if (res.notNil) {
			if ([inNames, outNames, inValDict].any(_.notNil)) {
				"% found, ignoring other args.\n".postf(res);
			};
			^res
		} {
			res = super.newCopyArgs(inNames, outNames, inValDict).init;
			res.prAdd(key);
		};
		^res
	}

	storeArgs { ^[key] }
}


