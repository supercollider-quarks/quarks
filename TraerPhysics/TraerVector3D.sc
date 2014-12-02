//Traer v3.0 physics engine, SuperCollider port by redFrik
//Originally created by Jeffrey Traer Bernstein http://murderandcreate.com/physics/

TraerVector3D {
	var <>x;										//Float
	var <>y;										//Float
	var <>z;										//Float
	*new {|x= 0, y= 0, z= 0|
		^super.newCopyArgs(x, y, z);
	}
	setX {|x|
		this.x= x;
	}
	setY {|y|
		this.y= y;
	}
	setZ {|z|
		this.z= z;
	}
	set {|x, y, z|
		this.x= x;
		this.y= y;
		this.z= z;
	}
	add {|p|
		x= x+p.x;
		y= y+p.y;
		z= z+p.z;
	}
	subtract {|p|
		x= x-p.x;
		y= y-p.y;
		z= z-p.z;
	}
	multiplyBy {|f|
		x= x*f;
		y= y*f;
		z= z*f;
	}
	distanceTo {|p|
		^this.distanceSquaredTo(p).sqrt;
	}
	distanceSquaredTo {|p|
		var dx= x-p.x;
		var dy= y-p.y;
		var dz= z-p.z;
		^(dx*dx)+(dy*dy)+(dz*dz);
	}
	dot {|p|
		^(x*p.x)+(y*p.y)+(z*p.z);
	}
	length {
		^((x*x)+(y*y)+(z*z)).sqrt;
	}
	lengthSquared {
		^(x*x)+(y*y)+(z*z);
	}
	clear {
		x= 0;
		y= 0;
		z= 0;
	}
	toString {
		^("(%, %, %)").format(x, y, z);
	}
	cross {|p|
		^TraerVector3D(
			(y*p.z)-(z*p.y),
			(x*p.z)-(z*p.x),
			(x*p.y)-(y*p.x)
		);
	}
	isZero {
		^x==0 and:{y==0 and:{z==0}}
	}
}
