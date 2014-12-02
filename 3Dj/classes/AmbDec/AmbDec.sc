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
// AmbDec.sc
//
// Wrapper for the wonderful AmbDec software
// http://kokkinizita.linuxaudio.org/linuxaudio/
////////////////////////////////////////////////////////////////////////////

AmbDec  {

	*new { |configFile|
		^super.new.init(configFile);
	}

	init { |pathToConfigFile|
		var string = "ambdec -c ";
		var path = pathToConfigFile ? "/usr/share/ambdec/presets/icosahedron-3h3v.ambdec";

		(string ++ path).unixCmd;
	}

}
