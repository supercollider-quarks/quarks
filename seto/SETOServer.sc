/*
Implementation of the server-side of SETO 
	http://tuio.lfsaw.de/
	http://modin.yuri.at/publications/tuio_gw2005.pdf

Author: 
	Till Bovermann 
	2012
	MediaLab Helsinki, Aaalto University	


	2004, 2005, 2006, 2007
	Neuroinformatics Group 
	Bielefeld University
	Germany
*/

/*
	Change
		2007-10-29	renamed to SETOServer
		2006-09-25	moved tuio update after interaction update (now both in alive) to support
					relative coordinate computation in one timestep. 
		2006-09-15	added tStamp support; currently only set from within the language
		2006-08-25	added interactive gui support
		2006-08-21	added general methods for alive/set messages
		2006-07-10	split up into TUIOServer and TUIO_OSCServer
		2006-02-26	added gui support
					remove now makes objects invisible rather then destroying them
		2005-12-16 	removed tuio.play call in TUIO_OSCServer-setFunc_

*/

SETOServer {
	var <knownObjs;
	var objectIDs;
	var <format;
	var <realFormat;
	var setoClass;
	/**
	  function defining how to construct a SETObject. 
	  @param format 
	  @param id
	  @param setoClass 
	  
	  @return instance of setoClass
	 */
	var <>setoConstructorFunc;
	
	var >setFunc;
	var >aliveFunc;


	var iClass;	/// interaction class
	/**
	  function defining how to construct an interaction. 
	  @param setObj1 
	  @param setObj2 
	  @param iClass 
	  
	  @return instance of iClass
	 */
	var <>iClassConstructorFunc; 


	var isEuler;	/// determines if using Euler or Axis Notation. standard is true.
	var <interactions;
	
	// gui support
	var hasGUI, <window, <view;

	*new {|format='2Dobj', setoClass, interactionClass|
		^super.new.pr_initSETOServer(format, setoClass, interactionClass);
	}
	/*
	*pr_hashValue{|a, b|
		^(a.hash@b.hash).hash;	
	}
	*/
	add {|anObject|
//		knownObjs.add(this.class.pr_hashValue(anObject.id, anObject.format) -> anObject);
		knownObjs.add(anObject.id -> anObject);
		objectIDs.add(anObject.id);
		
		/// check if we need a new interaction and create one 
		iClass !? {
			knownObjs.do {|obj|
				(obj == anObject).not.if{
					interactions = interactions.add(this.pr_newInteraction(anObject, obj));
				}
			};
		}
	}
	replaceInteractionFor {|anObj|
		var id;
		var count = 0;
		iClass !? {
			count = interactions.size;
			// remove all interactions containing objs with this id
			interactions = interactions.select{|int|
				int.parts.detect{|obj|
					anObj.id == obj.id
				}.isNil
			};
			// create new interactions
			knownObjs.do {|obj|
				(obj == anObj).not.if{
					interactions = interactions.add(this.pr_newInteraction(anObj, obj));
				}
			};
		}
	}
	start{
		"SETOServer:start : abstract method - no effect".warn
	}
	stop{
		"SETOServer:stop : abstract method - no effect".warn
	}
	gui {|editable = true|
		var addButton, idBox, classIdBox, xBox, yBox, aBox, eBox;
		hasGUI.not.if({
			window = GUI.window.new("SETObjects", Rect(800, 0, 480, 400))
				.front
				.onClose_{
					hasGUI = false;
				};
			window.view.background = Color(0.918, 0.902, 0.886);
			view = SETOServerView(window,  Rect(5,5,390,370), this);
//			view.background =  Color(0.81960784313725, 0.82352941176471, 0.87450980392157, 0.6);
			view.background = Color.fromArray([0.918, 0.902, 0.886] * 0.5 ++ [0.8]);
			view.resize_(5);
			addButton = GUI.button.new(window, Rect(400, 5, 75, 20))
				.states_(	[["add", Color.black, Color.gray(0.5)]])
				.action_{|butt|
					this.setWithFormat("ixya", idBox.value, [classIdBox.value, xBox.value, yBox.value, aBox.value]);
					this.allAlive;
					idBox.value = idBox.value+1;
 				}
 				.resize_(3);

 			GUI.staticText.new(window, Rect(400, 30, 10, 20)).string_("id").resize_(3);
 			idBox = 
 				GUI.numberBox.new(window, Rect(415, 30, 60, 20)).value_(100).resize_(3);
 			GUI.staticText.new(window, Rect(400, 50, 10, 20)).string_("cID").resize_(3);
 			classIdBox = 
 				GUI.numberBox.new(window, Rect(415, 50, 60, 20)).value_(100).resize_(3);
 			GUI.staticText.new(window, Rect(400, 70, 10, 20)).string_("x").resize_(3);
 			xBox  = 
 				GUI.numberBox.new(window, Rect(415, 70, 60, 20)).value_(0.5).step_(0.01).resize_(3);
 			GUI.staticText.new(window, Rect(400, 90, 10, 20)).string_("y").resize_(3);
 			yBox  = 
 				GUI.numberBox.new(window, Rect(415, 90, 60, 20)).value_(0.5).step_(0.01).resize_(3);
 			GUI.staticText.new(window, Rect(400, 110, 10, 20)).string_("a").resize_(3);
 			aBox  = 
 				GUI.numberBox.new(window, Rect(415, 110, 60, 20)).value_(0).step_(0.01).resize_(3);
 			
 			GUI.staticText.new(window, Rect(400, 180, 10, 20)).string_("ext").resize_(3);
 			eBox  = 
 				GUI.numberBox.new(window, Rect(415, 180, 60, 20)).value_(SETO_GUIObj.oExtent).step_(1).resize_(3).action_{|me| SETO_GUIObj.oExtent = me.value};
			hasGUI = true;
			^window.front;
		}, {
			^window
		});
	}

	// private
	pr_initSETOServer {|aFormat, argSETOClass, argInteractionClass|
		
		setoClass 	= argSETOClass ? SETObject;
		if (setoClass.isKindOf(Meta_SETObject).not, {
			"Meta_SETO_Server-new: argument setoClass is not subclass of SETObject.".error;
		});
		knownObjs = IdentityDictionary.new;
		objectIDs = Set[];


		iClass 	= argInteractionClass; // ? SETOInteraction;
		(iClass.notNil && {iClass.isKindOf(Meta_SETOInteraction).not}).if{
			"Meta_SETOServer-new: argument interactionClass is not subclass of SETOInteraction.".error;
		};
		interactions = [];

		format = aFormat.asSymbol;
		if (format.asString.beginsWith("_").not , {
			realFormat = setoClass.formatDict.at(format.asSymbol);
			// if Nil -> not in Dict -> warning;
			if (realFormat.isNil, {
				("SETOServer:pr_initSETOServer : Format not recognized -" + format).warn;
				^nil
			})
		}, {
			realFormat = format.asString[1..];
		});
		// determine if using Euler or Axis rotation notation
		isEuler = Set.newFrom(realFormat.asString).sect(Set[$u, $v, $w]).isEmpty;
		 	
		// initialize set and alive functions
		this.setFunc_{|id ... args|
			this.setWithFormat(realFormat, id, args);
		}; // end this.setFunc_
		
		this.aliveFunc_{|... argObjectIDs|
			this.alive(argObjectIDs);
		};
		// end initialize set and alive functions

		///// init GUI support ////////////////
		hasGUI = false;
	} // end pr_initSETOServer
	pr_removeAt {|id|
		var setObj;
		
		objectIDs.remove(id);
		
		setObj = knownObjs[id];
		setObj !? {setObj.visible = false;}
	}
	allAlive {
		this.alive(objectIDs);
	}
	visibleObjs {
		^knownObjs.selectAs(_.visible, Array)
	}
	deleteObjs {|... ids|
		this.alive(objectIDs -- ids.asSet);		
	}
	alive {|argObjectIDs|
		var deadObjectIDs, setObj;
		
		argObjectIDs = argObjectIDs ? #[];
		hasGUI.if{{
			view.alive(argObjectIDs);
			view.refresh;
		}.defer};
		deadObjectIDs = argObjectIDs.asSet -- objectIDs;
		deadObjectIDs.do{|id|
			this.pr_removeAt(id);
		};
		
		// interaction support
		interactions.do{|int| int.update};

		// update setObj representations.
		objectIDs.do{|id|
			setObj = knownObjs[id];
			setObj.isUpdated.if({
				setObj.update;
				setObj.isUpdated = false
			});
		};
		deadObjectIDs.do{|id|
			setObj = knownObjs[id];
			setObj.update; 
		};
	}
	set {|id, args|
		this.setWithFormat(realFormat, id, args)
	}
	setWithFormat {|actFormat, id, args|
		var setObj, now;	
		
		now = SystemClock.seconds;
		
		// get related object; add it, if it is not in the list
		setObj = knownObjs.at(id);
		setObj ?? {
			setObj = this.pr_newSETObj(format, id);
			setObj.tServer = this;
			this.add(setObj);
			setObj.isEuler = isEuler;				// set euler flag
		};
		
		//set the params of setObj related to its format string
		actFormat.asString.do{|item, i|
			setObj = setoClass.keyDict[item.asSymbol].value(setObj, args[i]);
		};
		objectIDs.add(setObj.id);

		setObj.visible = true;
		setObj.tStamp = now;
		setObj.isUpdated = true;
		
		knownObjs[id] = setObj;
		
		// GUI Support
		hasGUI.if{
			{view.setObj(setObj)}.defer;
		};
	}
	// creators
	pr_newInteraction{|obj1, obj2|
		iClassConstructorFunc.isNil.if({
			^iClass.new(obj1, obj2);
		}, {
			^iClassConstructorFunc.value(obj1, obj2, iClass);
		});
	}
	pr_newSETObj{|format, id|
		setoConstructorFunc.isNil.if({
			^setoClass.new(format, id);
		},{
			^setoConstructorFunc.value(format, id, setoClass);
		});
	}
}

/**
	@todo open gui -> set, alive -> close gui -> set alive -> open gui -> allAlive => no objects...
*/
SETOServerView {
	var view, objects, setoServer;
	
	*new{|parent, bounds, setoServer|
		^super.new.initView(parent,bounds, setoServer);
	}
	initView{|parent, bounds, tServer|
		setoServer = tServer;
		view = GUI.compositeView.new(parent,bounds);
		view.background = Color.white;
		objects = ();
	}
	setObj {|setObj|
		var obj, newObj;
		
		// if object doesn't exists, create it, write it into dict and to obj
		obj = objects[setObj.id] ?? {
			newObj = SETO_GUIObj.new(view, setObj, setoServer);
			objects.put(setObj.id, newObj);
			newObj;
		};
		obj.visible_(true);
	}
	alive {|ids|
		objects.keysValuesDo{|key, obj, i|
			// make non-alive objects invisible
			(ids.includes(key).not).if({obj.visible=false})
		}
	}
	background {
		^view.background;
	}
	background_{|color|
		view.background_(color);
	}
	resize {
	 	^view.resize;
	}
	resize_{|val|
		view.resize_(val)
	}
	refresh {
		objects.do(_.refresh)
	}
}