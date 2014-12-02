//cloth
//Originally created by Jeffrey Traer Bernstein http://murderandcreate.com/physics/

(
var width= 400, height= 400;
var win= Window("cloth", Rect(100, 100, width, height), false);
var usr= UserView(win, Rect(0, 0, width, height));
var physics;
var particles;
var gridSize= 10;
var spring_strength= 0.2;
var spring_damping= 0.1;
var gridStepX, gridStepY;
var mp= false, mx= 0, my= 0, px= 0, py= 0;

usr.background= Color.white;
physics= TraerParticleSystem(gy: 0.1, drag:0.01);
particles= Array.fill(gridSize, {Array.newClear(gridSize)});
//physics.integrator.postln;
//physics.setIntegrator(\MODIFIED_EULER);
//physics.integrator.postln;
gridStepX= (width/2)/gridSize;
gridStepY= (height/2)/gridSize;

gridSize.do{|i|
	gridSize.do{|j|
		particles[i][j]= physics.makeParticle(0.2, j*gridStepX+(width/4), i*gridStepY+20, 0);
		if(j>0, {
			physics.makeSpring(particles[i][j-1], particles[i][j], spring_strength, spring_damping, gridStepX);
		});
	};
};
gridSize.do{|j|
	gridSize.do{|i|
		if(i>0, {
			physics.makeSpring(particles[i-1][j], particles[i][j], spring_strength, spring_damping, gridStepY);
		});
	};
};
particles[0][0].makeFixed;
particles[0][gridSize-1].makeFixed;

usr.mouseDownAction= {|v, x, y|
	mp= true;
	mx= x;
	my= y;
};
usr.mouseMoveAction= usr.mouseDownAction;
usr.mouseUpAction= {|v, x, y|
	mp= false;
	mx= x;
	my= y;
	particles[0][gridSize-1].velocity.set(x-px, y-py, 0);
};

usr.drawFunc= {
	physics.tick;
	
	if(mp, {
		particles[0][gridSize-1].position.set(mx, my, 0);
		particles[0][gridSize-1].velocity.clear;
		px= mx;
		py= my;
	});
	
	Pen.color= Color.black;
	gridSize.do{|i|
		Pen.moveTo(Point(particles[i][0].position.x, particles[i][0].position.y));
		gridSize.do{|j|
			//particles[i][j].position.x.postln;
			Pen.lineTo(Point(particles[i][j].position.x, particles[i][j].position.y));
		};
		//?
		Pen.stroke;
	};
	gridSize.do{|j|
		Pen.moveTo(Point(particles[0][j].position.x, particles[0][j].position.y));
		gridSize.do{|i|
			Pen.lineTo(Point(particles[i][j].position.x, particles[i][j].position.y));
		};
		//?
		Pen.stroke;
	};
};
usr.animate= true;
win.front;
CmdPeriod.doOnce({win.close});
)
