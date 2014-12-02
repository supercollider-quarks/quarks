// rateNumberToColor.sc - (c) rohan drape, 2004-2007

+ SimpleNumber {
	rateNumberToColor {
		^(0:\yellow, 1:\blue, 2:\black, 3:\red, 4:\green).at(this).asString;
	}
}
