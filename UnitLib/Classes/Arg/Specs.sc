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

ListSpec : Spec {
	var <list;
	var <>defaultIndex = 0; 
	var sortedList, indexMap;
	var <>labels;
	
	// handles only Symbols and Numbers, no repetitions
	
	*new { |list, defaultIndex = 0, labels|
		^super.newCopyArgs( list ? [] ).init.defaultIndex_( defaultIndex ).labels_( labels );
	}
	
	init { 
		var tempList;
		tempList = list.collect({ |item| 
			if( item.isNumber.not ) { 
				item.asSymbol;
			} { 
				item;
			};
		});
		sortedList = tempList.copy.sort;
		indexMap = sortedList.collect({ |item| tempList.indexOfEqual( item ); });
	}
	
	default { ^this.at( defaultIndex ) }
	default_ { |value| defaultIndex = this.unmap( value ); }
	
	at { |index| ^list.at( index ); }
	put { |index, value| list.put( index, value ); this.init; }
	add { |value| list = list.add( value ); this.init; }
	remove { |value| list.remove( value ); this.init; }
	
	list_ { |newList| list = newList; this.init }
	
	constrain { |value|
		^list[ this.unmap( value ) ];
	}
	
	unmap { |value|
		var index;
		index = list.indexOf( value ); // first try direct (faster)
		if( index.notNil ) {
			^index;
		} {
			if( value.isNumber.not ) { value = value.asSymbol; };
			^indexMap[ sortedList.indexIn( value ) ] ? defaultIndex;
		};
	}
	
	map { |value|
		^list[ value.asInt ] ?? { list[ defaultIndex ] };
	}

	storeArgs { ^[list, defaultIndex] }
	
}

ArrayControlSpec : ControlSpec {
	// spec for an array of values
	
	asRangeSpec {  
		^RangeSpec.newFrom( this ).default_( this.default.asCollection.wrapAt([0,1]) );  
	}
	asControlSpec { ^ControlSpec.newFrom( this ).default_( this.default.asCollection[0] ); }
	asArrayControlSpec { ^this }
	
	uconstrain { |value| ^value.collect{ |x| this.constrain(x) } }

	*testObject { |obj| ^obj.isArray && { obj.every(_.isNumber) } }
}

StringSpec : Spec {
	
	var <>default = "";
	
	constrain { |value| ^(value ? default).asString }
	
	*testObject { |obj|
		^obj.isString
	}
	
}

MapSpec : Spec {
	
	var <>default = 'c0';
	
	constrain { |value| ^(value ? default).asSymbol }
	
	*testObject { |obj|
		^obj.class == Symbol
	}
	
	map { |value| ^value }
	unmap { |value| ^value }
	
	
}

AnythingSpec : Spec {
	var <>default;
	
	constrain { |value| ^value }
	
	*testObject { |obj|
		^true
	}
	
	map { |value| ^value }
	unmap { |value| ^value }
}

SMPTESpec : Spec {
	
	var <>minval = 0, <>maxval = inf;
	var <>fps = 1000;
	var <>default = 0;
	
	*new { |minval = 0, maxval = inf, fps = 1000, default = 0|
		^super.newCopyArgs.minval_( minval ).maxval_( maxval ).fps_( fps ).default_( default );
	}
	
	constrain { |value| ^value.clip( minval, maxval ); }

	storeArgs { ^[minval, maxval, fps] }
}

TriggerSpec : Spec {
	var <>label, <>spec;
	
	*new { |label, spec|
		^super.newCopyArgs( label, spec );
	}
	
	default { ^(spec !? _.default) ? 1 } 
	
	map { |value|
		^value;
	}
	
	unmap { |value|
		^value;
	}
	
	constrain { |value|
		^value;
	}
	
	asControlSpec { ^spec ? ControlSpec(0,1,\lin,1,0) }
}

BoolSpec : Spec {
	
	var <default = true;
	var <>trueLabel, <>falseLabel;
	
	*new { |default, trueLabel, falseLabel|
		^super.newCopyArgs( default ? true, trueLabel, falseLabel );
	}
	
	*testObject { |obj|
		^[ True, False ].includes( obj.class );
	}
	
	*newFromObject { |obj|
		^this.new( obj );
	}
	
	map { |value|
		^value.booleanValue;
	}
	
	unmap { |value|
		^value.binaryValue;
	}
	
	constrain { |value|
		if( value.size == 0 ) { 
			^value.booleanValue;
		} {
			^value.mean.booleanValue;
		};
	}
	
	default_ { |value| 
		default = this.constrain( value );
	}
	
	asControlSpec { ^ControlSpec(0,1,\lin,1,default.binaryValue) }

	storeArgs { ^[default, trueLabel, falseLabel] }
}

