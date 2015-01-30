+ Spec {
	
	adaptToSpec { }
}

+ ControlSpec {
	
	adaptToSpec { |spec|
		var res = this, class = this.class;
		if ( spec.respondsTo(\asControlSpec) ) {
			if( spec.isMemberOf( FreqSpec ) && { class == ControlSpec } ) {
				class = FreqSpec;
			};
			spec = spec.asControlSpec;
			res = class.newFrom( spec );
			res.minval = res.minval.max( (2**24).neg );
			res.maxval = res.maxval.min( 2**24 );
			res.default_( res.map( this.default ) );
		};
		^res;
	}
	
}

+ PointSpec {
	
	adaptToSpec { |spec|
		var res = this, class = this.class;
		if ( spec.respondsTo(\asControlSpec) ) {
			spec = spec.asControlSpec;
			res = this.class.new(1);
			res.minval = spec.minval.asArray.mean.max( (2**24).neg );
			res.maxval = spec.maxval.asArray.mean.min( 2**24 );
			res.default_( res.map( this.unmap( this.default ) ) );
		};
		^res;
	}
	
}

+ UEnvSpec {
	
	adaptToSpec { |spec|
		if( spec.notNil && spec.respondsTo(\asControlSpec) ) {
			^this.copy.spec_( spec.asControlSpec );
		} {
			^this;
		};
	}
	
}

+ DisplaySpec {
	adaptToSpec { |inSpec|
		^this.class.new( spec.adaptToSpec( inSpec ), formatFunc );
	}
}