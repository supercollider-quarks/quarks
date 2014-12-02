// =====================================================================
// Dimple examples (based on the shell scripts that come with dimple)
// =====================================================================
// Marije Baalman (nescivi), (c) 2009
// GNU/GPL
// =====================================================================

// class hierarchy:
Dimple
DimpleThing
  - DimpleWorld
  - DimpleCamera
  - DimpleConstraint
  - DimpleObject
  - - DimplePrism
  - - DimpleMesh
  - - DimpleSphere
  - - DimpleCursor
// =====================================================================

// set the path to your dimple executable:
Dimple.dimplePath = "/path/to/my/dimple/executable";

Dimple.dimplePath = "/home/nescivi/svn/dimple/src/dimple"

// start dimple
d = Dimple.new;

// debugging only:
(
thisProcess.recvOSCfunc = { |time, replyAddr, msg| 
	if(msg[0] != 'status.reply' and: ( msg[0] != '/info') ) {
		"At time %s received message % from %\n".postf( time, msg, replyAddr )
	}
}
);
// stop posting.
thisProcess.recvOSCfunc = nil;



// =====================================================================

// retrieving variables:
d.get( "gravity");
d.get( "collide");


d.world.verbose = 1;

d.world.dump;
d.world.properties

d.camera.get( "position" );
d.camera.get( "lookat" );
d.camera.get( "up" );

d.camera.dump
d.camera.properties;

// =====================================================================

(Dimple.dimplePath + "--send-url osc.udp://localhost:" ++ NetAddr.langPort).unixCmd;

// gravity example:

d.clear;
d.gravity_(0,0,0);

a = d.createSphere( 0, 0, 0 );
b = d.createPrism( 0, 0, -0.2);
a.set(\radius, 0.03 );


// retrieving some more variables
a.verbose_( 0 );

a.get( "velocity" );
a.get( "radius" );
a.get( "acceleration");
a.get( "rotation");
a.get( "force");
a.get( "mass");
a.get( "density");

b.get("position")
//

b.set(\size, [1, 1, 0.01] );
b.set(\color, [0.8, 0.9, 0.1] );
c = d.createConstraint( "fixed", b, d.world )

d.collide_( true );
d.gravity_( 0, 0, -1 );

a.get( "position", 100 );
a.addAction( \position, { |val| ("position = " + val).postln; });

a.set( \position, [0, 0, 0 ] );

a.get( "position", 0 );

a.set( "collide", 1 );

a.addAction( 'collide', { |msg| ("collision" + msg).postln; });
a.set( \position, [0, 0, 0 ] );


// =====================================================================

// balljoint

d.clear;
d.gravity_( 0, 0, 0 );

a = d.createSphere( -0.1, 0, 0 );
a.set( "radius", 0.03 );
a.set( \position, [-0.1, 0, 0] );

c = d.createConstraint( "ball", a, d.world, [0,0,0]);

(
Task({
	a.set( "force", [0.01, 0.005, 0.002] );
	3.0.wait;
	a.set( "force", [0, 0, 0.01] );
}).play;
);

// =====================================================================

/// collide

d.clear;

a = d.createSphere( -0.1, 0, 0 );
b = d.createSphere( 0.1, 0, 0 );

a.set( "radius", 0.03 );
b.set( "radius", 0.03 );

d.collide_( 1 );
a.set( "collide", 1 );

d.world.addAction( "collide", {|msg| msg.postln; });
a.addAction( "collide", {|msg| msg.postln; });

a.set( "force", [0.01, 0, 0] );
b.set( "force", [-0.01, 0, 0] );

// =====================================================================

// cube

d.clear;

a = d.createPrism(  0, 0, 0);
a.set( "size", [0.15, 0.15, 0.15] );
a.set( "color", [1, 0.2, 0.3] );

a.set( "force", [-0.1, 0, 0] );

// =====================================================================

// cylinder (doesn't work??)

d.clear;

a = d.createMesh( Document.current.dir +/+ "cylinder.3ds", 0, 0, 0);
a.set.("size", [0.15, 0.15, 0.15] );
a.set( "color",  [1, 0.2, 0.3] );

// =====================================================================

// destroy