BoolArraySpec : BoolSpec {
	// spec for an array of boolean values
	
	constrain { |value|
		^value.asArray.collect(_.booleanValue);
	}
	
	map { |value|
		^value.asArray.collect(_.booleanValue);
	}
	
	unmap { |value|
		^value.asArray.collect(_.binaryValue);
	}
}

PointSpec : Spec {
	
	classvar <>defaultMode = \point;
	
	var <rect, <>step, >default, <>units, <>mode; // constrains inside rect
	var clipRect;
	
	// mode can be \point, \polar, \deg_cw, \deg_ccw
	// only for gui; output will always be Point

	*new { |rect, step, default, units, mode|
		^super.newCopyArgs( rect ? inf, (step ? 0).asPoint, default, units ? "", mode ? this.defaultMode ).init;
	}
	
	*testObject { |obj|
		^obj.class == Point;
	}
	
	*newFromObject { |obj|
		var cspecs;
		cspecs = obj.asArray.collect({ |item| ControlSpec.newFromObject( item ) });
		^this.new( Rect.fromPoints(
			(cspecs[0].minval)@(cspecs[1].minval),
			(cspecs[0].maxval)@(cspecs[1].maxval) ),
			(cspecs[0].step)@(cspecs[1].step),
			obj );
	}
	
	init {
		// number becomes radius
		if( rect.isNumber ) { rect = Rect.aboutPoint( 0@0, rect, rect ); };
		rect = rect.asRect;
		clipRect = Rect.fromPoints( rect.leftTop, rect.rightBottom );
	}
	
	default { ^default ?? { clipRect.center.round( step ); } }
	
	minval { ^rect.leftTop }
	maxval { ^rect.rightBottom }
	
	minval_ { |value|
		var x,y;
		#x, y = value.asPoint.asArray;
		rect = Rect.fromPoints( x@y, rect.rightBottom );
		this.init;
	}
	
	maxval_ { |value|
		var x,y;
		#x, y = value.asPoint.asArray;
		rect = Rect.fromPoints( rect.leftTop, x@y );
		this.init;
	}
	
	rect_ { |newRect| rect = newRect; this.init }
	
	asControlSpec {
		^ControlSpec( 
			this.minval.asArray.mean.max( (2**24).neg ), 
			this.maxval.asArray.mean.min( 2**24 ), 
			\lin, 
			0,
			default.asArray.mean, 
			units 
		);
	}
	
	clip { |value|
		^value.clip( clipRect.leftTop, clipRect.rightBottom );
	}
	
	constrain { |value|
		^value.asPoint.clip( clipRect.leftTop, clipRect.rightBottom ); //.round( step );
	}
	
	map { |value|
		^this.constrain( value.asPoint.linlin(0, 1, rect.leftTop, rect.rightBottom, \none ) );
	}
	
	unmap { |value|
		^this.constrain( value.asPoint ).linlin( rect.leftTop, rect.rightBottom, 0, 1, \none );
	}

	storeArgs {
	    ^[rect, step, default, units, mode]
	}
}

CodeSpec : Spec {
	
	var <>default;
	
	*new { |default|
		^super.newCopyArgs( default );
	}
	
	constrain { |value| ^value }

	storeArgs { ^[ default ] }
	
}

UEnvSpec : Spec {
	var <>default;
	var <>spec;
	
	*new { |default, spec|
		^super.newCopyArgs( default ? Env(), spec );
	}
	
	*testObject { |obj|
		^obj.isKindOf( Env );
	}
	
	*newFromObject { |obj|
		^this.new( obj );
	}
	
	map { |value|
		^value;
	}
	
	unmap { |value|
		^value;
	}
	
	constrain { |value|
		^value
	}

	storeArgs { ^[ default, spec] }
	
	asControlSpec { ^spec.asControlSpec }
}

