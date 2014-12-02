/*
Interaction Class of the server-side implementation of SETO 
	http://tuio.lfsaw.de/
	http://modin.yuri.at/publications/tuio_gw2005.pdf

Author: 
	2004, 2005, 2006, 2007
	Till Bovermann 
	Neuroinformatics Group 
	Faculty of Technology 
	Bielefeld University
	Germany
*/

/*
	Change
		2007-10-29	renamed to SETOServer
*/

SETOInteraction {
	var <parts;
	// abstract class
	*new{|... parts| ^super.new.initInt(parts)}
	initInt{|argParts| parts = argParts}
	update {
//		parts.every(_.visible).if{
			this.interaction(parts.any{|item| item.visible.not}.not);
//		}
	}
	interaction {|isValid|
		postf("SETOInteraction: % <-> %\n", *parts.collect(_.id));
	}
}

SETOIDistance : SETOInteraction {
	classvar <>distFunc;
	interaction {|isValid|
		var posA, posB, distance;
		
		posA = parts[0].pos[0..2].reject(_.isNil);
		posB = parts[1].pos[0..2].reject(_.isNil);
		
		distance = posA.collect{|val, i| (val - posB[i]).squared}.sum.sqrt;
		this.class.distFunc.value(distance, isValid, parts);
	}
}