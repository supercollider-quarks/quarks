
/*
	streams of scales
	or more commonly known as Chord Progressions
*/
	
	
var scales, pentatonics;

// setting \scale Spec on demand
/*var checkSpec;
checkSpec = Spec.specs.at(\scale);
if(checkSpec.isNil,{
	Spec.specs.put(\scale , ArraySpec.new(StaticSpec(-100,100,\linear)));
},{
	if(checkSpec.isKindOf(ArraySpec).not,{
		("scaleStream Instr trying to init \scale Spec:  \scale is previously defined as a spec other than as an ArraySpec.  this is a conflict").error;
	})
});*/

//scales = CXScale.scalesArray;
/* #[ 
	[ 0, 2, 3, 5, 6, 8, 9, 11 ], 
	[ 0, 2, 4, 6, 8, 10 ], 
	[ 0, 2, 3, 5, 7, 9, 10 ], 
	[ 0, 1, 3, 4, 6, 7, 9, 10 ], 
	[ 0, 2, 3, 5, 7, 8, 10 ], 
	[ 0, 2, 4, 5, 7, 9, 11 ], 
	[ 0, 1, 3, 7, 8 ], 
	[ 0, 1, 5, 6, 10 ], 
	[ 0, 2, 3, 5, 7, 9, 10 ], 
	[ 0, 2, 4, 5, 7, 9, 10 ], 
	[ 0, 2, 3, 5, 7, 9, 10 ], 
	[ 0, 2, 4, 6, 7, 9, 10 ], 
	[ 0, 2, 4, 5, 7, 9, 10 ] ];
*/

pentatonics = 	#[
		[0, 1, 3, 5, 8],
		[0, 1, 3, 5, 9],
		[0, 1, 3, 7, 8],	// Javanese pelog scale
		[0, 1, 4, 5, 8],
		[0, 1, 4, 5, 9],
		[0, 1, 4, 6, 8],
		[0, 1, 4, 6, 9],
		[0, 1, 4, 7, 8],
		[0, 1, 4, 7, 9],	// Scriabin: Sonata No 7
		[0, 1, 4, 7, 10],
		[0, 1, 5, 6, 9],
		[0, 1, 5, 6, 10],	// Japanese Hira-Joshi Scale
		[0, 1, 5, 7, 8],
		[0, 1, 5, 7, 10],
		[0, 1, 5, 7, 11],
		[0, 2, 3, 7, 8],	// 1157
		[0, 2, 3, 7, 9],
		[0, 2, 3, 7, 10],
		[0, 2, 4, 5, 8],
		[0, 2, 4, 7, 8],
		[0, 2, 4, 7, 9],	// 1162
		[0, 2, 4, 8, 11],
		[0, 2, 5, 7, 8],
		[0, 2, 5, 7, 9],
		[0, 2, 5, 8, 10],
		[0, 2, 5, 9, 10],
		[0, 2, 5, 9, 11],	// 1168
		[0, 2, 6, 7, 9],
		[0, 2, 6, 8, 11],
		[0, 2, 6, 9, 10],
		[0, 2, 6, 9, 11],
		[0, 3, 5, 7, 8],
		[0, 3, 5, 7, 9],	// 1174
		[0, 3, 5, 7, 10],
		[0, 3, 5, 7, 11],
		[0, 3, 5, 8, 10],
		[0, 3, 5, 9, 11],
		[0, 3, 7, 8, 10],
		[0, 3, 7, 9, 10],	// 1180
		[0, 4, 5, 7, 8],
		[0, 4, 5, 7, 9],
		[0, 4, 5, 8, 10],
		[0, 4, 5, 8, 11],
		[0, 4, 5, 9, 10],
		[0, 4, 5, 9, 11],	// 1186
		[0, 4, 6, 7, 9],
		[0, 4, 6, 9, 10],
		[0, 4, 7, 9, 10],
		[0, 4, 7, 9, 11]
	];

	
// returns a scale as an array
Instr("scaleStream.scaleAt",{ arg scaleIndex=0,root=0;
	CXScale.scalesArray.at(scaleIndex) + root
},[
	StaticIntegerSpec(0,scales.size - 1),
	StaticIntegerSpec(-100,100)
],\scale);


Instr("scaleStream.randpenta",{ 
	pentatonics.choose
},nil,\scale);

Instr("scaleStream.pentaAt",{ arg index=0;
	pentatonics.at(index)
},[
	StaticIntegerSpec(0,pentatonics.size - 1)
],\scale);



/*
	these return patterns of scales
	ie. each step returns a float array
*/

Instr("scaleStream.randchordprog",{ arg chords=2,notesPerChord=16,root=0;

		Pstutter(notesPerChord,
			Pseq(
				Array.fill(chords,{ arg i;
					CXScale.scalesArray.choose + 12.rand + root
				}) ,inf)
		)
		
},[
	StaticIntegerSpec(2,16),
	StaticIntegerSpec(2,64),
	StaticIntegerSpec(-100,100)
],\scaleStream);

Instr("scaleStream.randpentas",{ arg chords=2,notesPerChord=16,root=0;

		Pstutter(notesPerChord,
			Pseq(
				Array.fill(chords,{ arg i;
					Instr.at("scaleStream.randpenta").value
						 + 12.rand + root
				}) ,inf)
		)
		
},[
	StaticIntegerSpec(2,16),
	StaticIntegerSpec(2,64),
	StaticIntegerSpec(-100,100)
],\scaleStream);

Instr("scaleStream.twopentas",{ arg notesPerChord=16,root1=0,scale1=0,root2=0,scale2=2;

		Pstutter(notesPerChord,
			Pseq([
				Instr.at("scaleStream.pentaAt").value(scale1) + root1,
				Instr.at("scaleStream.pentaAt").value(scale2) + root2
			],inf)
		)
		
},[
	StaticIntegerSpec(2,16),
	StaticIntegerSpec(-100,100),
	StaticIntegerSpec(0,48),
	StaticIntegerSpec(-100,100),
	StaticIntegerSpec(0,48)
],\scaleStream);



// 1 4
Instr("scaleStream.onefour",{ arg notesPerChord=16,root=0;
	var scale;
	scale = 	CXScale.scalesArray.choose;//at(\aeloian);

	Pstutter(notesPerChord,
		Pseq([scale,scale + 4] + root,inf)
	)
},[
	StaticIntegerSpec(2,64),
	StaticIntegerSpec(-100,100)
],\scaleStream);


// 1 4 1 4 1 5 4 1
Instr("scaleStream.bluesprog",{ arg notesPerChord=16,root=0;
	var scale;
	scale = CXScale.scalesArray.choose;//at(\aeloian);

	Pstutter(notesPerChord,
		Pseq(root + [scale,scale + 4, scale, scale + 4, scale, scale + 5, scale + 4, scale],inf)
	)
},[
	StaticIntegerSpec(2,64),
	StaticIntegerSpec(-100,100)
],\scaleStream);



//2 5 1
//coltrane changes
//CXScale.degrees(['bb',	'f#',	'd#',	'b',	'd'])







