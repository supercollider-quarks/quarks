+ Object {
	dopostln { this.do( _.postln ); ^"" }  // does not return object!!
	
	dopostcs { this.do( _.postcs ); ^"" }  // does not return object!!
	
	
	dopost { |start = "[ \n", before = "", after = ",\n", end = " ]\n"| 
		
		if( this.size == 0 )
			{ ( start ++ this ++ end ).post; ^"" }
			{	start.post;
				this.do({ |item, i| 
					if(i != 0 )
						{ before.post };
					item.post;
					if(i != (this.size - 1) )
						{ after.post };
					});
				end.post;
				^"";  // does not return object!!
			}
		}
	}
	
+ Dictionary {
	dopostln { this.keysValuesDo({ |key, value| "%: %\n".postf( key, value ) }); ^"" }  // does not return object!!
	}