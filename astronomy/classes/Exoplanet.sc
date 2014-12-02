///////////////////////////////////////////////////////////
//
// Andrés Pérez López -- contact [at] andresperezlopez.com
//
// Astronomic Hackathon - Big Bang Data Exposition - CCCB
// Barcelona 11/10/2014
//
// Convenience class for exoplanet sonification
//
///////////////////////////////////////////////////////////

Exoplanet {

	var <>starDistance; // in AU
	var <>ra; // azimuth, in degrees
	var <>dec; // elevation, in degrees

	var <>name;
	var <>starName;
	var <>discoveredIn; //year
	var <>mass; // in Jupiter mass
	var <>orbitalPeriod; // in days
	var <>eccentricity; // 0 to 1
	var <>radius; // in Jupiter radius
	var <>inclination; // in degrees
	var <>molecules; // if found
	var <>detectionMethod;
	var <>temperature; //in ºK

	//generate after
	var <>pos; //[distance,azimuth,elevation]

	*new{
		^super.new.init;
	}

	init {

	}

}

Galaxy {

	var <file;
	var <exoplanets;

	var <massMin,<massMax;
	var <distMin,<distMax;
	var <orbitMin,<orbitMax;

	*new { |filePath,type=\exoplanet|
		^super.new.init(filePath,type);
	}

	init { |filePath,type|

		exoplanets = Dictionary.new;

		//load data
		file = CSVFileReader.read(filePath,true);

		switch (type)

		{\exoplanet} {this.loadExoplanet}
		/// ... add more! ///
		;



	}

	loadExoplanet {
		var list;

		// load

		file.do { |row,i|
			if (i>0) { //skip name declarations
				var e = Exoplanet.new;
				var size = row.size;
				e.name = row[0].asSymbol;
				e.mass = row[1].asFloat;
				e.radius = row[4];
				e.orbitalPeriod = row[7].asFloat;
				e.eccentricity = row[13];
				e.inclination = row[17];
				e.temperature = if (row[38]=="") {row[39]} {row[38]};
				if (e.temperature=="") {e.temperature==nil};
				e.discoveredIn = row[46];
				e.detectionMethod = row[54];
				// e.molecules = row[58];
				e.starName = row[size-17];
				e.ra = row[size-16].asFloat;
				e.dec = row[size-15].asFloat;
				e.starDistance = row[size-9].asFloat;

				//add if we have...
				if (e.starDistance > 0 and:{e.mass > 0 and:{e.orbitalPeriod > 0}}) {
					exoplanets.add(row[0] -> e)
				}
			}
		};

		list=List.new;
		exoplanets.do{|e| list.add(e.starDistance)};
		distMin = list.sort[0]; //1.3
		distMax = list.sort[list.size-1]; //8500

		list=List.new;
		exoplanets.do{|e| list.add(e.mass)};
		massMin = list.sort[0]; //7e-05
		massMax = list.sort[list.size-1]; //32

		list=List.new;
		exoplanets.do{|e| list.add(e.orbitalPeriod)};
		orbitMin = list.sort[0];
		orbitMax = list.sort[list.size-1];
	}

	getExoplanet { |name|
		^exoplanets.at(name.asString)
	}

	getExoplanetNames {
		^exoplanets.keys.asArray;
	}

	getExoplanetsBetween { |min,max|
		var ans = List.new;
		exoplanets.do { |e|
			var dist = e.starDistance;
			if (dist > min and:{dist < max}) {
				ans.add(e.name);
			}
		};
		^ans.asArray;
	}

	getExoplanetsAround { |dist,n|
		// var distanceToPoint = List.new;
		// var distanceToPoint = exoplanets.collect{ |e| abs(dist - e.starDistance)};

		var distancesDictionary = Dictionary.new;
		var distances;

		var keysOrder;
		var distancesOrder;

		var orderedKeys;

		var order;

		exoplanets.do{ |e|
			distancesDictionary.add(e.name -> abs(dist - e.starDistance))
		};

		keysOrder = distancesDictionary.order;
		distancesOrder = distancesDictionary.atAll(keysOrder);

		orderedKeys = keysOrder[distancesOrder.order];

		^orderedKeys[..n-1]

	}

	getExoplanetsWithComposition {
		var ans = List.new;
		exoplanets.do{ |e|
			if (e.molecules!="") {ans.add(e)};
		};
		^ans;
	}

}