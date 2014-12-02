//Simple_Pendulum
//Originally created by Jeffrey Traer Bernstein http://murderandcreate.com/physics/

(
var width= 400, height= 400;
var win= Window("Simple Pendulum", Rect(100, 100, width, height), false);
var usr= UserView(win, Rect(0, 0, width, height));
var physics, p, anchor, s;

usr.background= Color.white;
physics= TraerParticleSystem(gy:1, drag:0.05);
p= physics.makeParticle(1, width/2, height/2, 0);
anchor= physics.makeParticle(1, width/2, height/2, 0);
anchor.makeFixed;
s= physics.makeSpring(p, anchor, 0.5, 0.1, 75);

usr.mouseDownAction= {|v, x, y|
	p.makeFixed;
	p.position.set(x, y, 0);
};
usr.mouseMoveAction= {|v, x, y|
	p.position.set(x, y, 0);
};
usr.mouseUpAction= {|v, x, y|
	p.makeFree;
};
usr.drawFunc= {
	physics.tick;
	Pen.color= Color.black;
	Pen.line(Point(p.position.x, p.position.y), Point(anchor.position.x, anchor.position.y));
	Pen.stroke;
	Pen.fillOval(Rect.aboutPoint(Point(anchor.position.x, anchor.position.y), 2.5, 2.5));
	Pen.fillOval(Rect.aboutPoint(Point(p.position.x, p.position.y), 10, 10));
};

usr.animate= true;
win.front;
CmdPeriod.doOnce({win.close});
)
