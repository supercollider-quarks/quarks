//2 parents mate and have n children. each child as a boid.  always the same 2 parents.
//first breed children - genomes are translated to synthesis networks
//children are kept in global variable c
//with mouse control over first child's target
//req. the classes in redGA060517 from fredrikolofsson.com

//--
//breed children
(
n= 6;											//number of children
s.waitForBoot{
	var parent0, parent1;
	g= RedGAGenome([
		0.9.rrand(1), 0, 0, 1,
		[0.679.rrand(0.74), 0.99, 1, 0.679].wchoose([0.7, 0.1, 0.1, 0.1]),
		0.1.linrand,
		0.5.rand
	]++{1.0.rand}.dup(6.rrand(20)));
	g.chromosome.postln;
	
	//--genome for the two parents
	parent0= RedGAGenome(#[0.97482731342316, 0, 0, 1, 0.99, 0.062391279963776, 0.09280002117157, 0.60202777385712, 0.71044504642487, 0.20561838150024, 0.67790448665619, 0.56602108478546, 0.47632050514221]);
	parent1= RedGAGenome(#[0.91047233343124, 0, 0, 1, 0.73857934403419, 0.01410934634041, 0.48293763399124, 0.018214225769043, 0.91825485229492, 0.70207643508911, 0.13825953006744, 0.83445274829865, 0.50881242752075, 0.4990097284317, 0.55754148960114, 0.8451384305954, 0.33422267436981, 0.43586611747742, 0.90562689304352]);
	//hardcoded parents.  randomize parent genomes above for quite different results
	
	RedGA.crossOverRate= 0.5;
	c= {RedGA.breedMultiPoint(parent0, parent1).choose}.dup(n);
})


//--
//testing
/*
c[0].chromosome
RedGAPhenome(c[0]).asDefString2
RedGAPhenome(c[0]).asSynthDef2.play(s, [\amp, 0.1])
*/


//--
//run simulation.
(
	var width= 300, height= 300, w, wgrav, world,
		synths;
	
	//--world
	w= RedWindow("boids test ga-synths", Rect(128, 64, width, height)).front;
	wgrav= RedVector2D[0, 0.98];
	world= RedWorld3(RedVector2D[width, height], wgrav, 10, 0.1); //dim,grav,maxvel,damp
	
	//--objects
	{
		var loc= RedVector2D[width, height].rand;
		var vel= RedVector2D[0.5, 0.5].rand2;
		var mass= 1.rrand(3);
		RedBoid(world, loc, vel, RedVector2D[0, 0], mass, mass*5) //world,loc,vel,acc,mass,size
			.maxForce_(1.rrand(6)).wdistance_(50).wchange_(0.5);
	}.dup(n);
	Routine{
	synths= c.collect{|x| RedGAPhenome(x).asSynthDef2.play(s, [\amp, 0])};
	0.5.wait;
	
	//--loop
	w.draw{
		4.do{|i|
			Pen.fillColor= Color.red(0.5, i+1/4);
			Pen.fillOval(Rect.aboutRedVector2D(w.mouse, 4-i/3*25));
		};
		world.objects.do{|o, i|
			var amp= 0;
			o.addForceWander2D;
			if(i==0, {
				o.addForceArrive(w.mouse);			//first child chases the mouse
			}, {									//the rest chases the first child
				o.addForceArrive(world.objects.wrapAt(i-1).loc);
			});
			o.addForce(world.gravity);
			world.objects.do{|oo, j| if(i!=j, {o.collide(oo)})};
			o.update;
			world.contain(o);
			amp= (width-o.loc.distance(w.mouse)/width).pow(8);
			synths[i].set(\amp, amp);
			
			//--render
			Pen.fillColor= Color.grey(amp.max(0.1), 0.5);
			Pen.fillOval(Rect.aboutRedObject2D(o));
		};
		
	};
	w.animate= true;
	CmdPeriod.doOnce({if(w.isOpen, {w.close})});
	w.onClose= {synths.do{|x| x.free}};
	}.play(AppClock);
)