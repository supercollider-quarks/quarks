//Random_Arboretum
//Originally created by Jeffrey Traer Bernstein http://murderandcreate.com/physics/

(
var width= 400, height= 400;
var win= Window("Random Arboretum", Rect(100, 100, width, height), false);
var usr= UserView(win, Rect(0, 0, width, height));
var node_size= 10/2;
var edge_length= 20;
var edge_strength= 0.2;
var spacer_strength= 1000;
var physics;
var scale= 1;
var centroidX= 0;
var centroidY= 0;

var initialize, updateCentroid, drawNetwork, addNode, addSpacesToNode, makeEdgeBetween;

usr.background= Color.white;
physics= TraerParticleSystem(gy: 0, drag:0.1);

// Runge-Kutta, the default integrator is stable and snappy,
// but slows down quickly as you add particles.

// Try this to see how Euler is faster, but borderline unstable:
//physics.setIntegrator(\MODIFIED_EULER);

// Now try this to see make it more damped, but stable:
//physics.setDrag(0.2);


usr.drawFunc= {
	physics.tick;
	if(physics.numberOfParticles>1, {
		updateCentroid.value;
	});
	Pen.color= Color.black;
	Pen.width= 2;
	Pen.stringAtPoint(""++physics.numberOfParticles+"PARTICLES\n"++usr.frameRate.round+"FPS", Point(10, 20));
	Pen.stringAtPoint(""++physics.numberOfParticles+"PARTICLES", Point(10, 20));
	Pen.translate(width/2, height/2);
	Pen.scale(scale, scale);
	Pen.translate(0-centroidX, 0-centroidY);
	drawNetwork.value;
};
drawNetwork= {
	
	//draw vertices
	Pen.fillColor= Color.grey(160/255);
	physics.numberOfParticles.do{|i|
		var v= physics.getParticle(i);
		Pen.addOval(Rect.aboutPoint(Point(v.position.x, v.position.y), node_size, node_size));
	};
	Pen.fill;
	
	//draw edges
	physics.numberOfSprings.do{|i|
		var e= physics.getSpring(i);
		var a= e.getOneEnd;
		var b= e.getTheOtherEnd;
		Pen.moveTo(Point(a.position.x, a.position.y));
		Pen.lineTo(Point(b.position.x, b.position.y));
	};
	Pen.stroke;
};
usr.mouseDownAction= {|v, x, y|
	addNode.value;
};
usr.mouseMoveAction= usr.mouseDownAction;
usr.keyDownAction= {|v, chr|
	if(chr==$c, {
		initialize.value;
	});
	if(chr==Char.space, {
		addNode.value;
	});
};
updateCentroid= {
	var xMax= -2147483648;
	var xMin= 2147483647;
	var yMin= 2147483647;
	var yMax= -2147483648;
	var deltaX, deltaY;
	physics.numberOfParticles.do{|i|
		var p= physics.getParticle(i);
		xMax= max(xMax, p.position.x);
		xMin= min(xMin, p.position.x);
		yMin= min(yMin, p.position.y);
		yMax= max(yMax, p.position.y);
	};
	deltaX= xMax-xMin;
	deltaY= yMax-yMin;
	centroidX= xMin+(0.5*deltaX);
	centroidY= yMin+(0.5*deltaY);
	if(deltaY>deltaX, {
		scale= height/(deltaY+50);
	}, {
		scale= width/(deltaX+50);
	});
};
addSpacesToNode= {|p, r|
	physics.numberOfParticles.do{|i|
		var q= physics.getParticle(i);
		if(p!=q and:{p!=r}, {
			physics.makeAttraction(p, q, 0-spacer_strength, 20);
		});
	};
};
makeEdgeBetween= {|a, b|
	physics.makeSpring(a, b, edge_strength, edge_strength, edge_length);
};
initialize= {
	physics.clear;
	physics.makeParticle;
};
initialize.value;
addNode= {
	var p= physics.makeParticle;
	var q= physics.getParticle(physics.numberOfParticles.rand);
	while({q==p}, {
		q= physics.getParticle(physics.numberOfParticles.rand);
	});
	addSpacesToNode.value(p, q);
	makeEdgeBetween.value(p, q);
	p.position.set(q.position.x+rand(-1.0, 1.0), q.position.y+rand(-1.0, 1.0), 0);
};

usr.focus(true);
usr.animate= true;
win.front;
CmdPeriod.doOnce({win.close});
)
