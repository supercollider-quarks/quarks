// this file is part of redUniverse toolkit /redFrik


RedSpring {
	var	<>object, <>target,						//objects
		<>stiffness, <>damping, <>length;			//floats
	*new {|object, target, stiffness, damping, length|
		^super.newCopyArgs(
			object,
			target,
			stiffness ? 0.1,
			damping ? 0.9,
			length ? 0
		)
	}
	update {
		object.spring(target, stiffness, damping, length);
	}
	update2 {		//convenience method for interconnecting two objects with one spring
		object.spring(target, stiffness, damping, length);
		target.spring(object, stiffness, damping, length);
	}
}