d.clear;
a = d.createSphere( -0.1, 0, 0 );
a.set( "radius", 0.03 );
b = d.createSphere( -0.3, 0, 0 );
b.set( "radius", 0.03 );
b.set( "color" ,[0.2, 0.7, 0.3] );
c = d.createConstraint( "hinge", a, b, [-0.2,0,0, 0,1,0]);

// destroy
//c.destroy

a.set( "force", [0, 0, 0.01] );

a.destroy;
b.destroy;

// =====================================================================

// fixed

d.clear;

a = d.createSphere( -0.1, 0, 0 );
b = d.createPrism( 0.1, 0, 0 );

a.radius_(0.03);
b.size_( 0.06, 0.06, 0.06 );

c = d.createConstraint( "fixed", b, d.world )

// push the sphere towards the prism
a.force_( 0.01, 0, 0 );

a.position
a.position( 50 );
a.addAction( "position", {|msg| msg.postln; });
a.position_( -0.1, 0, 0 );
a.force_( 0.01, 0, 0 );

a.get( "position", 100 )
a.get( "position" )

a.position( 0 );

// =====================================================================

// grab

d.clear;

a = d.createSphere( 0, 0, 0 );

a.radius_(0.02);

a.mass_( 1 );

a.grab;

// =====================================================================

// hinge2

d.clear;

a = d.createPrism( -0.1, 0, 0 );

b = d.createSphere( 0, 0, 0 );

a.size_( 0.07, 0.06, 0.05 );
b.radius_( 0.02 );

a.mass_(0.001);
b.mass_(0.001);

c = d.createConstraint( "hinge2", a, b, [0,0,0, 0,0,1, 0,1,0]);

e = d.createConstraint( "fixed", b, d.world );

(
Task({
	c.torque1_(0.001);
	1.0.wait;
	c.torque2_(0.001);
	1.0.wait;
	c.torque1_(-0.001);
	1.0.wait;
	c.torque2_(-0.001);
	1.0.wait;
}).play;
)


// =====================================================================

// hinge

d.clear;


a = d.createSphere( -0.1, 0, 0 );
a.radius_( 0.03 ); 
b = d.createSphere( -0.3, 0, 0 );
b.radius_( 0.03 ); 
b.color_( 0.2, 0.7, 0.3 );

c = d.createSphere( -0.6, 0, 0 );
c.radius_( 0.03 ); 
c.color_( 0.7, 0.2, 0.3 );

e = d.createConstraint( "hinge", a, d.world, [0,0,0, 0,1,0]);
f = d.createConstraint( "hinge", a, b, [-0.1,0,0, 0,1,0]);
h = d.createConstraint( "hinge", b, c, [-0.3,0,0, 0,1,0]);

// Push the first one really hard, the others oscillate in a funny way.
a.force_( 0, 0, 1 );

// =====================================================================
// many spheres;

d.clear;

a = d.createPrism( 0, 0, -0.2);
a.size_( 1, 1, 0.01 );
a.color_( 0.8, 0.9, 0.1 );
c = d.createConstraint( "fixed", a, d.world );

d.gravity_( 0, 0, -1 );

~size = 6;
(~size*~size - 1).do{ |i| d.createSphere( 
( (i%~size)-( (~size-1)/2.0) )*(0.5 / ~size ), 
( (i%~size)-( (~size-1)/2.0) )*(0.5 / ~size ), 0 );
  };


// =====================================================================

// piston

d.clear;
d.gravity_( 0, 0, 0);

a = d.createPrism( -0.2, 0, 0 );
a.size_( 0.05, 0.05, 0.05 );

c = d.createConstraint( "piston", a, d.world, [-0.1,0,0,0,0,1]);

c.spring_( 0.001, 0.0001 );

a.force_( 0.1, 0.01, 0.001 );
c.force_( -0.01 );

// =====================================================================

// slide

d.clear;

a = d.createPrism( -0.1, 0, 0 );
a.size_( 0.03, 0.03, 0.1 );

c = d.createConstraint( "slide", a, d.world, [0,0,1]);
c.spring_( 0.001, 0.0001);

(
Task( {
	a.force_( 0.01, 0.005, 0.002 );
	2.0.wait;
	c.force_( -0.01 );
}).play
)

// =====================================================================

// spring-hinge

d.clear;

a = d.createPrism( 0, 0, 0.1 );
a.size_( 0.02, 0.1, 0.2 );
a.color_( 1, 0.2, 0.3 );

