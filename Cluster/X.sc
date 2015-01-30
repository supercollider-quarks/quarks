/*
    Cluster Library
    Copyright 2009-2012 Miguel Negrão.

    Cluster Library: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

   Cluster Library is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with Cluster Library.  If not, see <http://www.gnu.org/licenses/>.
*/

// Just use a different class to mark an array as being a ClusterArg
M[slot] : Array {

	asClusterArg {
		^ClusterArg(this.as(Array))
	}

    printOn { arg stream;
		if (stream.atLimit, { ^this });
		stream << "M[ " ;
		this.printItemsOn(stream);
		stream << " ]" ;
	}

}

+ Array {

	asClusterArg { }

}