//lijon 2011

// TODO:
// *sphere, *pyramid, etc..?
// morphing?

/*
for(float theta=0;theta < PI ;theta = theta + (PI/meridians)){
   for(float phi=0;phi < 2*PI;phi= phi + (2*PI/parallels)){
         verts[index].x = cos(theta) * sin(phi);
         verts[index].y = sin(theta) * sin(phi);
         verts[index].z = cos(phi);
   }
}
*/

Canvas3DItem {
    var <>paths;
    var <>color;
    var <>width;
    var <>transforms;
    var <>fill = false;

    *new {
        ^super.new.init;
    }

    *cube {
        ^this.new.paths = #[
            [[-1,-1,-1],[1,-1,-1],[1, 1,-1],[-1, 1,-1],[-1,-1,-1]],
            [[-1,-1,-1],[-1,-1,1]],[[ 1,-1,-1],[ 1,-1,1]],[[-1, 1,-1],[-1, 1,1]],[[ 1, 1,-1],[ 1, 1,1]],
            [[-1,-1, 1],[1,-1, 1],[1, 1, 1],[-1, 1, 1],[-1,-1, 1]]
        ];
    }

    *grid {|n|
        var i=(n-1)/2;
        ^this.new.paths_(n.collect {|x| x=x/i-1; [[-1,x,0],[1,x,0]]} ++ n.collect {|x| x=x/i-1; [[x,-1,0],[x,1,0]]})
    }

    // added TH
    *regPrism { |sides=3|
        var twosqrt = 2.sqrt;
        var step = pi/(sides/2);
        var a = twosqrt.neg/2, b = twosqrt/2;
        var path = [
            // top and bottom
            (sides+1).collect {|i| [ sin(step*i), a, cos(step*i)]},
            (sides+1).collect {|i| [ sin(step*i), b, cos(step*i)]}
        ]
        ++
        // sides
        sides.collect{|i|
	        var c = sin(step*i), d = cos(step*i), e = sin(step*(i+1)), f = cos(step*(i+1));
            [
                [ c, a, d],
                [ c, b, d],
                [ e, b, f],
                [ e, a, f],
                [ c, a, d]
            ]
        };
        ^this.new.paths_(path).transform(Canvas3D.mScale(twosqrt, twosqrt, twosqrt))
    }

    init {
        paths = [];
        width = 1;
        color = Color.black;
        transforms = [];
    }

    transform {|matrix|
        paths = paths.collect {|p| p.collect {|v| Canvas3D.vectorMatrixMul(v, matrix) }};
    }
}

Canvas3D : SCViewHolder {
    var <>items;
    var <>scale = 200;
    var <>perspective = 0.5;
    var <>distance = 2;
    var <>transforms;
    var <>preDrawFunc;
    var <>postDrawFunc;
    var animator;

    *new { |parent, bounds|
        ^super.new.init(parent, bounds);
    }

    init { |parent, bounds|
        items = [];
        transforms = [];
        this.view = UserView(parent, bounds)
            .background_(Color.white)
            .drawFunc_({
                var penClass= try{if(\GUI.asClass.notNil, {GUI.pen})}?Pen;//avoids gui redirect
                preDrawFunc.value;
                items.do {|item|
                    penClass.width = item.width;
                    item.paths.do {|path|
                        path.do {|v,i|
			                var x, y, z, p;
			                item.transforms.do {|m| v = Canvas3D.vectorMatrixMul(v, m) };
			                transforms.do {|m| v = Canvas3D.vectorMatrixMul(v, m) };
			                z = v[2]*perspective+distance;
			                x = scale*(v[0]/z)+(this.bounds.width/2);
			                y = scale*(v[1]/z)+(this.bounds.height/2);
                            p = Point(x, y);
                            if(i==0) {
                                penClass.moveTo(p);
                            } {
                    			penClass.lineTo(p);
                    		};
                        };
                        if(item.fill, {
                            penClass.fillColor = item.color;
                            penClass.fill;
                        }, {
                            penClass.strokeColor = item.color;
                            penClass.stroke;
                        });
                    };
                };
                postDrawFunc.value;
            });
    }

    add {|item|
        items = items.add(item);
    }

    remove {|item|
        items.remove(item);
    }

    animate {|rate, func|
        animator.stop;
        if(rate.notNil and: {func.notNil}) {
            animator = Routine {
                var frame = 0;
                while {this.view.notNil and: {animator.notNil}} {
                    func.value(frame);
                    this.refresh;
                    (1/rate).wait;
                    frame = frame + 1;
                }
            }.play(AppClock);
        } {
            animator = nil;
        }
    }

    //matrix stuff by redFrik 050703
	*mIdentity {
		^[	#[1, 0, 0, 0],
			#[0, 1, 0, 0],
			#[0, 0, 1, 0],
			#[0, 0, 0, 1]];
	}
	*mTranslate {|tx, ty, tz|
		^[	#[1, 0, 0, 0],
			#[0, 1, 0, 0],
			#[0, 0, 1, 0],
			[tx, ty, tz, 1]];
	}
	*mScale {|sX, sY = (sX), sZ = (sX)|
		^[	[sX, 0, 0, 0],
			[0, sY, 0, 0],
			[0, 0, sZ, 0],
			#[0, 0, 0, 1]];
	}
	*mRotateX {|ax|
		^[	#[1, 0, 0, 0],
			[0, cos(ax), sin(ax), 0],
			[0, sin(ax).neg, cos(ax), 0],
			#[0, 0, 0, 1]];
	}
	*mRotateY {|ay|
		^[	[cos(ay), 0, sin(ay).neg, 0],
			#[0, 1, 0, 0],
			[sin(ay), 0, cos(ay), 0],
			#[0, 0, 0, 1]];
	}
	*mRotateZ {|az|
		^[	[cos(az), sin(az), 0, 0],
			[sin(az).neg, cos(az), 0, 0],
			#[0, 0, 1, 0],
			#[0, 0, 0, 1]];
	}

	*matrixMatrixMul {|matrix1, matrix2|
		var m0, m1, m2, m3;
		#m0, m1, m2, m3= matrix2;
		^Array.fill(4, {|x|
			Array.fill(4, {|y|
				(matrix1[x][0]*m0[y])+
				(matrix1[x][1]*m1[y])+
				(matrix1[x][2]*m2[y])+
				(matrix1[x][3]*m3[y])
			});
		});
	}
	*vectorMatrixMul {|vector, matrix|
		var v0, v1, v2, m0, m1, m2, m3;
		#v0, v1, v2= vector;
		#m0, m1, m2, m3= matrix;
		^[
			(v0*m0[0])+(v1*m1[0])+(v2*m2[0])+m3[0],
			(v0*m0[1])+(v1*m1[1])+(v2*m2[1])+m3[1],
			(v0*m0[2])+(v1*m1[2])+(v2*m2[2])+m3[2]
		];
	}
}

