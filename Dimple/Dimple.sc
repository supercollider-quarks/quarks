// =====================================================================
// Marije Baalman (nescivi), (c) 2009
// GNU/GPL
// =====================================================================

Dimple {

	classvar <>dimplePath = "/home/nescivi/Desktop/dimple-0.0.8-1/src/dimple";

	var <>addr;
	var <>recvaddr;

	var <world,<camera,<cursor;
	

	*new{
		^super.new.init;
	}

	init{
		addr = NetAddr("localhost",7774);
		//	recvaddr = NetAddr("localhost",7775);
		recvaddr = nil;
		this.start;
		world = DimpleWorld.new( this );
		camera = DimpleCamera.new( this );
		cursor = DimpleCursor.new( this ).set( \position, [0, 0, 0] );
	}

	start{
		//	(dimplePath + NetAddr.langPort).unixCmd;
		(dimplePath + "-u osc.udp://localhost:" ++ NetAddr.langPort).unixCmd;
		//		ShutDown.add({ this.stop });
	}

	createSphere{ |x,y,z|
		var obj;
		obj = DimpleSphere.new( this, x, y, z );
		world.addObject(obj);
		^obj;
	}

	createPrism{ |x,y,z|
		var obj;
		obj = DimplePrism .new( this, x, y, z );
		world.addObject(obj);
		^obj;
	}

	createMesh{ |meshfile,x,y,z|
		var obj;
		obj = DimpleMesh.new( this, x,y,z, meshfile );
		world.addObject(obj);
		^obj
	}

	createConstraint{ |type,a,b,argVector|
		var constr = DimpleConstraint.new( this, type, a, b, argVector );
		world.addConstraint( constr );
		^constr;
	}


	// forward to world:

	clear{
		^world.clear;
	}

	drop{
		^world.drop;
	}

	

	doesNotUnderstand{ arg selector ... args;
		^this.world.performList( selector, args );
	}

}

DimpleThing {

	var <>name;
	var <>dimple;

	var <responders;
	var <actions;
	var <properties;

	var <>verbose = 0;

	init{
		this.initProps;
	}

	initProps{
		actions = IdentityDictionary.new;
		properties = IdentityDictionary.new;
		responders = List.new;
	}

	oscPath{ |property|
		^("/world" ++ "/" ++ name ++ "/" ++ property.asString)
	}

	set{ |property,val|
		var msg = [this.oscPath(property) ] ++ val;
		dimple.addr.sendMsg( *msg );
		properties[property.asSymbol] = val;
		if ( property.asSymbol == 'collide' ){
			this.addOSCresponder('collide');
		};
	}

	get{ |property,interval|
		var msg = [this.oscPath(property) ++ "/" ++ "get"] ++ interval;
		dimple.addr.sendMsg( *msg );
		this.addOSCresponder( property );
	}

	addAction{ |property,func|
		actions.put( property.asSymbol, func );
	}

	removeAction{ |property|
		actions.put( property.asSymbol, {} );
	}

	addOSCresponder{ |property|
		var newresponder = OSCresponder.new( dimple.recvaddr, this.oscPath(property), {
			|t,r,msg| 
			if ( verbose > 0 ){ [t,r,msg].postln; };
			this.properties[property.asSymbol] = msg.copyToEnd(1);
			this.actions.at( property.asSymbol ).value( (msg.copyToEnd(1)) );
		}).add;
		responders.add( newresponder );
	}

	addResponders{
		responders.do{ |it| it.add };
	}

	removeResponders{
		responders.do{ |it| it.remove };
	}

	doesNotUnderstand{ arg selector ... args;
		if ( selector.asString.last == $_ ){
			selector = selector.asString.drop(-1).asSymbol;
			^this.set( selector, args );
		}{
			^this.get( selector, args );
		}
	}

}

DimpleWorld : DimpleThing{
	//	var <collide;
	//	var <gravity;
	
	var <objects,<constraints;

	*new{ |dimple|
		^super.newCopyArgs( "world", dimple ).init;
	}

	init{
		this.initProps;
		objects = List.new;
		constraints = List.new;
	}

	oscPath{ |property|
		^("/world" ++ "/" ++ property.asString)
	}

	drop{ 
		dimple.addr.sendMsg( "/world/drop" );
	}

	clear{ 
		dimple.addr.sendMsg( "/world/clear" );
		objects.clear;
		constraints.clear;
	}

	addObject{ |obj|
		objects.add( obj );
	}

	addConstraint{ |obj|
		constraints.add( obj );
	}

	removeObject{ |obj|
		objects.remove( obj );
	}

	removeConstraint{ |obj|
		constraints.remove( obj );
	}
	
}

