////////////////////////////////////////////////////////////////////////////
//
// Copyright (C) Fundació Barcelona Media, October 2014 [www.barcelonamedia.org]
// Author: Andrés Pérez López [contact@andresperezlopez.com]
//
// This program is free software: you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation, either version 3 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; withot even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program. If not, see <http://www.gnu.org/licenses/>
//
////////////////////////////////////////////////////////////////////////////
//
// plusSpherical_SS.sc
//
// Some convenience methods for the classes Spherical and Cartesian from Joseph Anderson
//
////////////////////////////////////////////////////////////////////////////

+ Spherical {
	// convenience methods for creation
	*fromArray { |array|
		^this.new(array@0,array@1,array@2)
	}
	// not to confuse phi and theta due to different nomenclatures!
	azimuth {
		^theta;
	}
	azimuth_ { |angle|
		theta=angle;
	}
	addAzimuth { |angle|
		this.azimuth_(this.azimuth+angle)
	}
	elevation {
		^phi;
	}
	elevation_ { |angle|
		phi=angle;
	}
	addElevation { |angle|
		this.elevation_(this.elevation+angle)
	}
}

+ Cartesian {

	// convenience methods for creation
	*fromArray { |array|
		^this.new(array@0,array@1,array@2)
	}
	// other useful stuff
	any { | function |
		this.asArray.do {|elem, i| if (function.value(elem, i)) { ^true } }
		^false;
	}
}
