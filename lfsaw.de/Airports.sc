/*
This class relies on a Database of airports kindly provided by 
	http://www.partow.net/miscellaneous/airportdatabase/index.html
under the cpl.
For convenience reasons it is included in this release.
Free use of The Global Airport Database are permitted under the guidelines and in accordance with the most current version of the "Common Public License."
See e.g. 
	http://www.opensource.org/licenses/cpl1.0.php 
for details.


Field 00 - ICAO Code			4 character ICAO code
Field 01 - IATA Code			3 character IATA code
Field 02 - Airport Name			String of varying length
Field 03 - City,Town or Suburb	String of varying length
Field 04 - Country				String of varying length
Field 05 - Latitude Degrees		2 ASCII characters representing one numeric value
Field 06 - Latitude Minutes		2 ASCII characters representing one numeric value
Field 07 - Latitude Seconds		2 ASCII characters representing one numeric value
Field 08 - Latitude Direction	1 ASCII character either N or S representing compass direction
Field 09 - Longitude Degrees	2 ASCII characters representing one numeric value
Field 10 - Longitude Minutes	2 ASCII characters representing one numeric value
Field 11 - Longitude Seconds	2 ASCII characters representing one numeric value
Field 12 - Longitude Direction	1 ASCII character either E or W representing compass direction
Field 13 - Altitude	 		Varying sequence of ASCII characters representing a numeric value corresponding to the airport's altitude from mean sea level (ie	 "123" or "-123")*/

Airport {
	/* Fix: azimuthFrom, distanceFrom -- inefficient */
	
	var <icao, <iata, <name, <city, <country, <latDeg, <latMin, <latSec, <latDir, <longDeg, <longMin, <longSec, <longDir, <alt;
	
	*new{|icao, iata, name, city, country, latDeg, latMin, latSec, latDir, longDeg, longMin, longSec, longDir, alt|
		^super.new.initAirport(icao, iata, name, city, country, latDeg, latMin, latSec, latDir, longDeg, longMin, longSec, longDir, alt)
	}
	
	*fromArray{|array|
		^this.new(*array);
	}
	
	initAirport {|argIcao, argIata, argName, argCity, argCountry, argLatDeg, argLatMin, argLatSec, argLatDir, argLongDeg, argLongMin, argLongSec, argLongDir, argAlt|
	
		icao    = argIcao   ; 
		iata    = argIata   ; 
		name    = argName   ; 
		city    = argCity   ; 
		country = argCountry; 
		latDeg  = argLatDeg ; 
		latMin  = argLatMin ; 
		latSec  = argLatSec ; 
		latDir  = argLatDir ; 
		longDeg = argLongDeg; 
		longMin = argLongMin; 
		longSec = argLongSec; 
		longDir = argLongDir; 
		alt     = argAlt    ;
	}
	coordinate {
		^Point(latDeg, longDeg);
	}
	id {
		^icao;
	}
	printOn { | stream |
		stream << this.class.name << "(" << this.icao << ")" ;
	}
	azimuthFrom{|point|
		var distance, orient;
		#distance, orient = (this.coordinate-point).asPolar.asArray;
		^orient;
	}
	distanceFrom{|point|
		var distance, orient;
		#distance, orient = (this.coordinate-point).asPolar.asArray;
		^distance;
	}

}

Airports {
	classvar <>path;
	classvar data;
	classvar <keys;

	*initClass {
		path = "%/GlobalAirportDatabase/GlobalAirportDatabase.txt".format(this.filenameSymbol.asString.dirname);
		// path = "/Users/tboverma/Library/Application Support/SuperCollider/quarks-bi/ChopStix/dataAcquisition/Wunderground-Weather/GlobalAirportDatabase/GlobalAirportDatabase.txt";
	}
	
	*load {
		data = FileReader.read(path, delimiter: $:);
		data = data.collect{|row| 
			Airport.fromArray(
				row[0..4].collect{|val| (val != "N/A").if({val.asSymbol}, {nil})} ++ 
				row[5..7].collect(_.interpret) ++
				row[8].asSymbol ++
				row[9..11].collect(_.interpret) ++
				row[12].asSymbol ++
				row[13].interpret
			)
		};
		
	}
	
/*	query {|ICAO, IATA, name, city, country, latDeg, latMin, latSec, latDir, longDeg, longMin, longSec, longDir, alt|
		data.select{}
	}
*/

	*data {
		data.isNil.if{
			this.load
		};
		^data;
	}
	*collect {|function|
		^this.data.collect(function);
	}
	*select {|function|
		^this.data.select(function);
	}
	*at{|idx|
		^this.data.at(idx)
	}
	*inCountry {|country = \GERMANY|
		country = country.asArray;
		^this.data.select{|airport| country.includes(airport.country)}
	}
	*countries {
		^this.data.collectAs({|airport| airport.country}, Set)
	}
	*inArea{|latRange([-90, 90]), longRange([-180, 180])|
		var rect = Rect.fromPoints(
			Point(latRange[0], longRange[0]), 
			Point(latRange[1], longRange[1]));
		
		rect.postln;
		^this.data.select{|airport|
			rect.containsPoint(airport.coordinate)
		}
	}
}