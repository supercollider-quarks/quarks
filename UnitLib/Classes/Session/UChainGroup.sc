/*
    Unit Library
    The Game Of Life Foundation. http://gameoflife.nl
    Copyright 2006-2011 Miguel Negrao, Wouter Snoei.

    GameOfLife Unit Library: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    GameOfLife Unit Library is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with GameOfLife Unit Library.  If not, see <http://www.gnu.org/licenses/>.
*/

UChainGroup : UEvent {
    var <chains;

    *new { |...chains|
        ^super.new.init(chains)
    }

    init { |inChains|
        chains = inChains;
    }
    
    at { |index| ^chains[ index ] }
    copySeries { |first, second, last| ^chains.copySeries( first, second, last ) }
	collect { |func|  ^chains.collect( func );  }
	do { |func| chains.do( func ); }
	last { ^chains.last }
	first { ^chains.first }
	indexOf { |obj| ^chains.indexOf( obj ); }

    name { ^chains.collect(_.name).asString }

    waitTime { ^chains.collect(_.waitTime).sum }

    prepare { |target, startPos = 0, action|
        chains.do(_.prepare(target, startPos, action))
    }

    prepareAndStart{ |target, startPos = 0|
        chains.do(_.prepareAndStart(target, startPos))
    }

    prepareWaitAndStart { |target, startPos = 0|
        chains.do(_.prepareWaitAndStart(target, startPos))
    }

    start { |target, startPos, latency|
        chains.do(_.start(target, startPos, latency))
    }

    release { |time|
        chains.do(_.release(time))
    }

    dispose { chains.do(_.dispose) }

    dur { ^chains.collect(_.duration).maxItem }

    dur_ { |dur| chains.do(_.duration_(dur)) }

    groups { ^chains.collect(_.groups).flat }

    gui { |parent, bounds, score| ^MassEditUChain(chains).gui( parent, bounds, score ) }
    
    storeArgs { ^chains }
}