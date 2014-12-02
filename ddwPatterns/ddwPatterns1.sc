/*******

	dewdrop_world Pattern Enhancement quark
	h. james harkins - jamshark70@dewdrop-world.net - http://www.dewdrop-world.net
	
	This program is free software: you can redistribute it and/or modify
	it under the terms of the GNU General Public License as published by
	the Free Software Foundation, either version 3 of the License, or
	(at your option) any later version.

	This program is distributed in the hope that it will be useful,
	but WITHOUT ANY WARRANTY; without even the implied warranty of
	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
	GNU General Public License for more details.

	You should have received a copy of the GNU General Public License
	along with this program.  If not, see <http://www.gnu.org/licenses/

*******/


// NB: If you use the default lo and hi values of -inf .. inf, you must supply a start value
// because rrand(-inf, inf) == nan

Paccum : Pattern {
	var <>lo, <>hi, <>step, <>length, <>start, <>operator;
	*new { arg lo = -inf, hi = inf, step, length=inf, start, operator = '+';
		^super.newCopyArgs(lo, hi, step, length, start, operator)
	}
	storeArgs { ^[lo,hi,step,length,start, operator] }
	embedInStream { arg inval;
			// .value allows you to use a function or a stream as the starting value
		var	streamlo = lo.value,
			streamhi = hi.value,
			cur = start.value(inval) ?? { streamlo rrand: streamhi },
			stepStream = step.asStream,
			opStream = operator.asStream,
			nextStep;
		length.value(inval).do({
			inval = cur.yield;
			(nextStep = stepStream.next(inval)).isNil.if({
				^inval
			}, {
				cur = (cur.perform(opStream.next(inval), nextStep)).fold(streamlo, streamhi);
			});
		});
		^inval;
	}
}

Paccumbounce : Paccum {
	*new { arg lo = -inf, hi = inf, step, length=inf, start;
		^super.newCopyArgs(lo, hi, step, length, start)
	}
	storeArgs { ^[lo,hi,step,length,start] }
	embedInStream { arg inval;
		var	streamlo = lo.value,
			streamhi = hi.value,
			cur = start.value(inval) ?? { streamlo rrand: streamhi },
			stepStream = step.asStream,
			direction = 1, nextStep;
		length.value(inval).do({
			inval = cur.yield;
			(nextStep = stepStream.next(inval)).isNil.if({
				^inval
			}, {
				cur = cur + (nextStep * direction);
				(cur < streamlo or: { cur > streamhi }).if({
					cur = cur.fold(streamlo, streamhi);
					direction = direction.neg;
				});
			});
		});
		^inval;
	}
}

Pvbrown : Paccumbounce {
	embedInStream { arg inval;
		var	streamlo = lo.value,
			streamhi = hi.value,
			cur = start.value(inval) ?? { streamlo rrand: streamhi },
			stepStream = step.asStream,
			nextStep;
		length.value(inval).do({
			inval = cur.yield;
			(nextStep = stepStream.next(inval)).isNil.if({
				^inval
			}, {
				cur = (cur + (nextStep * #[-1, 1].choose)).fold(streamlo, streamhi);
			});
		});
		^inval;
	}
}