RealVector3DSpec : Spec {

	classvar <>defaultMode = \point;

	var <nrect, <>step, >default, <>units, <>mode; // constrains inside rect

	// mode can be \point, \polar, \deg_cw, \deg_ccw
	// only for gui; output will always be Point

	*new { |nrect, step, default, units, mode|
		^super.newCopyArgs( nrect ? inf, (step ? 0).asRealVector3D, default, units ? "", mode ? \point ).init;
	}

	*testObject { |obj|
		^RealVector.subclasses.includes( obj.class );
	}

	*newFromObject { |obj|
		var cspecs;
		cspecs = obj.as(Array).collect({ |item| ControlSpec.newFromObject( item ) });
		^this.new( NRect(
			cspecs.collect(_.minval).as(RealVector3D),
			cspecs.collect(_.maxval).as(RealVector3D) ),
			cspecs.collect(_.step).as(RealVector3D),
			obj );
	}

	init {
		// number becomes radius
		if( nrect.isNumber ) { nrect = NRect.aboutPoint( 0.asRealVector3D, nrect.asRealVector3D ); };
		nrect = nrect.asNRect;
	}

	default { ^default ?? { nrect.center } }

	minval { ^nrect.origin }
	maxval { ^nrect.endPoint }

	minval_ { |value|
		nrect.origin = value;
	}

	maxval_ { |value|
		nrect.endPoint = value;
	}

	rect_ { |newNRect| nrect = newNRect; this.init }

	clip { |value|
		^nrect.clipVector(value);
	}

	constrain { |value|
		^nrect.clipVector( value.as(RealVector3D).asRealVector3D );
	}

	map { |value|
	    ^nrect.mapVector(value)
	}

	unmap { |value|
		^nrect.unmapVector(value)
	}

	storeArgs {
	    ^[nrect, step, default, units, mode]
	}
}

PolarSpec : Spec {
	
	var <>maxRadius, <step, >default, <>units; // constrains inside rect
	var clipRect;
	
	*new { |maxRadius, step, default, units| 			
		^super.newCopyArgs( maxRadius, (step ? 0), default, units ? "" ).init;
	}
	
	*testObject { |obj|
		^obj.class == Polar;
	}
	
	*newFromObject { |obj|
		var cspec;
		cspec = ControlSpec.newFromObject( obj.rho );
		^this.new( cspec.maxval, cspec.step, obj );
	}
	
	init {
		if( step.class != Polar ) {
			step = Polar( step ? 0, 0 );
		};
	}
	
	default { ^default ?? { clipRect.center.round( step ); } }
	
	step_ { |inStep| step = this.makePolar( inStep ) }
	
	makePolar { |value|
		if( value.class != Polar ) {
			if( value.isArray ) {
				^Polar( *value );
			} {
				^value.asPoint.asPolar;
			};
		} {
			^value.copy;
		};
	}
	
	clipRadius { |value|
		value = this.makePolar( value );
		if( maxRadius.notNil ) {
			value.rho = value.rho.clip2( maxRadius ); // can be negative too
		};
		^value;
	}
	
	roundToStep { |value|
		value = this.makePolar( value );
		value.rho = value.rho.round( step.rho );
		value.theta = value.theta.round( step.theta );
		^value;
	}
	
	scaleRho { |value, amt = 1|
		value = this.makePolar( value );
		value.rho = value.rho * amt;
		^value;
	}
	
	constrain { |value|
		value = this.clipRadius( value );
		^this.roundToStep( value );
	}
	
	map { |value|
		^this.constrain( this.scaleRho( value, maxRadius ? 1 ) );
	}
	
	unmap { |value|
		^this.scaleRho( this.constrain( value ), 1/(maxRadius ? 1));
	}

	storeArgs {
	    ^[maxRadius, step, default, units]
	}
}

