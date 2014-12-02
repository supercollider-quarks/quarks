/**********************************************************
* DIMPLE GUI                                              *
* Graphical User Interface for DIMPLE, the interactive    *
* Environment supporting haptic interaction               *
*                                                         *
* Created by David Hofmann, HfM Karlsruhe, September 2012 *
**********************************************************/

DimpleGUI {
	
	// dimple instance
	var <>dimple;
	
	// ListView of objects in the environment
	var objectList;
	
	// default width of number boxes
	var numberBoxWidth = 45;
	
	// currently selected object (name of object as symbol)
	var selectedObject;
	
	// List of DIMPLE attributes which are monitored with the GUI
	// initialized in init()
	var attributes;
	
	// Dictionaty of default values for attributes
	// initialized in init()
	var defaultValues;
	
	// Dictionary mapping GUI elements for an attribute
	var attributeInterfaces;
	
	*new { |dimple| ^super.newCopyArgs(dimple).init }
	
	init {
		// initalize attributes
		attributes = List.new;
		defaultValues = Dictionary.new;
		this.addAttribute(\position, [0,0,0]);
		this.addAttribute(\size, [0.01,0.01,0.01]);
		this.addAttribute(\radius, [0.01]);
		this.addAttribute(\velocity, [0,0,0] );
		this.addAttribute(\acceleration, [0,0,0]);
		this.addAttribute(\force, [0,0,0]);
		this.addAttribute(\mass, [0]);
		this.addAttribute(\density, [100]);
		this.addAttribute(\color, [0,0,0]);
		this.addAttribute('friction/static', 0);
		this.addAttribute('friction/dynamic', 0);
		
		attributeInterfaces = Dictionary.new;

		this.createGUI;
	}
	
	addAttribute { |attribute, defaultValue|
		attributes.add(attribute);
		defaultValues.put(attribute, defaultValue);
	}
	
	createGUI {
		var window, hLayout;
		var controlSection, objectSection, attributeSection;
		var numColumns = 3;
		
		var controlSectionWidth = 180;
		var objectSectionWidth = 150;
		var attributeSectionWidth = 300;
		
		var totalWidth;
		var height = 600;
		
		totalWidth = controlSectionWidth + objectSectionWidth + attributeSectionWidth;
		
		window = Window("DIMPLE GUI", Rect(100, 500, totalWidth, height)).front;
		window.addFlowLayout;
		
		hLayout = HLayoutView(window, Rect(0,0,totalWidth,height));
		controlSection = VLayoutView(hLayout, Rect(0,0,controlSectionWidth,height));
		objectSection = VLayoutView(hLayout, Rect(0,0,objectSectionWidth,height));
		attributeSection = VLayoutView(hLayout, Rect(0,0,attributeSectionWidth,height));
		
		// Control Section
		this.createControlSection(controlSection, controlSectionWidth, height);

		// Object selection
		this.createObjectBrowser(objectSection, objectSectionWidth, height);
		
		// Attribute section
		this.createAttributeSection(attributeSection, attributeSectionWidth, height);
	
	}
	
	createControlSection { |parent, width, height|
		
		
		this.createHeading(parent, width, "Control Section");
		
		// start button
		Button(parent, Rect(0,0,100,20))
			.states_([["Start DIMPLE", Color.black, Color.green]])
			.action_{ dimple = Dimple.new };
			
		// Camera position section
		this.create3DControlSection(parent, width, "Camera Position", 0, -1, 0, { |x,y,z|
			if(dimple.notNil) {
				dimple.camera.set(\position, [x,y,z]);
			};
		});
		
		// Camera look at section
		this.create3DControlSection(parent, width, "Camera Look At Point", 0, 0, 0, { |x,y,z|
			if(dimple.notNil) {
				dimple.camera.set(\lookat, [x,y,z]);
			};
		});
		
		// Gravity Section
		this.create3DControlSection(parent, width, "Gravity", 0, 0, 0, { |x,y,z|
			if(dimple.notNil) {
				dimple.gravity_(x,y,z);
			};
		});
	}
	
	createObjectBrowser { |parent, width, height|
		
		this.createHeading(parent, width, "Object Browser");
		objectList = ListView(parent, Rect(0,0,width,height-100))
			.action_({ |lv|
				selectedObject = lv.items[lv.value].asSymbol;
				this.updateAttributes(0);
			});
		this.updateObjectList;
		//objectList.items_(["object1", "object2", "object3"]);
		// update object list
		Button(parent, Rect(0,0,100,20))
			.states_([["Update Object List", Color.black, Color.green]])
			.action_({ this.updateObjectList });
		
		// create prism
		Button(parent, Rect(0,0,100,20))
			.states_([["Create Prism", Color.black, Color.new(0.5, 0.5, 0.9)]])
			.action_({ dimple.createPrism; this.updateObjectList; });
		
		// create sphere
		Button(parent, Rect(0,0,100,20))
			.states_([["Create Sphere", Color.black, Color.new(0.5, 0.5, 0.8)]])
			.action_({ dimple.createSphere; this.updateObjectList; });
	}
	
	updateObjectList {
		if(dimple.notNil) {
			objectList.items_(
				dimple.world.objects.collect({ |object| object.name.asString });
			);
		};
	}
	
	createAttributeSection { |parent, width, height|

		this.createHeading(parent, width, "Attribute Inspector");
	
		attributes.do { |attribute|
			var interface;
			attribute = attribute.asSymbol;
			interface = this.createAttributeInterface(parent, width, attribute, defaultValues.at(attribute));
			attributeInterfaces.put(attribute, interface);
		};
		
		this.createUpdateControl(parent, width);
	}
	
	createUpdateControl { |parent, width|
		var layout, updateIntervalField;
		// spacing
		HLayoutView(parent, Rect(0,0,width,40));
		
		// update section
		layout = HLayoutView(parent, Rect(0,0,width,20));
		StaticText(layout, Rect(0,0,20,20));
		StaticText(layout, Rect(0,0,140,20)).string_("Update Time Interval");
		updateIntervalField = NumberBox(layout, Rect(0,0,numberBoxWidth,20))
			.value_(0);
		Button(layout, Rect(0,0,80,20))
			.states_([["update", Color.black, Color.green]])
			.action_({ this.updateAttributes(updateIntervalField.value) });
		
	}
	
	updateAttributes { |interval|
		var object = this.getObject(selectedObject);
		if(object.isNil) {
			("could not find object" + selectedObject).postln;
			^this;
		};
		
		// register OSC responders for attribute updates
		object.removeResponders;
		attributes.do { |attribute|
			("setting up OSC responder for attrbibute" + attribute + ", interval" + interval).postln;
			object.addAction(attribute, { |value|
				if(selectedObject == object.name.asSymbol) {
					this.updateNumberBox(attribute, value);
				};
			});
			object.get(attribute, interval);
		}
		
	}
	
	updateNumberBox { |attribute, value|
		var interface = attributeInterfaces.at(attribute);
		("Updating NumberBox(es) for attribute" + attribute).postln;
		if(interface.notNil) {
			{
				if(value.isArray and: { interface.isArray}) {
					value.do { |val, i|
						interface[i].value_(val);
					};
				} {
					if(value.isNumber) {
						interface.value_(value);
					} {
						("not a number for attribute" + attribute + ": " + value).postln;
					}
				}
			}.defer;
		} {
			("no interface found for attribute" + attribute).postln;
		};
	}
	
	getObject { |objectName|
		if(objectName.isNil) { ^nil };
		
		dimple.world.objects.do { |object|
			if(object.name.asSymbol == objectName) { ^object };
		};
		
		^nil;
	}
	
	createAttributeInterface { |parent, width, attribute, defaultValue|
		var layout = HLayoutView(parent, Rect(0,0,width,20));
		
		// some space
		StaticText(layout, Rect(0,0,20,20));
		
		// atrribute name
		StaticText(layout, Rect(0,0,120,20)).string_(attribute.asString);
		
		// number boxes
		case
		{defaultValue.isNumber}
		    { ^this.createAttributeNumberBox(layout, defaultValue, attribute) }
		{defaultValue.isArray}
		    {
			    var array = nil!defaultValue.size;
			    defaultValue.do { |value, i|
				    array[i] = this.createAttributeNumberBox(layout, value, attribute);
				}
				
				^array;
			};
	}
	
	createAttributeNumberBox { |parent, initialValue, attribute, width|
		var numberBox;
		
		if(width.isNil) { width = numberBoxWidth };
		
		numberBox = NumberBox(parent, Rect(0,0,width,20))
		.value_(initialValue)
		.action_({this.setNewValue(attribute)});
		
		^numberBox;
	}
	
	setNewValue { |attribute|
		var interface = attributeInterfaces.at(attribute);
		("setting new value for attribute" + attribute).postln;
		if(interface.notNil) {
			if(interface.isArray.not) {
				var value = interface.value;
				this.postNewValue(attribute, value);
			} {
				var array = nil!interface.size;
				interface.do { |numberBox, i|
					array[i] = numberBox.value;
				};
				
				this.postNewValue(attribute, array);
			}
		} {
			("no interface found for attribute" + attribute).postln;
		};
	}
	
	postNewValue { |attribute, value|
		 var object = this.getObject(selectedObject);
		 
		 if(object.notNil) {
			 ("setting attribute" + attribute + "of object" + selectedObject + "to" + value).postln;
			 object.set(attribute, value);
		 } {
			 ("object" + selectedObject + "not found").postln;
		 };
	}
	
	create3DControlSection { |parent, width, heading, initialX = 0, initialY = 0, initialZ = 0, updateFunc|
		
		var positionLayout, numberBoxLayout;
		var xNumberBox, yNumberBox, zNumberBox;
		var min = -1;
		var max = 1;
		var sliderHeight = 140;
		var staticTextWidth = 12;
		
		this.createHeading(parent, width, heading);
		
		positionLayout = HLayoutView(parent, Rect(0,0,sliderHeight+40,sliderHeight));
		
		Slider2D(positionLayout, Rect(0,0,sliderHeight,sliderHeight))
			.x_(initialX.linlin(min,max,0,1))
			.y_(initialY.linlin(min,max,0,1))
			.action_{ |slider|
				xNumberBox.valueAction_(slider.x.linlin(0,1,min,max));
				yNumberBox.valueAction_(slider.y.linlin(0,1,min,max));
			};
			
		Slider(positionLayout, Rect(0,0,20,sliderHeight))
			.value_(initialZ.linlin(min,max,0,1))
			.action_{ |slider|
				zNumberBox.valueAction_(slider.value.linlin(0,1,min,max));
			};
			
		numberBoxLayout = HLayoutView(parent, Rect(0,0,width,20));
		
		StaticText(numberBoxLayout, Rect(0,0,staticTextWidth,20)).string_("X:");
		xNumberBox = NumberBox(numberBoxLayout, Rect(0,0,numberBoxWidth,20))
			.value_(initialX)
			.action_{ updateFunc.value(xNumberBox.value, yNumberBox.value, zNumberBox.value) };
		
		StaticText(numberBoxLayout, Rect(0,0,staticTextWidth,20)).string_("Y:");
		yNumberBox = NumberBox(numberBoxLayout, Rect(0,0,numberBoxWidth,20))
			.value_(initialY)
			.action_{ updateFunc.value(xNumberBox.value, yNumberBox.value, zNumberBox.value) };
		
		StaticText(numberBoxLayout, Rect(0,0,staticTextWidth,20)).string_("Z:");
		zNumberBox = NumberBox(numberBoxLayout, Rect(0,0,numberBoxWidth,20))
			.value_(initialZ)
			.action_{ updateFunc.value(xNumberBox.value, yNumberBox.value, zNumberBox.value) };
	}
	
	createHeading { |parent, width, heading|
		StaticText(parent, Rect(0,0,width,20))
			.align_(\center)
			.string_(heading);
	}

}