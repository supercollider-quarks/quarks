//Bouncy_Balls
//Originally created by Jeffrey Traer Bernstein http://murderandcreate.com/physics/

(
var width= 400, height= 400;
var win= Window("Bouncy Balls", Rect(100, 100, width, height), false);
var usr= UserView(win, Rect(0, 0, width, height));
var mouse, b, c;
var physics;
var handleBoundaryCollisions;

usr.background= Color.white;
physics= TraerParticleSystem.new;
mouse= physics.makeParticle(1, 0, 0, 0);
mouse.makeFixed;
b= physics.makeParticle(1, width.rand, height.rand, 0);
c= physics.makeParticle(1, width.rand, height.rand, 0);

physics.makeAttraction(mouse, b, 10000, 10);
physics.makeAttraction(mouse, c, 10000, 10);
physics.makeAttraction(b, c, -10000, 5);

usr.mouseDownAction= {|v, x, y|
	mouse.position.set(x, y, 0);
};
usr.mouseMoveAction= usr.mouseDownAction;

usr.drawFunc= {
	handleBoundaryCollisions.value(b);
	handleBoundaryCollisions.value(c);
	physics.tick;
	
	Pen.color= Color.black;
	Pen.strokeOval(Rect.aboutPoint(Point(mouse.position.x, mouse.position.y), 17.5, 17.5));
	Pen.fillOval(Rect.aboutPoint(Point(b.position.x, b.position.y), 17.5, 17.5));
	Pen.fillOval(Rect.aboutPoint(Point(c.position.x, c.position.y), 17.5, 17.5));
};
handleBoundaryCollisions= {|p|
	if(p.position.x<0 or:{p.position.x>width}, {
		p.velocity.set(-0.9*p.velocity.x, p.velocity.y, 0);
	});
	if(p.position.y<0 or:{p.position.y>height}, {
		p.velocity.set(p.velocity.x, -0.9*p.velocity.y, 0);
	});
	p.position.set(p.position.x.clip(0, width), p.position.y.clip(0, height), 0);
};
usr.animate= true;
win.front;
CmdPeriod.doOnce({win.close});
)