RectSpec : Spec {
	
	var <rect, >default, <>units; // constrains inside rect
	var clipRect;
	
	// mode can be \point, \polar, \deg_cw, \deg_ccw
	// only for gui; output will always be Point

	*new { |rect, default, units|
		^super.newCopyArgs( rect ? inf, default, units ? "" ).init;
	}
	
	*testObject { |obj|
		^obj.class == Rect;
	}
	
	*newFromObject { |obj|
		var cspecs;
		cspecs = obj.asArray.collect({ |item| ControlSpec.newFromObject( item ) });
		^this.new( 200 );
	}
	
	init {
		// number becomes radius
		if( rect.isNumber ) { rect = Rect.aboutPoint( 0@0, rect, rect ); };
		rect = rect.asRect;
		clipRect = Rect.fromPoints( rect.leftTop, rect.rightBottom );
	}
	
	default { ^default ?? { Rect.aboutPoint( 0@0, 5, 5 ); } }
	
	minval { ^clipRect.leftTop }
	maxval { ^clipRect.rightBottom }
	
	minval_ { |value|
		var x,y;
		#x, y = value.asPoint.asArray;
		rect.left = x;
		rect.top = y;
		this.init;
	}
	
	maxval_ { |value|
		var x,y;
		#x, y = value.asPoint.asArray;
		rect.right = x;
		rect.top = y;
		this.init;
	}
	
	rect_ { |newRect| rect = newRect; this.init }
	
	clip { |value|
		//^value.clip( clipRect.leftTop, clipRect.rightBottom );
		^value
	}
	
	constrain { |value|
		^value.asRect; //.clip( clipRect.leftTop, clipRect.rightBottom ); //.round( step );
	}
	
	map { |value|
		^this.constrain( value.asRect.linlin(0, 1, rect.leftTop, rect.rightBottom, \none ) );
	}
	
	unmap { |value|
		^this.constrain( value.asRect ).linlin( rect.leftTop, rect.rightBottom, 0, 1, \none );
	}

	storeArgs {
	    ^[rect, default, units]
	}
}

RangeSpec : ControlSpec {
	var <>minRange, <>maxRange;
	var realDefault;
	
	// a range is an Array of two values [a,b], where:
	// a <= b, maxRange >= (b-a) >= minRange
	// the spec is a ControlSpec or possibly a ListSpec with numbers
	
	*new { |minval=0.0, maxval=1.0, minRange=0, maxRange = inf, warp='lin', step=0.0,
			 default, units|
		^super.new( minval, maxval, warp, step, default ? [minval,maxval], units )
			.minRange_( minRange ).maxRange_( maxRange )
	}
	
	*newFrom { arg similar; // can be ControlSpec too
		^this.new(similar.minval, similar.maxval, 
			similar.tryPerform( \minRange ) ? 0,
			similar.tryPerform( \maxRange ) ? inf,
			similar.warp.asSpecifier, 
			similar.step, similar.default, similar.units)
	}
	
	*testObject { |obj|
		^obj.isArray && { (obj.size == 2) && { obj.every(_.isNumber) } };
	}
	
	*newFromObject { |obj|
		var cspecs;
		cspecs = obj.collect({ |item| ControlSpec.newFromObject( item ) });
		^this.new( 
			cspecs.collect(_.minval).minItem, 
			cspecs.collect(_.maxval).maxItem, 
			0, inf, \lin, 
			cspecs.collect(_.step).minItem, 
			obj
			);
	}
	
	
	default_ { |range| realDefault = default = this.constrain( range ); }
	default { ^realDefault ?? 
		{ realDefault = this.constrain( default ? [minval, maxval] ); } } // in case of a bad default
	
	storeArgs { ^[minval, maxval, minRange, maxRange, warp.asSpecifier, step, this.default, units] }
	
	constrain { arg value;
		var array;
		array = value.asArray.copy.sort;
		if( array.size != 2 ) { array = array.extend( 2, array.last ); };
		array = array.collect({ |item| item.asFloat.clip( clipLo, clipHi ); });
		case { (array[1] - array[0]) < minRange } { 
			//"clipped minRange".postln;
			array = array.mean + ( minRange * [-0.5,0.5] );
			case { array[0] < clipLo } {
				array = array + (clipLo-array[0]);
			} {  array[1] > clipHi } {
				array = array + (clipHi-array[1]);
			}; 
		} { (array[1] - array[0]) > maxRange } {
			//"clipped maxRange".postln;
			array = array.mean + ( maxRange * [-0.5,0.5] );
			case { array[0] < clipLo } {
				array = array + (clipLo-array[0]);
			} {  array[1] > clipHi } {
				array = array + (clipHi-array[1]);
			}; 
		};
		^array.round(step); // step may mess up the min/maxrange
	}
	
	uconstrain { |val| ^this.constrain( val ) }
	
	map { arg value;
		// maps a value from [0..1] to spec range
		^this.constrain( warp.map(value) );
	}
	
	unmap { arg value;
		// maps a value from spec range to [0..1]
		^warp.unmap( this.constrain(value) );
	}
	
	asRangeSpec { ^this }
	asControlSpec { ^ControlSpec.newFrom( this ).default_( this.default[0] ); }
	asArrayControlSpec { ^ArrayControlSpec.newFrom( this ); }

}

