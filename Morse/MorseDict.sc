		// based on hairi vogel's morse classMorseDict {
	classvar <codes, <strings, <letters;
	*initClass {

		strings = [$., $-, " / "];

		codes =  Event[ 	
			$a -> [0,1],			$b -> [1,0,0,0],			$c -> [1,0,1,0],			$d -> [1,0,0],			$e -> [0],			$f -> [0,0,1,0],			$g -> [1,1,0],			$h -> [0,0,0,0],			$i -> [0,0],			$j -> [0,1,1,1],			$k -> [1,0,1],			$l -> [0,1,0,0],			$m -> [1,1],			$n -> [1,0],			$o -> [1,1,1],			$p -> [0,1,1,0],			$q -> [1,1,0,1],			$r -> [0,1,0],			$s -> [0,0,0],			$t -> [1],			$u -> [0,0,1],			$v -> [0,0,0,1],			$w -> [0,1,1],			$x -> [1,0,0,1],			$y -> [1,0,1,1],			
			$z -> [1,1,0,0],			$0 -> [1,1,1,1,1],			$1 -> [0,1,1,1,1],			$2 -> [0,0,1,1,1],			$3 -> [0,0,0,1,1],			$4 -> [0,0,0,0,1],			$5 -> [0,0,0,0,0],			$6 -> [1,0,0,0,0],			$7 -> [1,1,0,0,0],			$8 -> [1,1,1,0,0],			$9 -> [1,1,1,1,0],
						$. -> [0,1,0,1,0,1],			$, -> [1,1,0,0,1,1],			$: -> [1,1,1,0,0,0],
			$? -> [0,0,1,1,0,0],			$' -> [0,0,1,1,0,0],
			$- -> [1,0,0,0,0,1],
			$/ -> [1,0,0,1,0],  
			
			$( -> [1,0,1,1,0],  
			$) -> [1,0,1,1,0,1],  
			$" -> [0,1,0,0,1,0],  	// inverted commas, quotation marks
			$= -> [1,0,0,0,1],  

			$+ -> [1,0,1,0,1],  	// cross, plus
			$* -> [1,0,0,1],		// multiply, also x
			$@ -> [0,1,1,0,1,0],	// commercial at
			
			'understood' -> [0,0,0,1,0],  
			'error' -> [0,0,0,0,0,0,0,0],  
			'invitationToTransmit' -> [1,0,1],  
			'wait' -> [0,1,0,0,0],  
			'endOfWork' -> [0,0,0,1,0,1],  
			'startingSignal' -> [1,0,1,0,1],  

			$  -> [2]				// stop - is that so? 		];	
		
		letters = ();
		
		codes.keysValuesDo { |key, seq|
			letters.put(this.signs(key).asSymbol, key);
		}	}
	
	*letter { |code| ^letters.at(code.asSymbol) }
		
	 	// single char
	*signs { |char| ^strings[this.at(char)].join }

	 	// a word
	*wordSigns { |word| ^word.as(Array).collect { |char| this.signs(char) ++ " "; }.join }
	 
	*fromAscii { arg code; ^this.at(code) }
	
	*at { arg code; 
		 if (code.isKindOf(Symbol).not) { code = code.asAscii.toLower }; 			^codes.at(code) ? [];	}
	
	*keys { ^codes.keys.asArray.sort }
	
	*postKeys { this.keys.printcsAll.postcs }}
