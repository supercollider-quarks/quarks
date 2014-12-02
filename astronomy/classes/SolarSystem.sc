///////////////////////////////////////////////////////////
//
// Andrés Pérez López -- contact [at] andresperezlopez.com
//
// Astronomic Hackathon - Big Bang Data Exposition - CCCB
// Barcelona 11/10/2014
//
// Convenience class for solar system sonification
//
///////////////////////////////////////////////////////////

SolarSystem {

	var <planets;

	*new {
		^super.new.init;
	}

	init {
		planets = Dictionary.new;
		planets.add(\mercury -> Planet.mercury);
		planets.add(\venus -> Planet.venus);
		planets.add(\earth -> Planet.earth);
		planets.add(\mars -> Planet.mars);
		planets.add(\jupiter -> Planet.jupiter);
		planets.add(\saturn -> Planet.saturn);
		planets.add(\uranus -> Planet.uranus);
		planets.add(\neptune -> Planet.neptune);
	}

}

Planet {

	var <>name;
	var <>perihelion; // in AU
	var <>mass; // in Earth mass
	var <>radius; // mean,in Earth radius
	var <>orbitalPeriod; // in Julian years
	var <>rotationPeriod; // in earth days
	var <>temperature; // mean equatorial temperature, in K
	var <>surfaceGravity; // in g

	*new {
		^super.new;
	}

	// data from wikipedia.org

	*mercury {
		^super.newCopyArgs(\mercury,0.307499,0.055,0.3829,0.240846,58.646,340,0.38)
	}

	*venus {
		^super.newCopyArgs(\venus,0.718440,0.815,0.9499,0.615198,−243.0185,737,0.904)
	}

	*earth {
		^super.newCopyArgs(\earth,0.98329134,1,1,1,1,288,1)
	}

	*mars {
		^super.newCopyArgs(\mars,1.3814,0.107,0.53,1.8808,1.025957,210,0.376)
	}

	*jupiter {
		^super.newCopyArgs(\jupiter,4.950429,317.8,11,11.8618,0.41354167,165,2.528)
	}

	*saturn {
		^super.newCopyArgs(\saturn,9.04807635,95.152,9,29.4571,0.44041,134,1.065)
	}

	*uranus {
		^super.newCopyArgs(\uranus,18.283135,14.536,3.968,84.016846,0.7183,76,0.886)
	}

	*neptune {
		^super.newCopyArgs(\neptune,29.809946,17.147,3.856,164.8,0.6713,72,1.14)
	}

}