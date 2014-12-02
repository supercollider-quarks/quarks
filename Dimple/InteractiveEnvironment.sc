InteractiveEnvironment {
	
	// Dimple instance
	var <>dimple;
	
	// Function to invoke to create the virtual environment
	var <>creationFunc;
	
	// Function to be invoked for every object created in the virtual environment
	var <>objectFunc;
	
	// Map from objects names (symbols) to object instaces
	var <objects;
	
	// constructor
	*new { |dimple| ^super.newCopyArgs(dimple) }
	
	// invokes the function to create the environment if one was registered.
	// the clear flag controls whether the environment is cleared before (re)creation
	create { |clear=true|
		if(creationFunc.notNil)
		{
			if(objects.isNil) { objects = Dictionary.new };
			if(clear) { this.clear };
			// invoke the creation function
			creationFunc.value(dimple);
			
			// update the object dictionary
			this.updateObjects;
			
			// invoke the object function for each object
			dimple.world.objects.do { |object, i|
				if(objectFunc.notNil) {
					objectFunc.value(object, i);
				};
			};
		} {
			"error: no function to create the environment registered. Use creationFunc_{|dimple|} to register a function to create an interactive environment.";
		};
	}
	
	updateObjects {
		dimple.world.objects.do { |object|
			objects.put(object.name.asSymbol, object);
		};
	}
	
	// clears the DIMPLE world and clears the object map
	clear {
		dimple.clear;
		objects.clear;
	}
	
	// activates collision detection in DIMPLE and registers a function to handle collisions
	// the callback function will receive the two names of the collided objects (as symbols) and the velocity of the collision as arguments
	handleCollisions { |callback|
		"activating collision detection".postln;
		dimple.collide_(1);
		dimple.world.addAction(\collide, { |msg|
			var object1Name = msg[0];
			var object2Name = msg[1];
			var velocity = msg[2];
			callback.value(object1Name.asSymbol, object2Name.asSymbol, velocity);
		});
	}
	
	// registers a function which is executed at regular time intervals and tracks attribute changes of a specific object
	// the callback function will receive the value of the attribute of interest (can be an array) and the object as argument
	handleAttribute { |object, attribute, interval, callback|
		
		("registering handler for attribute" + attribute + "of object" + object.name + "(update interval:" + interval + "ms)").postln;
		
		object.get(attribute, interval);
		object.addAction(attribute, { |msg|
			callback.value(msg, object);
		});
		
	}
	
	// Attaches a sound to an object, sonifying it in 3D space at the object's current position. The position is updated at an regular time interval specified by the user (default: 10ms)
	// Parameters:
	// object: object to be sonified
	// ndefFunc: sound generation function to be implemented as an Ndef. Function receives x, y and z as paraeters
	// positionUpdateRate: position update interval (in ms)
	sonifyObject { |object, ndefFunc, positionUpdateRate=10|
		var ndefName = object.name.asSymbol;
			
		Ndef(ndefName, ndefFunc).play;
		
		this.handleAttribute(object, \position, positionUpdateRate, { |value|
			var x = value[0];
			var y = value[1];
			var z = value[2];
			Ndef(ndefName).set(\x, x, \y, y, \z, z);
		});
	}

}