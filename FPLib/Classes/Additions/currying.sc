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

+ Function {

	//partial application with unknown number of arguments
	pa { |arg1| ^{ arg ...args; this.value(arg1,*args) } }
	curried1 { ^{ |x| this.pa(x) } }
	curried2 { ^{ |x| { |y| this.pa(x).(y) } } }
	curried3 { ^{ |x| { |y| {|z| this.pa(x).pa(y).(z) } } } }
	curried {
		var r = { |f,n|
			var result;
			if(n == 1){
				{ |x| f.(x) }
			} {
				{|x|	r.value(f.pa(x),n-1) }
			}
		};
		^r.(this,this.def.argNames.size)
	}

}
