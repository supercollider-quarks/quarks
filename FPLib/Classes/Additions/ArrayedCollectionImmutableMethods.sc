/*
    FP Quark
    Copyright 2012 - 2013 Miguel Negrão.

    FP Quark: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    FP Quark is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with FP Quark.  If not, see <http://www.gnu.org/licenses/>.

    It is possible to add more type instances by adding the functions
    directly to the dict from the initClass function of the class that
    one wants to make an instance of some type class.
*/

+ ArrayedCollection {

    swapI { arg i, j;
        ^this.copy.swap(i,j)
	}

    remoteAtI { arg index;
        ^this.copy.removeAt(index)
    }

    addI { arg item;
        ^this.copy.add(item)
    }

    sortI { |f|
        ^this.copy.sort(f)
    }

    prependI { |item|
        ^[item]++this
    }

    takeN { |n|
        ^this[..(n-1)]
    }

	head {
		^this[0]
	}

	tail {
		^this[1..]
	}

	heads {
		var n = this.size;
		^(n+1).collect{ |i|
			this[..(i-1)]
		}
	}

	tails {
		var n = this.size;
		^(n+1).collect{ |i|
			this[i..]
		}
	}
}



   /*
bench{

//faster
1000.collect{
var x = [1,2,3];
x.copy.add(3);
}
}

bench{
1000.collect{
var x = [1,2,3]++[3];
x
}
}

*/