DimpleCamera : DimpleThing {
	
	//	var <position,<lookat,<up;

	*new{ |dimple|
		^super.newCopyArgs( "camera", dimple ).init;
	}

}


DimpleConstraint : DimpleThing {

	classvar <>lastid = 0;

	var <>type,<objectA, <objectB;
	var <>argVector;

	*new{ |dimple,type,a,b,argVector|
		^super.newCopyArgs( "constraint", dimple ).setMyArgs( type, a, b, argVector ).init;
	}

	setMyArgs{ |type,a,b,argVector |
		this.type_( type );
		objectA = a;
		objectB = b;
		this.argVector_( argVector );
	}

	init{ 
		var msg;
		this.initProps;
		name = type.asString++lastid;
		lastid = lastid + 1;
		msg = [ "/world" ++ "/" ++ type ++ "/" ++ "create", name, objectA.name, objectB.name  ] ++ argVector;
		dimple.addr.sendMsg( *msg );
		//		this.dump;
		objectA.addConstraint( this );
		objectB.addConstraint( this );
	}

	spring_{ |a,b|
		//properties.put( 'response/spring', [a,b] );
		this.set( "response/spring", [a,b]);
	}

	destroy{ |osc=true|
		if ( osc ){
			dimple.addr.sendMsg( "/world" ++ "/" ++ name ++ "/" ++ "destroy" );
		};
		this.removeResponders;
		dimple.world.removeConstraint( this );
		objectA.removeConstraint( this );
		objectB.removeConstraint( this );
	}

}

DimpleObject : DimpleThing {

	classvar <>lastid = 0;

	var <>type;

	var <>addr;

	var <constraints;

	*new{ |name,dimple,type,x,y,z|
		^super.newCopyArgs( name, dimple ).type_( type ).init;
	}

	init{
		this.initProps;
		this.initObject;
	}

	initObject{
		constraints = List.new;
		addr = dimple.addr;		
	}

	grab{
		addr.sendMsg( "/world" ++ "/" ++ name ++ "/" ++ "grab" );
	}

	destroy{ |osc=true|
		if ( osc ){
			addr.sendMsg( "/world" ++ "/" ++ name ++ "/" ++ "destroy" );
		};
		this.removeResponders;
		dimple.world.removeObject( this );
		constraints.do{ |it| it.destroy(false) };
	}

	addConstraint{ |constr|
		constraints.add( constr );
		//		constraints.postln;
		//		constr.dump;
	}

	removeConstraint{ |constr|
		constraints.remove( constr );
	}

}

DimpleSphere : DimpleObject {

	*new{ |dimple,x,y,z|
		^super.newCopyArgs( "sphere", dimple ).init( x,y,z );
	}

	init{ |x,y,z|
		this.initProps;
		this.initObject;
		name = "sphere"++lastid;
		type = \sphere;
		lastid = lastid + 1;
		addr = dimple.addr;
		addr.sendMsg( "/world/sphere/create", name, x, y, z );
	}
}


DimpleMesh : DimpleObject {

	var <>file;

	*new{ |dimple,x,y,z,file|
		^super.newCopyArgs( "mesh", dimple ).file_( file ).init(x,y,z);
	}

	myInit{ |x,y,z|
		this.initProps;
		this.initObject;
		name = "mesh"++lastid;
		type = \mesh;
		lastid = lastid + 1;
		addr = dimple.addr;
		addr.sendMsg( "/world/sphere/create", name, file, x, y, z );
	}

}

DimplePrism : DimpleObject {

	*new{ |dimple,x,y,z|
		^super.newCopyArgs( "prism", dimple ).init(x,y,z);
	}

	init{ |x,y,z|
		this.initProps;
		this.initObject;
		type = \prism;
		name = "prism"++lastid;
		lastid = lastid + 1;
		addr = dimple.addr;
		addr.sendMsg( "/world/prism/create", name, x, y, z );
	}

}

DimpleCursor : DimpleObject {

	*new{ |dimple|
		^super.newCopyArgs( "cursor", dimple ).init;
	}

	init{
		this.initProps;
		this.initObject;
		addr = dimple.addr.copy.port_( 7772 );
		type = \cursor;
	}

}