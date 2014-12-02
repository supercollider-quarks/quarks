
ChuckableBrowser {
	classvar	<chuckClasses, <>default, <all;
	var	<classMenu, <subTypes, <subTypeMenu,
		<instanceList, <instanceListView,
		<keyCommandView, <>keyController;
	
	var	<stateByClass;	// holds last-touched subtype and instance name for each chuck class
	
	var	<layout, <masterLayout, <iMadeMasterLayout = false;

	*initClass {
		chuckClasses = AbstractChuckArray.allSubclasses
			.reject({ |item| item.name.asString[0..7] == "Abstract" })
			.sort({ |a, b| a.name < b.name });
		all = IdentitySet.new;
	}

	*new { |masterLayout, bounds|
		^super.new.init(masterLayout, bounds)
	}
	
	*newWindow { |name|
		var	window = ResizeFlowWindow(name ?? { "Chuck browser" }),
			browser = this.new(window);
		ChuckBrowserKeyController(browser);
		window.recursiveResize.front;
		^browser
	}
	
	init { |master, bounds|
		default.isNil.if({ default = this });
		masterLayout = master ?? {
			iMadeMasterLayout = true;
			FlowView.new
		};
			// note, if you give bounds that are too small, you will be unhappy
		layout = FlowView(masterLayout, bounds ?? { Rect(0, 0, 290, 310) }, margin: 2@2);
		layout.onClose = { this.free; };  // gc so automatic browser updates don't break
		this.initStates;
		this.makeViews;
		this.changeClass;
		instanceListView.focus;
		all.add(this);
	}
	
	free { all.remove(this); }
	
	initStates {
		stateByClass = IdentityDictionary.new;
		chuckClasses.do({ |class|
			stateByClass[class] = [nil, nil];
		});
	}
	
	changeClass { |class|
		var	thisClass, thisType;
		thisClass = class ?? { this.currentClass };
		classMenu.value = chuckClasses.indexOf(thisClass);
		thisType = this.subTypeForClass(thisClass);
			// this use of IdentitySet is like "select distinct" in SQL
		subTypes = IdentitySet.new.addAll(thisClass.collection
			.select(_.respondsTo(\subType))	// the collect breaks if there is nil in the coll.
			.collect(_.subType))
			.asArray.sort;
		subTypes = #[\all] ++ subTypes;
		{ subTypeMenu.items_(subTypes)
			.value_(subTypes.indexOf(thisType) ? 0);
		}.defer;
		this.getInstances(thisClass);
	}
	
	getInstances { |thisClass, currentSubType|
		thisClass = thisClass ?? { this.currentClass };
		instanceList = thisClass.collection.keys;
		currentSubType = currentSubType ?? { this.currentSubtype };
		(currentSubType.notNil).if({
			instanceList = instanceList.select({ |key|
				thisClass.new(key).respondsTo(\subType) and: {
					thisClass.new(key).subType == currentSubType
				}
			});
		});
		instanceList = instanceList.asArray.sort;
		{	instanceListView.items = instanceList.collect(_.asString);
			instanceListView.value = instanceList.indexOf(this.instanceForClass(thisClass)) ? 0;
		}.defer;
	}
	
	makeViews {
		GUI.dragSource.new(layout, 100@20)
			.string_("drag selection").align_(\center)
			.background_(Color.white).stringColor_(Color.new255(70, 130, 200))
			.beginDragAction_({ |drag|
				this.currentClass.new(instanceList[instanceListView.value])
			});
		GUI.dragSource.new(layout, 100@20)
			.string_("drag name").align_(\center)
			.background_(Color.white).stringColor_(Color.new255(70, 130, 200))
			.beginDragAction_({ |drag|
				instanceList[instanceListView.value].asSymbol
			});
		GUI.button.new(layout, 70@20)
			.states_([["refresh", Color.new255(70, 130, 200), Color.new255(255, 218, 237)]])
			.action_({ this.changeClass });
		layout.startRow;
		GUI.button.new(layout, 100@20)
			.states_([["insert selection", Color.new255(70, 130, 200),
				Color.new255(255, 218, 237)]])
			.action_({
/* testing for emacs... didn't work fully
				{
					Document.current.debug("calling insertTextRange on")
					.insertTextRange(
						this.currentClass.new(instanceList[instanceListView.value])
						.asCompileString,
						Document.current.selectedRangeLocation, 0
					)
				}.fork(AppClock);
*/
				Document.current.selectedString_(
					this.currentClass.new(instanceList[instanceListView.value])
					.asCompileString
				)
			});
		GUI.button.new(layout, 90@20)
			.states_([["insert name", Color.new255(70, 130, 200), Color.new255(255, 218, 237)]])
			.action_({
				// {
				// 	Document.current
				// 	.insertTextRange(
				// 		instanceList[instanceListView.value].asSymbol.asCompileString,
				// 		Document.current.selectedRangeLocation, 0
				// 	)
				// }.fork(AppClock);
				Document.current
				.selectedString_(instanceList[instanceListView.value].asSymbol.asCompileString)
			});
		GUI.button.new(layout, 80@20)
			.states_([["insert proto", Color.new255(70, 130, 200), Color.new255(255, 218, 237)]])
			.action_({
				this.currentClass.new(instanceList[instanceListView.value])
					.tryPerform(\proto);
			});
		layout.startRow;
		classMenu = GUI.popUpMenu.new(layout, 160@20)
			.items_(chuckClasses.collect(_.name)).value_(0)
			.action_({ |menu|
				this.changeClass;
			});
		subTypeMenu = GUI.popUpMenu.new(layout, 120@20)
			.action_({ |menu|
				this.setSubtypeForClass(this.currentClass, this.currentSubtype);
				this.getInstances(this.currentClass, this.currentSubtype)
			});
		layout.startRow;
		instanceListView = GUI.listView.new(layout, 284@220)
			.action_({ |list|
				this.setInstanceForClass(this.currentClass,
					instanceList[list.value])
			})
			.beginDragAction_({ |drag|
				this.currentClass.new(instanceList[instanceListView.value])
			});
		keyCommandView = TextFieldOld(layout, 284@20);
	}
	
	focus {	// no flag, this always takes focus
		instanceListView.focus(true);
	}
	
	classMatch { |string|
		chuckClasses.do({ |class|
			(class.name.asString[0..string.size-1] == string).if({
				this.changeClass(class);
				^class
			});
		});
		^nil
	}
	
	subtypeMatch { |string|
		subTypes.do({ |type, i|
			(type.asString[0..string.size-1] == string).if({
				subTypeMenu.value = i;
				(type == \all).if({ type = nil });
				this.setSubtypeForClass(this.currentClass, type);
				this.getInstances(nil, type);
				^type
			});
		});
		^nil
	}
	
	instanceMatch { |string|
		instanceList.do({ |inst, i|
			(inst.asString[0..string.size-1] == string).if({
				instanceListView.value = i;
				this.setInstanceForClass(this.currentClass, inst);
				^inst
			});
		})
		^nil
	}
	
	currentClass {
		^chuckClasses[classMenu.value]
	}
	
	currentSubtype {
		^(subTypes[subTypeMenu.value] == \all).if({ ^nil }, { subTypes[subTypeMenu.value] });
	}
	
	currentObject {
		^this.currentClass.new(instanceList[instanceListView.value])
	}
	
	currentObject_ { |obj|
		var index;
		this.changeClass(obj.class);
		(index = instanceList.detectIndex({ |inst| inst === obj.collIndex })
		).notNil.if({
			instanceListView.value = index;
			this.setInstanceForClass(obj.class, obj.collIndex)
		});
	}
	
	subTypeForClass { |class|
		^stateByClass[class][0]
	}
	
	instanceForClass { |class|
		^stateByClass[class][1]
	}

	setSubtypeForClass { |class, type|
		stateByClass[class][0] = type;
	}

	setInstanceForClass { |class, inst|
		stateByClass[class][1] = inst;
	}
	
		// automatic gui updates
		// only need to change if gui is displaying the class that added or removed an object
	*updateGui { |changedClass| all.do(_.updateGui(changedClass)) }
	
	updateGui { |changedClass|
		{	(this.currentClass === changedClass).if({
				this.changeClass;
			});
		}.defer;
	}
}
