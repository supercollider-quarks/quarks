+ Number {

	celcius2kelvin { ^this + 273.15; }
	kelvin2celcius { ^this - 273.15; }
		
	kelvin2farenheit { ^(this * (9/5)) - 459.67; }
	farenheit2kelvin { ^(this + 459.67) * (5/9); }
	
	celcius2farenheit { ^(this * (9/5)) + 32; }
	farenheit2celcius { ^(this - 32) * (5/9); }
		
	*speedOfSound { |temp = 15, substance = \air| // temp in celcius, output: m/s
	
		// this formula neglects humidity and air pressure, which are said to have 
		// only little effect on the speed of sound. This means that the outcome
		// of the formula is not as precise as the 64-bit float value it produces;
		// it is only an approximation. However, it does deal with the most important parameter:
		// the temperature.
		
		var r = 8.314472; // universal gas constant (J/mol K)
		var m; // molecular weight of the gas in kg/mol
		var y; // adiabatic constant, characteristic of the specific gas
		
		#m, y = ( air: [ 28.95 / 1000, 1.41 ], // dry air (0%humidity)
			   	 helium: [ 4/1000, 5/3 ]
			)[ substance ];
		
		^(( y * r * temp.celcius2kelvin ) / m).sqrt; // ideal gas formula
		
		
		}
	}