ColorSpec : Spec {
	
	classvar <>presetManager;

	var >default;
	
	*initClass {
		Class.initClassTree( PresetManager );
		Class.initClassTree( Color );
		presetManager = PresetManager( Color );
		presetManager.presets = Color.web16.getPairs( Color.web16.keys.as(Array).sort );
		presetManager.applyFunc_( { |object, preset|
			 	if( object === Color ) {
				 	preset.deepCopy;
			 	} {	
				 	object.red = preset.red;
				 	object.green = preset.green;
				 	object.blue = preset.blue;
				 	object.alpha = preset.alpha;
				 }
		 	} );
	}

	*new { |default|
		^super.newCopyArgs( default ).init;
	}
	
	*testObject { |obj|
		^obj.class == Color;
	}
	
	*newFromObject { |obj|
		^this.new( obj.asColor );
	}
	
	init {
	}
	
	default { ^default ?? { Color.gray(0.5) } }
	
	clip { |value|
		^value
	}
	
	constrain { |value|
		^value.asColor;
	}

	storeArgs {
	    ^[ default ]
	}

}

RichBufferSpec : Spec {
	
	var <>numChannels = 1; // fixed number of channels
	var <numFrames;

	
	*new { |numChannels = 1, numFrames|
		^super.newCopyArgs( numChannels, numFrames ).init;
	}
	
	*testObject { |obj|
		^obj.class == RichBuffer;
	}
	
	*newFromObject { |obj|
		^this.new( obj.numChannels );
	}
	
	init {
		if( numFrames.isNumber ) { numFrames = [numFrames,numFrames].asSpec }; // single value
		if( numFrames.isNil ) { numFrames = [0,inf,\lin,1,44100].asSpec }; // endless
		numFrames = numFrames.asSpec;
	}
	
	constrain { |value|
		if( value.class == RichBuffer ) {
			value.numFrames = numFrames.constrain( value.numFrames );
			value.numChannels = numChannels.asCollection.first;
			^value;
		} {
			^RichBuffer( numFrames.default, numChannels );
		};
	}
	
	default { 
		^RichBuffer( numFrames.default, numChannels );
	}

	storeArgs { ^[numChannels, numFrames] }
	
}

BufSndFileSpec : RichBufferSpec {
	
	*testObject { |obj|
		^obj.isKindOf( BufSndFile );
	}
	
	constrain { |value|
		value = value.asBufSndFile;
		if( numChannels.notNil ) {
			if( numChannels.asCollection.includes( value.numChannels ).not ) {
				if( numChannels.asCollection.includes( value.useChannels.size ).not ) {
					value.useChannels = (..numChannels.asCollection[0]-1)
						.wrap( 0, value.numChannels );
				};
			};
		};
		^value;
	}
	
	default { 
		^nil.asBufSndFile;
	}
	
	*newFromObject { |obj|
		^this.new( obj.numChannels );
	}
	
}

MonoBufSndFileSpec : BufSndFileSpec {
	
	*testObject { |obj|
		^obj.isKindOf( MonoBufSndFile );
	}
	
	constrain { |value|
		^value.asMonoBufSndFile;
	}
	
	default { 
		^nil.asMonoBufSndFile;
	}
	
	*newFromObject { |obj|
		^this.new();
	}
	
}

DiskSndFileSpec : BufSndFileSpec {
	
	*testObject { |obj|
		^obj.isKindOf( DiskSndFile );
	}
	
	constrain { |value|
		value = value.asDiskSndFile;
		if( numChannels.notNil ) {
			if(  numChannels.asCollection.includes( value.numChannels ).not ) {
				"DiskSndFileSpec - soundfile '%' has an unsupported number of channels (%)"				.format( value.path.basename, value.numChannels )
					.warn;
			};
		};
		^value;
	}
	
	default { 
		^nil.asDiskSndFile;
	}
	
	*newFromObject { |obj|
		^this.new( obj.numChannels );
	}
	
}

