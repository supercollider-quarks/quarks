//redFrik 2012

(
var width= 640, height= 360, maxAge= 200, maxBorneAge= 100;
var win= Window("red04", Rect(100, 100, width, height), false, false);
var usr= UserView(win, Rect(0, 0, width, height));
var physics;

usr.background= Color.red;
physics= TraerParticleSystem(0, 0, 0, 0.1);
usr.mouseDownAction= {|v, x, y|
	var p= physics.makeParticle(1.0.rrand(20), x, y, 0);
	p.age= maxBorneAge.rand;
	p.age.div(10).rand.do{|i|
		var q= physics.particles.choose;
		if(p!=q, {
			physics.makeSpring(p, q, 0.1.rand, 0.25.rand, 100.rand+10);
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
	Pen.fillColor= Color.green(0.6, 0.3);
	physics.particles.do{|p|
		Pen.addOval(Rect.aboutPoint(Point(p.position.x, p.position.y), p.mass*(1-(p.age/maxAge)), p.mass*(1-(p.age/maxAge))));
	};
	Pen.fill;
};

usr.animate= true;
win.front;
CmdPeriod.doOnce({win.close});
)

//click and drag
//press cmd+period to stop
