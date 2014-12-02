+ Date {

	*fromSMPTE { |smpte, daylightSavings = 1|
		var theDay, dayN, monthLib, lastMonth = 1, thisMonth;
		var year, month, day, hour, minute, second, dayOfWeek, rawSeconds, bootSeconds;
		smpte = smpte.asSMPTE;
		theDay = ( (smpte.hours + daylightSavings) / 24).floor;
		
		// this is not entirely right, but seems to function ok
		year = (theDay / 365.242199).floor + 1970;
		dayN = ( ( ( theDay / 365.242199 ).frac * 365.242199 ) 
				// - ((year - 1972) / 4).floor 
			).round(1) + 1;
		// leap year correction ; valid until year 2100!!
		
		monthLib = [ 31, 28, 31, 30, 31, 30, 30, 31, 30, 31, 30, 31];
		if ( (year / 4).frac == 0 )
			{ monthLib[1] = 29 };
			
		monthLib = monthLib.collect({ |item, i| 
			var out = item + ( lastMonth ? 1); lastMonth = out;
			[i+1, out, item] });
			
		thisMonth = monthLib.select({ |item| item[1] > dayN }).first;
		month = thisMonth.first;
		day = (dayN - (thisMonth[1] - thisMonth[2])) + 1;
		hour = (smpte.hours + daylightSavings) % 24;
		minute = smpte.minutes;
		second = smpte.seconds;
		rawSeconds = smpte.asSeconds;
		dayOfWeek = ( (theDay + 4) % 7 );
		^super.newCopyArgs(year, month, day, hour, minute, second, dayOfWeek, 
				rawSeconds);
		}
				
	*fromRawSeconds { |rawSeconds = 0, daylightSavings = 1|
		// starts at 1/1/1970
		^Date.fromSMPTE( SMPTE( rawSeconds ), daylightSavings );
		}	
	
	asSMPTE { |fps| ^SMPTE( rawSeconds, fps ); }
		
	}
		