MultiSndFileSpec : Spec {
	
	// array of points instead of a single point
	
	var <>default;
	
	*new { |default|
		^super.new.default_( default );
	}
	
	*testObject { |obj|
		^obj.isCollection && { obj[0].isKindOf(AbstractSndFile) };
	}
	
	constrain { |value|
		^value;
	}
	
	*newFromObject { |obj|
		^this.new;
	}
	
}

PartConvBufferSpec : RichBufferSpec {
	
	*new {
		^super.newCopyArgs().init;
	}
	
	*testObject { |obj|
		^obj.isKindOf( PartConvBuffer );
	}
	
	constrain { |value|
		^value.asPartConvBuffer;
	}
	
	default { 
		^nil.asBufSndFile;
	}
	
	*newFromObject { |obj|
		^this.new( obj.numChannels );
	}
	
}

MultiSpec : Spec {
	
	// an ordered and named collection of specs, with the option to re-map to another spec
	
	var <names, <specs, <>defaultSpecIndex = 0;
	var <>toSpec;
	
	*new { |...specNamePairs|
		specNamePairs = specNamePairs.clump(2).flop;
		^super.newCopyArgs( specNamePairs[0], specNamePairs[1] ).init;
	}
	
	init {
		names = names.asCollection.collect(_.asSymbol);
		specs = specs.asCollection;
		specs = names.collect({ |item, i| specs[i].asSpec });
	}
	
	findSpecForName { |name| // name or index
		name = name ? defaultSpecIndex;
		if( name.isNumber.not ) { name = names.indexOf( name.asSymbol ) ? defaultSpecIndex };
		^specs[ name ];
	}
	
	default { |name| // each spec has it's own default
		^this.findSpecForName(name).default;
	}
	
	defaultName { ^names[ defaultSpecIndex ] }
	defaultName_ { |name| defaultSpecIndex = names.indexOf( name.asSymbol ) ? defaultSpecIndex }
	
	defaultSpec { ^specs[ defaultSpecIndex ] }
	
	constrain { |value, name|
		^this.findSpecForName(name).constrain( value );
	}
	
	map { |value, name|
		if( toSpec.notNil ) { value = toSpec.asSpec.unmap( value ) };
		^this.findSpecForName(name).map( value );
	}
	
	unmap { |value, name|
		if( toSpec.notNil ) { value = toSpec.asSpec.map( value ) };
		^this.findSpecForName(name).unmap( value );
	}
	
	mapToDefault { |value, from|
		if( from.isNil ) { value = this.unmap( value, from ); };
		^this.map( value, defaultSpecIndex );
	}
	
	unmapFromDefault { |value, to|
		value = this.unmap( value, defaultSpecIndex );
		if( to.isNil ) { 
			^this.map( value, to ); 
		} {
			^value
		};	
	}
	
	mapFromTo { |value, from, to|
		^this.map( this.unmap( value, from ), to );
	}
	
	unmapFromTo { |value, from, to|
		^this.mapFromTo( value, to, from );
	}
}

IntegerSpec : Spec {

	var <default = 0;
	var <>step = 1;
	var <>alt_step = 1;
	var <>minval = -inf;
	var <>maxval = inf;
	var <>units;
	
	*new{ |default = 0, minval = -inf, maxval = inf|
        ^super.new.minval_( minval ).maxval_( maxval ).default_(default);
	}

	*testObject { |obj|
		^obj.class == Integer;
	}

	constrain { |value|
		^value.clip(minval, maxval).asInteger;
	}
	
	range { ^maxval - minval }
	ratio { ^maxval / minval }

	default_ { |value|
		default = this.constrain( value );
	}
	
	warp { ^LinearWarp( this ) }
	
	floatMinMax {
		^minval.max( (2**24).neg )
	}
	
	map { |value|
		^value.linlin( 0, 1, 
			this.minval.max( (2**24).neg ), 
			maxval.min( 2**24 ), \minmax 
		).round( step ).asInteger;
	}
	
	unmap { |value|
		^value.round(step).linlin( 
			this.minval.max( (2**24).neg ), 
			maxval.min( 2**24 ), 
			0,1, \minmax 
		);
	}
		
	asControlSpec {
		^ControlSpec( this.minval.max( (2**24).neg ), maxval.min( 2**24 ), \lin, 1, default )
	}
	

    storeArgs { ^[default, minval, maxval] }
}

