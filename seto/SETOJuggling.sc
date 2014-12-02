/**
	A SETObject factory for Juggling
*/
SETOJuggling : SETOmeta {
	*initClass {
		setoClasses = [SETOClub, SETOJugglersHead];
	}
}

/**
	A club is a rigid body and a solid of revolution.
	Therefore it has one distinguished axis which is the symmetry axis (symAxis, z).
	When throwing it, a club flips around an axis perpendicular to the symAxis (flipAxis).

*/
SETOClub : JITseto {
	var <>symAxis, <>lastSymAxis, <>lastTStamp, <>zeroCrossing;
	var <>posRelHead, <>posRelGroundPoint; //, <>rotRelHead;
	classvar <>action;
	
//	<>flipAxis, <>lastFlipAxis;	
	
	var <>flipAngleVel;
	var <>lastPoleOut, lastVz = true, <>vZthresh = 1.17187e-05;
	var <>catched, <>fHist;
	
	*new {|format, id|
		^super.new(format, id).initClub
	}
	initClub {
		lastTStamp = SystemClock.seconds;
		lastSymAxis = [0,0,1];
		fHist = [0,0,0];
		lastPoleOut = 0;
	}
	curvature {|ins| 
		^( ins[0] - (2*ins[1]) + ins[2] )
	}
	onePole {|coef, in| 
		^(lastPoleOut = (((1 - abs(coef)) * in) + (coef * lastPoleOut)));
	}
	process {
		var filteredFlip, vz;
		
		// symmetric axis is determined as z Axis;
		symAxis = this.rotMat.flop.last;
		flipAngleVel = acos((symAxis * lastSymAxis).sum) / (tStamp - lastTStamp).max(0.0001);
		zeroCrossing = symAxis.last.sign != lastSymAxis.last.sign;

		filteredFlip = this.onePole(0.995, pos[2]);
		fHist = fHist.addFirst(filteredFlip).keep(3);

		vz = this.curvature(fHist) > vZthresh;
		catched = (vz && {lastVz.not});

		lastVz = vz;
		lastSymAxis = symAxis;
		lastTStamp = tStamp;
	}
	*newFrom {|obj|
		var out;
		out = super.newFrom(obj);
		((out !== obj) && {obj.isKindOf(this)}).if({
			out.symAxis = obj.symAxis;
			out.lastSsymAxis = obj.lastSymAxis;
			out.flipAngleVel = obj.flipAngleVel; 
			out.posRelHead = obj.posRelHead;
			out.posRelGroundPoint = obj.posRelGroundPoint
		})
		^out;
	}
}


/**
	A Head is a rigid body. (:-)
*/
SETOJugglersHead : JITseto {
	classvar <>regions;
	classvar <>action;
	var <>region = 0;
	var <>regionChanged = false;
	var rotAcc2Ground;
	var homogeneMatAcc2Ground;
	*initClass {
		regions = [
			Rect(0,0,0.5,0.5),
			Rect(0.5,0,0.5,0.5), 
			Rect(0,0.5,0.5,0.5), 
			Rect(0.5,0.5,0.5,0.5)
		];
	}
	process {
		var newRegion = 
			regions.detectIndex{|reg| reg.containsPoint(pos[0..1].asPoint)} ? region;
		regionChanged = (region != newRegion);
		region = newRegion;
	}
	rotAcc2Ground {
		var dat;
		^(rotAcc2Ground ?? {
			// make sure there is a rotMat
			this.rotMat;
			dat = [rotMat[0][0..1],rotMat[1][0..1]];
			dat = dat.collect{|row|
				row * row.squared.sum.sqrt.reciprocal ++ [0]
			};
			rotAcc2Ground = dat ++ [[0,0,1]];
		})
	}
	homogeneMatAcc2Ground {
		var rotMat, invRot, invTrans;

		homogeneMatAcc2Ground.notNil.if({
			^homogeneMatAcc2Ground
		}, {
			invRot = this.rotAcc2Ground.flop;
			invTrans = invRot.collect{|row|
				(row * pos).sum
			};
			^homogeneMatAcc2Ground = [
				invRot[0] ++ invTrans[0].neg,
				invRot[1] ++ invTrans[1].neg,
				invRot[2] ++ invTrans[2].neg,
				[0,0,0, 1]
			];
		});
	}
//////////

	transformPoint2Ground {|point|
		point = point ++ 1;
		
		// make sure homogeneMatAcc2Ground contains a valid transformation matrix
		this.homogeneMatAcc2Ground;
		
		^[
			(point[0] *	homogeneMatAcc2Ground[0][0]) + 
			(point[1] *	homogeneMatAcc2Ground[0][1]) + 
			(point[2] *	homogeneMatAcc2Ground[0][2]) + 
			(			homogeneMatAcc2Ground[0][3]),
			
			(point[0] * 	homogeneMatAcc2Ground[1][0]) + 
			(point[1] * 	homogeneMatAcc2Ground[1][1]) + 
			(point[2] * 	homogeneMatAcc2Ground[1][2]) +
			(		   	homogeneMatAcc2Ground[1][3]),
			
			(point[0] * 	homogeneMatAcc2Ground[2][0]) + 
			(point[1] * 	homogeneMatAcc2Ground[2][1]) + 
			(point[2] * 	homogeneMatAcc2Ground[2][2]) +
			(		   	homogeneMatAcc2Ground[2][3])
		];

	}
	dirty {
		rotAcc2Ground = nil;
		homogeneMatAcc2Ground = nil;
		super.dirty;
	}
	*newFrom {|obj|
		var out;
		out = super.newFrom(obj);
		((out !== obj) && {obj.isKindOf(this)}).if({
			out.region = obj.region;
			out.regionChanged = obj.regionChanged;
			out.rotAcc2Ground = obj.rotAcc2Ground;
		})
		^out;
	}
	
}
