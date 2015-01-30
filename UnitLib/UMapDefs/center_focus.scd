/*
\center_focus

Creates an UMap intended for use on modulatable range parameters. It converts the range into three linear controls: center, focus and knee. If the center value is at minimum or maximum, the low and high values of the range will be the same. The focus parameter controls the behaviour of the range in between. If focus == 1 (maximum), the range values will be equal to center. If focus == 0, the range will be [min,max] when the center value is halfway in between. The knee parameter controls the smoothness of the transition via sine mapping.

center -> range

with focus == 0;
0      -> [ 0,    0    ]
0.25   -> [ 0,    0.5  ]
0.5    -> [ 0,    1    ]
0.75   -> [ 0.5,  1    ]
1      -> [ 1,    1    ]

with focus == 0.5;
0      -> [ 0,    0    ]
0.25   -> [ 0,    1/3  ]
0.5    -> [ 1/3,  2/3  ]
0.75   -> [ 2/3,  1    ]
1      -> [ 1,    1    ]

with focus == 1; ( same as \center_range with range == 0)
0      -> [ 0,    0    ]
0.25   -> [ 0.25, 0.25 ]
0.5    -> [ 0.5,  0.5  ]
0.75   -> [ 0.75, 0.75 ]
1      -> [ 1,    1    ]

center: position above or below the center (-1 - 1)
focus: the width of the range (0-1)
knee: transition smoothness at point where lo or hi value starts changing

The 'center' arg range is mapped to that of the parameter to which the UMap is connected.

--
this is an UMapDef definition file
part of the Unit lib default UMapDefs set
*/

UMapDef( \center_focus, { |center = 0.5, focus = 0.0, knee = 0.0|
	var lo, hi;
	knee = knee * (1-focus);
	lo = center.linlin( focus.linlin(0,1,0.5,0),1,-1,0,\none).sineclip2(1,knee) + 1;
	hi = center.linlin( 0, focus.linlin(0,1,0.5,1),0,1,\none).sineclip2(1,knee);
	UMapOut.kr([lo,hi]);
})
	.mappedArgs_([\center])
	.category_( 'range' );