PositiveIntegerSpec : IntegerSpec {
	
	constrain { |value|
		^value.clip(minval.max(0), maxval).asInteger;
	}
	
	minval { ^minval.max(0) }

}

PositiveRealSpec : Spec {

	var <default = 0;

    *new{ |default = 0|
        ^super.new.default_(default)
    }

	constrain { |value|
		^value.max(0);
	}

	default_ { |value|
		default = this.constrain( value );
	}

	storeArgs { ^[default] }
}

FreqSpec : ControlSpec {
	
	classvar <>mode = 'hz'; // \hz, \midi, \note - gui only
	
	*new { arg minval=20, maxval=20000, warp='exp', step=0.0, default = 440, units = " Hz", grid;
		^super.newCopyArgs(minval, maxval, warp, step,
				default ? minval, units ? "", grid
			).init
	}
	
	*initClass {
		specs.put( \freq, FreqSpec() ); // replace default freq spec with this
	}

}

AngleSpec : ControlSpec {
	
	classvar <>mode = 'rad'; // \rad, \deg	
	
	*new { arg minval= -pi, maxval= pi, warp='lin', step=0.0, default = 0, units, grid;
		^super.newCopyArgs(minval, maxval, warp, step,
				default ? minval, units ? "", grid
			).init
	}
}

AngleArraySpec : ArrayControlSpec { }

DisplaySpec : Spec { // a spec for displaying a value that should not be user-edited
	var <>spec;
	var <>formatFunc;
	
	*new { |spec, formatFunc|
		^this.newCopyArgs( (spec ? [0,1]).asSpec, formatFunc ? _.asString );
	}
	
	doesNotUnderstand { |selector ...args|
		var res;
		res = spec.perform( selector, *args );
		if( res != this ) {
			^res;
		};
	}
}

ControlSpecSpec : Spec {
	
	*new {
		^super.newCopyArgs();
	}
	
	*testObject { |obj|
		^obj.isKindOf( ControlSpec );
	}
	
	constrain { |value|
		^value.asControlSpec;
	}
	
	default { 
		^nil.asControlSpec;
	}

}

+ Spec {
	*testObject { ^false }
	
	*forObject { |obj|
		var specClass;
		specClass = [ ControlSpec, RangeSpec, BoolSpec, PointSpec, PolarSpec, 
				BufSndFileSpec, DiskSndFileSpec ]
			.detect({ |item| item.testObject( obj ) });
		if( specClass.notNil ) {
			^specClass.newFromObject( obj );
		} {
			^nil;
		};
	}
	
	*newFromObject { ^this.new }
	
	uconstrain { |...args| ^this.constrain( *args ) }
}


+ ControlSpec { 
	asRangeSpec { ^RangeSpec.newFrom( this ) }
	asControlSpec { ^this }
	asArrayControlSpec { ^ArrayControlSpec.newFrom( this ) }
	
	*testObject { |obj| ^obj.isNumber }
	
	uconstrain { |val|
		if( val.size == 0 ) { 
			^this.constrain( val );
		} {
			^this.constrain( val.mean );
		};
	}
	
	*newFromObject { |obj| // float or int
		var range;
		
		if( obj.isNegative ) {
			range = obj.abs.ceil.asInt.nextPowerOfTwo.max(1) * [-1,1];
		} {
			range = [ 0, obj.ceil.asInt.nextPowerOfTwo.max(1) ];
		};
		
		if( obj.isFloat ) {
			^this.new( range[0], range[1], \lin, 0, obj );
		} {
			^this.new( range[0], range[1], \lin, 1, obj );
		};	
	}
}

+ Nil {
	asRangeSpec { ^RangeSpec.new }
	asControlSpec { ^this.asSpec; }
	asArrayControlSpec { ^this.asSpec.asArrayControlSpec }
}

+ Symbol {
	asRangeSpec { ^this.asSpec.asRangeSpec }
	asControlSpec { ^this.asSpec; }
	asArrayControlSpec { ^this.asSpec.asArrayControlSpec }
}

+ Array {
	asRangeSpec { ^RangeSpec.newFrom( *this ) }
	asControlSpec { ^this.asSpec; }
	asArrayControlSpec { ^ArrayControlSpec( this.minItem, this.maxItem, \lin, 0, this ); }
}