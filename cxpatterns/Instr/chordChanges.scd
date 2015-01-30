

/*
	chord changes would be an array of arrays
*/

var scales, pentatonics;

/*// setting \scale Spec on demand
var checkSpec;
checkSpec = Spec.specs.at(\scale);
if(checkSpec.isNil, {
	Spec.specs.put(\scale , ArraySpec.new(StaticSpec(-100, 100, \linear)));
}, {
	if(checkSpec.isKindOf(ArraySpec).not, {
		("scaleStream Instr trying to init \scale Spec:  \scale is previously defined as a spec other than as an ArraySpec.  this is a conflict").error;
	})
});*/



pentatonics = 	#[
		[0, 1, 3, 5, 8],
		[0, 1, 3, 5, 9],
		[0, 1, 3, 7, 8], 	// Javanese pelog scale
		[0, 1, 4, 5, 8],
		[0, 1, 4, 5, 9],
		[0, 1, 4, 6, 8],
		[0, 1, 4, 6, 9],
		[0, 1, 4, 7, 8],
		[0, 1, 4, 7, 9], 	// Scriabin: Sonata No 7
		[0, 1, 4, 7, 10],
		[0, 1, 5, 6, 9],
		[0, 1, 5, 6, 10], 	// Japanese Hira-Joshi Scale
		[0, 1, 5, 7, 8],
		[0, 1, 5, 7, 10],
		[0, 1, 5, 7, 11],
		[0, 2, 3, 7, 8], 	// 1157
		[0, 2, 3, 7, 9],
		[0, 2, 3, 7, 10],
		[0, 2, 4, 5, 8],
		[0, 2, 4, 7, 8],
		[0, 2, 4, 7, 9], 	// 1162
		[0, 2, 4, 8, 11],
		[0, 2, 5, 7, 8],
		[0, 2, 5, 7, 9],
		[0, 2, 5, 8, 10],
		[0, 2, 5, 9, 10],
		[0, 2, 5, 9, 11], 	// 1168
		[0, 2, 6, 7, 9],
		[0, 2, 6, 8, 11],
		[0, 2, 6, 9, 10],
		[0, 2, 6, 9, 11],
		[0, 3, 5, 7, 8],
		[0, 3, 5, 7, 9], 	// 1174
		[0, 3, 5, 7, 10],
		[0, 3, 5, 7, 11],
		[0, 3, 5, 8, 10],
		[0, 3, 5, 9, 11],
		[0, 3, 7, 8, 10],
		[0, 3, 7, 9, 10], 	// 1180
		[0, 4, 5, 7, 8],
		[0, 4, 5, 7, 9],
		[0, 4, 5, 8, 10],
		[0, 4, 5, 8, 11],
		[0, 4, 5, 9, 10],
		[0, 4, 5, 9, 11], 	// 1186
		[0, 4, 6, 7, 9],
		[0, 4, 6, 9, 10],
		[0, 4, 7, 9, 10],
		[0, 4, 7, 9, 11]
	];



Instr("chordChanges.blues", {

	var scale;
	scale = CXScale.scalesArray.choose; //scales.choose;
	[1, 4, 1, 4, 1, 5, 4, 1].collect({ arg root;
		scale + root
	})

}, [],
\chordChanges);

