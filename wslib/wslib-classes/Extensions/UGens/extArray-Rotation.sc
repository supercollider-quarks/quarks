// wslib 2005
+ Array { // convenience methods for Array with RotateL / RotateN
	rotateN { |n = 0| // no interpolation
		^case {n.rate == 'scalar'}
				{this.rotate(n)}
			{this.rate == 'audio'}
				{ RotateN.ar(n, this) }
			{this.rate == 'control'}
				{ RotateN.kr(n, this) }
			{this.rate == 'scalar'}
			{ if(n.rate == 'control')
				{ RotateN.kr(n, this) }
				{ RotateN.ar(n, K2A.ar(this)) }
			};
		}
		
	rotateL { |n = 0|  // linear interpolation
		^case {n.rate == 'scalar'}
				{ (this.rotate(n.floor.asInt) * (1-n.frac))
				+ (this.rotate(((n.floor+1)%this.size).asInt) * n.frac) }
			{this.rate == 'audio'}
				{ RotateL.ar(n, this) }
			{this.rate == 'control'}
				{ RotateL.kr(n, this) }
			{this.rate == 'scalar'}
			{ if(n.rate == 'control')
				{ RotateL.kr(n, this) }
				{ RotateL.ar(n, K2A.ar(this)) }
			};
		}
	
	rotateS { |n = 0|
		// sinusoid interpolation
		// only for scalars for now
		var sinEnv = Env([0,1],[1],\sine);
		^(this.rotate(n.floor.asInt) * sinEnv[(1-n.frac)])
				+ (this.rotate(((n.floor+1)%this.size).asInt) * sinEnv[(n.frac)]) ;
	}
	
}