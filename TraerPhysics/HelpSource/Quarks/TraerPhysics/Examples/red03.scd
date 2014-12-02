//redFrik 2012

(
var width= 640, height= 360, num= 100;
var win= Window("red03", Rect(100, 100, width, height), false, false);
var usr= UserView(win, Rect(0, 0, width, height));
var physics, anchor, springs;

usr.background= Color.red;
physics= TraerParticleSystem(0.1, 0.2, 0, 0.1);
num.do{|i| physics.makeParticle(0.5, width.rand, height.rand, 0)};
anchor= physics.makeParticle(1, width/2, height/2, 0);
anchor.makeFixed;
springs= physics.particles.do{|p| physics.makeSpring(p, anchor, 0.5.rand, 0.1.rand, 75.rand+1)};
usr.clearOnRefresh= false;
usr.mouseDownAction= {|v, x, y| anchor.position.set(x, y, 0)};
usr.mouseMoveAction= usr.mouseDownAction;
usr.drawFunc= {
	physics.tick;
	Pen.width= 0.1;
	Pen.strokeColor= Color.grey(0, sin(usr.frame%1000/1000*2pi)*0.5+0.5);
	physics.springs.do{|s, i|
		Pen.moveTo(Point(s.getOneEnd.position.x, s.getOneEnd.position.y));
		Pen.lineTo(Point(s.getTheOtherEnd.position.x, s.getTheOtherEnd.position.y));
	};
	Pen.stroke;
};

usr.animate= true;
win.front;
CmdPeriod.doOnce({win.close});
)

//click and drag
//press cmd+period to stop
