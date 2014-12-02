//redFrik

//--changes120801
//qt fix by wouter snoei
//--changes110923
//bugfix for osx 10.6: make sure no windows hidden behind another
//--changes100727
//minor fix to initClass so that new gui schemes (e.g. qt) won't break compilation
//--changes090619
//now using the redirect classes instead of GUI
//took out relativeOrigin and rewrote to use sc3.3.1 userview x/y mouse positions
//--changes090521
//added sortFunc, value_ and valueAction_.  thanks miguel
//--changes090114
//bugfix: userview canfocus now set to false
//--changes080827
//added hiliteColor_
//moved around some methods and added more comments
//andother rewrite of positioning.  bugfix for nested views with decorators.
//--changes080826
//now using SCPopUpMenu instead of SCListView as base. strange mistake
//added GUI.popUpTreeMenu for consistency
//fixed bug in bounds getter
//rewrote positioning. should now work inside views with flowlayout decorators, tabbedview etc
//this also fixed the swingosc offset issues
//--080825
//initial release

PopUpTreeMenu : SCViewHolder {
	var	<>tree, <value, <currentLeaf, <>action, <>openAction, <>closeAction,
		<font, <bounds, <>hiliteColor,
		<>sortFunc,
		pop, usr, hgt, lst, add,
		lastSelected= 0, xIndexLast, yIndexLast, parentWindow, mouseMoved;
	*initClass {
		GUI.schemes.do{|z|
			try{
				z.popUpTreeMenu= PopUpTreeMenu;
			};
		};
	}
	*new {|parent, bounds|
		^super.new.init(parent, bounds);
	}
	init {|argParent, argBounds|
		var dec;
		lst= List.new;							//one array in here for each submenu
		tree= (\nil: ());							//default tree
		font= Font("Monaco", 9);
		hiliteColor= Color.grey;
		
		//--create popUpMenu.  visible when submenus not open
		pop= PopUpMenu(argParent, argBounds)
			.font_(font)
			.background_(Color.clear)
			.stringColor_(Color.black);
		bounds= pop.bounds;
		pop.onClose= {lst.do{|z| if(z[1].notNil, {z[1].close})}};
		this.view_(pop);
		
		//--search for parent decorator.  shift it to allow for userView on top of popUpMenu
		dec= this.parent.decorator;
		if(dec.notNil, {
			dec.shift(bounds.left-dec.left, bounds.top-dec.top);
		});
		
		//--create userView on top of popUpMenu.  any decorator is bypassed with shift above
		usr= UserView(argParent, bounds);
		usr.mouseDownAction_({|v, x, y| mouseMoved= false; this.prUserAction(v, x, y)});
		usr.mouseMoveAction_({|v, x, y| mouseMoved= true; this.prUserAction(v, x, y)});
		usr.mouseUpAction_({|v, x, y| this.prUserActionEnd(v, x, y)});
		usr.canFocus= false;
		
		//--find parentWindow and compensate for some containers that add extra offset (TabbedView)
		argParent= this.parent;
		add= Point(0, 0);
		while({argParent.respondsTo(\findWindow).not}, {
			add= add+Point(argParent.bounds.left.neg, argParent.bounds.top);
			argParent= argParent.parent;
		});
		parentWindow= argParent.findWindow;			//set main window
	}
	
	//--instance methods
	currentPath {
		^lst.collect{|z| z[3][z[2].value]};
	}
	value_ {|path|
		this.prValue_(path, false);
	}
	valueAction_ {|path|
		this.prValue_(path, true);
	}
	
	//--overrides
	bounds_ {|argRect| bounds= argRect; pop.bounds_(bounds); usr.bounds_(bounds)}
	font_ {|argFont| font= argFont; pop.font_(font); /*pop.refresh*/}
	
	//--private
	prValue_ {|path, actionFlag|
		var tmp= tree;
		if(path.every{|x| tmp= tmp[x]; tmp.notNil}, {	//check path valid
			if(tmp.isEmpty, {
				currentLeaf= path;
				value= currentLeaf;
				if(actionFlag, {
					action.value(this, value);		//call action function
				});
				pop.items_([value.last.asString]);
			}, {
				(this.class.name++": node"+path.last+"is a submenu").warn;
			});
		}, {
			(this.class.name++": path"+path+"does not exist").warn;
		});
	}
	prUserAction {|v, x, y|
		var relativePoint= usr.bounds.origin+Point(x, y);
		var xIndex, yIndex;
		if(lst.size==0, {							//check if at root level
			openAction.value(this, x, y);
			this.prSubmenu(v.bounds, nil, pop, nil);	//open a submenu
			xIndex= 0;							//force y update below
			xIndexLast= 0;
		}, {										//at some sub level
			xIndex= lst.detectIndex{|z| z[0].containsPoint(relativePoint)};
			if(xIndex.notNil, {
				if(xIndex!=xIndexLast, {
					if(xIndex>xIndexLast, {			//open a submenu if moving right
						this.prSubmenu(*lst[xIndex]);
					}, {							//else close submenus if open
						lst.copyRange(xIndex+2, lst.size-1).do{|z| z[1].close};
						lst= lst.copyRange(0, xIndex+1);
						yIndexLast= nil;
					});
					xIndexLast= xIndex;
				});
			});
		});
		if(xIndex.notNil, {
			yIndex= (y-(lst[xIndex][0].top-bounds.top)).div(hgt).min(lst[xIndex][2].items.size-1);
			if(yIndex!=yIndexLast, {
				if(lst.size-1>xIndex, {				//close a submenu if open
					lst.last[1].close;
					lst.pop;
					lst.do{|x| x[1].front};			//make sure visibility in order
				});
				lst[xIndex][2].value_(yIndex);
				this.prSubmenu(*lst[xIndex]);
				yIndexLast= yIndex;
				if(lst.size==2, {
					lastSelected= lst[1][2].value;	//remember submenu level1 state
				});
			});
		}, {
			yIndexLast= nil;
		});
	}
	prUserActionEnd {|v, x, y|
		var relativePoint= usr.bounds.origin+Point(x, y);
		var xIndex= lst.detectIndex{|z| z[0].containsPoint(relativePoint)};
		if(xIndex.isNil, {							//mouse released outside menu tree
			//nil.postln;
		}, {										//mouse released on node
			if(mouseMoved.not and:{xIndex==0}, {
				//'did not move'.postln;			//todo: for noclickmode later
			});
			if(currentLeaf.size>0, {
				value= currentLeaf;
				action.value(this, value);			//call action function
				pop.items_([value.last.asString]);
			}, {									//mouse released on node with submenu
				pop.items_([]);
			});
		});
		lst.do{|z| if(z[1].notNil, {z[1].close})};	//close all windows
		lst= List.new;
		xIndexLast= nil;
		yIndexLast= nil;
		closeAction.value(this, x, y);
	}
	prSubmenu {|bounds, window, listView, keys|		//here window argument is ignored
		var addy, subdict, items, newWidth, screenBounds;
		addy= lst.collect{|z| z[3][z[2].value]};		//collect keys
		if(addy.size==0, {							//check if at root level
			subdict= tree;
		}, {										//at some sub level
			subdict= this.prLookup(tree, addy);
		});
		if(subdict.size>0, {						//node not a leaf - create submenu
			keys= subdict.keys.asArray.sort(sortFunc);
			items= keys.collect{|z|					//assume only symbol keys in dict
				if(subdict[z].size>0, {
					z= (z++" >").asSymbol;			//add arrow to nodes with subnodes
				});
				z;
			};
			hgt= "".bounds(font).height;
			if(GUI.id!==\qt, {
				hgt= hgt+3;
			}, {
				if(font.size==9 and:{font.name=="Monaco" or:{font.name=="Geneva"}}, {
					hgt= hgt+1;
				});
			});
			if(addy.size==0, {
				newWidth= bounds.width;				//force root level width to listview
			}, {
				newWidth= items.maxValue{|z| z.asString.bounds(font).width}.max(30)+14;
				bounds= bounds.moveBy(bounds.width, listView.value*hgt)
			});
			bounds= bounds.resizeTo(newWidth, keys.size*hgt);
			if(GUI.id===\qt, {bounds= bounds.resizeBy(0, 4)});
			screenBounds= this.prToScreen(bounds);
			if(screenBounds.top<0, {				//check if submenu below screen bottom
				bounds= bounds.moveBy(0, screenBounds.top);
				screenBounds= this.prToScreen(bounds);
			});
			window= Window("", screenBounds, false, false).front;
			listView= ListView(window, Rect(0, 0, bounds.width, bounds.height))
				.font_(pop.font)
				.background_(pop.background)
				.stringColor_(pop.stringColor)
				.hiliteColor_(hiliteColor)
				.items_(items);
			//here later somehow test if in noclickmode and then track mouseposition from win
			//window.acceptsMouseOver= true;
			//listView.mouseOverAction_({|v, x, y| [v, x, y].postln});
			lst.add([bounds, window, listView, keys]);
			if(lst.size==2, {						//recall submenu level1 state
				listView.value= lastSelected;
			});
			currentLeaf= nil;
		}, {
			currentLeaf= addy;
		});
	}
	prLookup {|tree, addy|
		^if(addy.size>1, {
			this.prLookup(tree[addy[0]], addy.drop(1));
		}, {
			tree[addy[0].asSymbol];					//assume only symbol keys in dict
		});
	}
	prToScreen {|bounds|
		^bounds.moveTo(
			parentWindow.bounds.left+bounds.left-add.x,
			parentWindow.bounds.height+parentWindow.bounds.top-bounds.top-bounds.height-add.y
		)
	}
}
