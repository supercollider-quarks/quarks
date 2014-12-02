//redFrik 2012

(
var width= 640, height= 360, maxAge= 200, maxBorneAge= 100;
var win= Window("red05", Rect(100, 100, width, height), false, false);
var usr= UserView(win, Rect(0, 0, width, height));
var physics;

usr.background= Color.red;
usr.clearOnRefresh= false;
physics= TraerParticleSystem(0, 0, 0, 0.4);
usr.mouseDownAction= {|v, x, y|
	var p= physics.makeParticle(2.0.rrand(15), x, y, 0);
	p.age= maxBorneAge.rand;
	p.age.div(10).rand.do{|i|
		var q= physics.particles.choose;
		if(p!=q, {
			physics.makeSpring(p, q, 0.1.rand, 0.25.rand, 10.rand+10);
		});
	};
};
usr.mouseMoveAction= usr.mouseDownAction;
usr.drawFunc= {
	var dead= List.new;
	physics.particles.do{|p, i|
		if(p.age>maxAge, {
			p.dead= true;
			dead.add(p);
		});
	};
	dead.do{|p|
		physics.springs.select{|s| s.a==p or:{s.b==p}}.do{|s| physics.removeSpring(s)};
		physics.removeParticle(p);
	};
	physics.tick;
	Pen.width= 0.25;
	physics.springs.do{|s|
		Pen.strokeColor= Color.grey(1-(s.currentLength/75), 0.2);
		Pen.addOval(Rect.newSides(s.getOneEnd.position.x, s.getOneEnd.position.y, s.getTheOtherEnd.position.x, s.getTheOtherEnd.position.y));
	};
	Pen.stroke;
};

usr.animate= true;
win.front;
CmdPeriod.doOnce({win.close});
)

//click and drag
//press cmd+period to stop
