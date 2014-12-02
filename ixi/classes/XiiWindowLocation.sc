
XiiWindowLocation {

	*new { arg name;
		^super.new.initXiiWindowLocation(name);
		}
		
	initXiiWindowLocation {arg name;
		var point;

		// ARCHIVE CODE - REMEMBER THE WINDOW LOCATION
		// read position from archive.sctxar
		// create dictionary if it doesnt exist
		
		if ( (Archive.global.at(\win_position).isNil), {
			Archive.global.put(\win_position, IdentityDictionary.new);
		});
		
		// add pair if not there already else fetch the info
		if ( (Archive.global.at(\win_position).at(name.asSymbol).isNil), {
			point = Point(200,200);
			Archive.global.at(\win_position).put(name.asSymbol, point);
		}, {
			point = Archive.global.at(\win_position).at(name.asSymbol);
		});
			
		
		^point;
		// END OF ARCHIVE CODE... Thanks blackrain.
	}
	
	*storeLoc {arg name, point;
		Archive.global.at(\win_position).put(name.asSymbol, point);
	}
}