c = d.createConstraint( "hinge", a, d.world, [0,0,0, 0,1,0]);
c.spring_( 0.001, 0.00001 );

c.torque_( 0.01 );

// =====================================================================
// universal

d.clear

a = d.createPrism( -0.1, 0, 0 );
a.size_( 0.07, 0.06, 0.05 );

c = d.createConstraint( "universal", a, d.world, [0,0,0, 0,0,1, 0,1,0]);
c.spring_( 0.00001, 0.000001 );

(
Task({
	c.torque1_(0.001);
	1.wait;
	c.torque2_(0.001);
	1.wait;
	c.torque1_(-0.001);
	1.wait;
	c.torque2_(-0.001);
	1.wait;
}).play
)

// =====================================================================


d.clear;
d.world.clear

a = d.createSphere( 0.1, 0, 0 );
b = d.createSphere( 0.2, 0, 0 );
c = d.createSphere( 0.3, 0, 0 );

m = d.createPrism( 0, 0, -0.1 );
l = d.createConstraint( "fixed", m, d.world );
m.size_( 5, 5, 0.01 );

~pistons = [a,b,c].collect{ |it| d.createConstraint( "slide", it, m, [0,0,-0.05] ); };

~pistons.do{ |it| it.set("response/spring", [0.001, 0.0001]) };

a.color_( 1, 0, 0);
b.color_( 0, 1, 0);
c.color_( 0, 0, 1);

e = d.createConstraint( "hinge", a, b, [0.05,0,0, 1,0,0] );
f = d.createConstraint( "hinge", b, c, [0.05,0,0, 1,0,0] );

b.force_( 0,0,0 );
b.force_( 0,0,0.01 );
c.force_( 0,0,0.01 ); a.force_( 0,0,0.005 ); b.force_( 0,0,-0.02)


a.force_( 0,0,0 ); c.force_( 0,0,0 ); b.force_( 0,0,0 );
a.position_(0.1,0,0); b.position_( 0.2, 0, 0); c.position_(0.3,0,0);

s.boot;

( 
SynthDef.new( 'springsine', { |out=0,freq=500,amp=0.1,ampmul=1,lag= 0.05,pos=0|
	Out.ar( out, Pan2.ar( SinOsc.ar( freq ) * amp * ampmul.lag(lag), pos) );
}).send(s);
)

~synths = [a,b,c].collect{ |it,i|
	Synth.new( \springsine, [\freq, 500 + (i*200)]);
};



[a,b,c].do{ |it,i| 
	it.addAction( "position", {|msg| ~synths[i].set( \ampmul, msg[2] ); });
	it.position(50);
};

~synths.do{ |it| it.set(\amp,1)};

~synths.do{ |it,i| it.set('freq', 300*i + 300 )};
~synths.do{ |it,i| it.set('pos',(i-1))};

~synths.do{ |it| it.free}; [a,b,c].do{ |it,i| 	it.position(0); };

// --------------------

d.clear;

~xpos = (-1.5..1.5)*0.1;
~ypos = (-3.5..3.5)*0.1;

~spheres = ~xpos.collect{ |it| ~ypos.collect{|jt| d.createSphere( it, jt, 0 ); }};

m = d.createPrism( 0, 0, -0.1 );
m.size_( 5, 5, 0.01 );
l = d.createConstraint( "fixed", m, d.world );


~slides = ~spheres.collect{ |it| it.collect{ |jt| d.createConstraint( "slide", jt, m, [0,0,-0.05] ); }; };
~slides.do{ |it| it.do{ |jt| jt.set("response/spring", [0.001, 0.0001]) }; };

~hingesHor = 8.collect{ |i| 3.collect{ |j| d.createConstraint( "hinge", ~spheres[j][i], ~spheres[j+1][i], [0.05,0,0, 1,0,0]) }};

~hingesVer = 4.collect{ |i| 7.collect{ |j| d.createConstraint( "hinge", ~spheres[i][j], ~spheres[i][j+1], [0,0.05,0, 1,0,0]) }};


~spheres[2][5].force_( 0, 0, -0.025);
~spheres[0][2].force_( 0, 0, -0.025);

~spheres[2][5].force_( 0, 0, 0);

m.visible_( 0 );

d.camera.lookat;
d.camera.up;
d.camera.position_( 0, -1, 0 );

d.camera.position_( 1, -0.5, 0.26 );

d.clear