/******* by jostM http://www.glyph.de *******/
/******* Part of TabbedView2 Quark *******/
/******* Uses TabbedViewTab  *******/

TabbedView2{
	var
		<view,
		<>alwaysOnTop=true, // for the popup window
		<tabPosition = \top,
		<>lockPosition = false,  // disables position changing
		<followEdges=true,
		<>lockEdges = false, // disables edge following
		<>dragTabs=true,
		<labelColors, // default array of Colors. can be overriden by individual ta
		<unfocusedColors, // default array of Colors. can be overriden by individual tabs
		<backgrounds,  // default array of Colors. can be overriden by individual tabs
		<stringColors,  // default array of Colors. can be overriden by individual tabs
		<stringFocusedColors,  // default array of Colors. can be overriden by individual tabs
		<focusFrameColor,  // normally Color.clear
		<labelPadding = 20,
		>clickbox=15,
		>swingFactor, // for swing only
		<>unfocusTabs=false, // for swing only
		tabWidth = \auto, // default scheme. cviewan be overriden by individual tabs
		<tabHeight=\auto, // cannot be overriden by individual tabs
		tbht, // the calculated  or set tab height
		tabCurve = 3, // default value. cannot be overriden by individual tabs
		<>tabViews, // array of TabbbedViewTab instances
		<font, // default Font. cannot be overriden by individual tabs
		<resize = 1,
		<>detachedClosable=true,
		<activeTab,
	<>refreshAction,
		focusHistory,
		<window,
		<context, // thu GUI COntext
		<pen,
		>closeIcon,
		>detachIcon,
		left = 0, // probably obsolete
		top  = 0 // probably obsolete
		;

	*new{ arg parent, bounds;
		^super.new.init(parent, bounds);
	}

	init{ arg parent, bounds ;
		var w, col;
		context = GUI.current; // The context is set globally, in case you change gui kits and
		pen     = context.pen;
		/*** Set the Parent View or make a Window, and add a container for the tabs ***/
		w=parent;
		w.isNil.if{ window = w = Window("***",bounds).front;
			bounds = w.view.bounds;
			resize = 5;
		};

		//must be written this way, or nested views don't work with a nil bounds.
		bounds.isNil.if{
			bounds = w.asView.bounds.moveTo(0,0);
		};
		bounds=bounds.asRect; // allows you to use a Point for the bounds.
		( \TabbedViewView.asClass.notNil && (GUI.id == \qt) ).if{
			view = TabbedViewView(w,bounds).resize_(resize);
		}{
			view = context.compositeView.new(w,bounds).resize_(resize);
		};


		/*** Set some defaults ***/
		focusFrameColor=Color.clear;
		font=Font.default;
		swingFactor = Point(0.52146, 1.25);
		stringFocusedColors=[Color.white]	;
		stringColors=[Color.black]	;

		labelColors = [Color(0.85,0.85,0.85)];
		unfocusedColors = [Color(0.75,0.75,0.75,1)];
		backgrounds = [Color(0.85,0.85,0.85)];
		if( GUI.id == \swing)  {
			unfocusTabs=true; // unfocus Tabs if not Cocoa;
			};
		tabViews = [];

		this.defineIcons;
		this.pr_setHandlers;
		^this;
	}


	// Tab Factory
	add { arg label,index, scroll=false; //actually this is an insert method with args backwards
		var tab, container, calcTabWidth, i;
		index = index ? tabViews.size; //if index is nil, add it to the end
		tab = TabbedViewTab.new(this,label,index,scroll); //bounds are set later

		tabViews = tabViews.insert(index, tab);
		// Set defaults
		tab.labelColor=labelColors[ index%labelColors.size ];
		tab.background=backgrounds[ index%backgrounds.size ];
		tab.stringColor=stringColors[ index%stringColors.size ];
		tab.stringFocusedColor=stringFocusedColors[ index%stringFocusedColors.size ];
		tab.unfocusedColor=unfocusedColors[ index%unfocusedColors.size ];

		tab.tabWidth = tabWidth;
		if(tabViews.size==1){ // you want to do this to initially set the history, etc.
			activeTab=tab;	// in other cases, it's up to the user what to focus.
			tab.focus;
		};
		{0.1.wait;this.refresh}.fork(AppClock);
		^tab;

	}

	tabAt{arg index;  ^tabViews[index]}

	resetColors{
		tabViews.do{|tab,index|
		tab.labelColor=labelColors[ index%labelColors.size ];
		tab.background=backgrounds[ index%backgrounds.size ];
		tab.unfocusedColor=unfocusedColors[ index%unfocusedColors.size ];
		tab.stringColor=stringColors[ index%stringColors.size ];
		tab.stringFocusedColor=stringFocusedColors[ index%stringFocusedColors.size ];

		};
		this.refresh
	}


	insert{ arg index,label,scroll=false;
		^this.add(label,index,scroll);
	}


	/** this paints the tabs with rounded edges **/
	paintTab{ arg tab, labelColor, strColor;
		var drawCenter,drawLeft,drawTop,drawRect,drawRight,
			drawBottom,rotPoint,moveBy,rotPointText,drawRectText,
			drawRectText2,rot1=pi/2,rot2=0, tabLabelView=tab.widget, label=tab.label;

		followEdges.if{
			switch(tabPosition)
				{\top}{rot1=0;rot2=0}
				{\left}{rot1=pi;rot2=pi/2}
				{\bottom}{rot1=pi;rot2=pi}
				{\right}{rot1=0;rot2=pi/2};
		}{
			switch(tabPosition)
				{\top}{rot1=0;rot2=pi/2.neg}
				{\left}{rot1=pi;rot2=pi}
				{\bottom}{rot1=pi;rot2=pi/2}
				{\right}{rot1=0;rot2=0};
		};


		tabLabelView.drawFunc = { arg tview;
			var drawCenter,drawLeft,drawTop,drawRect,drawRight,
				drawBottom,rotPoint,moveBy,rotPointText,
				drawRectText,drawRectText2, closable,useDetachIcon,offset=0;

			closable= tab.closable;//tabLabelView.mouseUpAction.notNil;
			useDetachIcon = tab.useDetachIcon;

			drawRect= tview.bounds.moveTo(0,0);
			drawCenter=Point(drawRect.left+(drawRect.width/2),drawRect.top+(drawRect.height/2));

			if ([\top,\bottom].occurrencesOf(tabPosition)>0)
				{drawRectText=Rect(drawRect.left-((drawRect.height-drawRect.width)/2),
					drawRect.top+((drawRect.height-drawRect.width)/2),drawRect.height,drawRect.width);}
				{drawRectText=drawRect};

			if ([\right,\left].occurrencesOf(tabPosition)>0)
				{drawRectText2=Rect(drawRect.left-((drawRect.height-drawRect.width)/2),
					drawRect.top+((drawRect.height-drawRect.width)/2),drawRect.height,drawRect.width);}
				{drawRectText2=drawRect};

			drawLeft  =drawCenter.x-(drawRect.width/2);
			drawTop   =drawCenter.y-(drawRect.height/2);
			drawRight =drawCenter.x+(drawRect.width/2);
			drawBottom=drawCenter.y+(drawRect.height/2);

			pen.use{
				pen.rotate(rot1,drawCenter.x,drawCenter.y);
				pen.width_(1);
				pen.color_(labelColor);
				if ([\top,\bottom].occurrencesOf(tabPosition)>0){
					pen.addWedge( (drawLeft + tabCurve)@(drawTop + tabCurve),
						tabCurve, pi, pi/2);
					pen.addWedge( (drawRight - tabCurve)@(drawTop + tabCurve),
						tabCurve, 0, (pi/2).neg);

					pen.addRect( Rect(drawLeft + tabCurve,
								drawTop,
								drawRect.width - tabCurve - tabCurve,
								tabCurve)
								);
					pen.addRect( Rect(drawLeft,
								drawTop+tabCurve,
								drawRect.width,
								drawRect.height-tabCurve)
							);
				}{
					pen.addWedge( (drawLeft+drawRect.width - tabCurve)
						@(drawTop + tabCurve),
						tabCurve, -pi/2, pi/2);
					pen.addWedge( (drawLeft+drawRect.width - tabCurve)
						@(drawTop + drawRect.height - tabCurve),
						tabCurve, 0, pi/2);

					pen.addRect( Rect(drawLeft+drawRect.width - tabCurve ,
								drawTop + tabCurve,
								tabCurve,
								drawRect.height - tabCurve - tabCurve)
								);
					pen.addRect( Rect(drawLeft,
								drawTop,
								drawRect.width-tabCurve,
								drawRect.height)
							);
				};
				pen.draw;

				pen.rotate(rot2,drawCenter.x,drawCenter.y);
				pen.font_(font);
				pen.color_ (strColor);
				followEdges.if{
 					pen.stringCenteredIn(label,
 						drawRectText2.moveBy(0,if(tabPosition==\top){1}{0};));
 						tab.userDrawFunction.value(pen,drawRectText2,tabPosition,followEdges);
		 				closable.if{
			 				closeIcon.value(pen, Rect(drawRectText2.right-clickbox,
			 					drawRectText2.top,clickbox,clickbox));
			 				offset=(clickbox).neg;
		 				};
		 				useDetachIcon.if{
			 				detachIcon.value(pen, Rect(drawRectText2.right-clickbox+offset,
			 					drawRectText2.top,clickbox,clickbox));
		 				};
 					}{
 					pen.stringLeftJustIn(label,
 						drawRectText.insetAll((labelPadding/2)-2,0,0,0).moveBy(0,1));
 						tab.userDrawFunction.value(pen,drawRectText,tabPosition,followEdges);
		 				closable.if{
		 					closeIcon.value( pen,
		 					  Rect(drawRectText.right-clickbox,drawRectText.top,clickbox,clickbox));
		 					  offset=(clickbox).neg;
		 				};
		 				useDetachIcon.if{
		 					detachIcon.value( pen,
		 					  Rect(drawRectText.right-clickbox+offset,
		 					  	drawRectText.top,clickbox,clickbox));
		 				};
 				};
			};
		};
		tabLabelView.refresh;
	}


	updateFocus{
		tabViews.do{ arg tab,i;
			if (activeTab == tab){
				this.paintTab( tab,
					tab.labelColor,
					tab.stringFocusedColor ); // focus colors
				tab.view.visible_(true);
				// do the user focusAction only on focus
			}{
				this.paintTab( tab,
					tab.unfocusedColor,
					tab.stringColor );// unfocus colors
				tab.view.visible_(false);
					//do the user unfocusAction only on unfocus
			};
		};
	}


	doActions{
		tabViews.do{ arg tab,i;
			if (activeTab == tab){
				if (focusHistory!= tab){ tab.focusAction.value(tab); };
			}{
				if (focusHistory == tab) //do the user unfocusAction only on unfocus
				{ tabViews[ focusHistory.index ]
					.unfocusAction.value(tabViews[ focusHistory.index  ]) };
			};
		};

	}

	stringBounds { |string, font|
		(context.id === \swing).if{
		^Rect(0, 0, string.size * font.size * swingFactor.x, font.size * swingFactor.y);
		}{
		^context.stringBounds(string, font);
		}
	}

	updateViewSizes{"updateViewSizes is deprecated. uses refresh instead".warn; this.refresh}

	refresh{

		if ( tabHeight == \auto ){ tbht = (this.stringBounds("A",font).height+6 )}{tbht=tabHeight};
		tabViews.do{ arg tab, i;
			var closable, useDetachIcon, closepadding=0, detachpadding=0, padding= 0 ;
			closable = tab.closable;
			useDetachIcon = tab.useDetachIcon;
			closable.if{closepadding = clickbox};
			useDetachIcon.if{detachpadding = clickbox};
			// calculate space for icons
			if(useDetachIcon ||closable ){padding = (closepadding + detachpadding)};
			if ( tab.tabWidth.asSymbol == \auto )
				{tab.tbwdth = (this.stringBounds(tab.label.asString,font).width + labelPadding+padding);}
				{tab.tbwdth=tab.tabWidth};

		};

		switch(tabPosition)
		{\top}{this.updateViewRectsTop}
		{\left}{this.updateViewRectsLeft}
		{\bottom}{this.updateViewRectsBottom}
		{\right}{this.updateViewRectsRight};

		tabViews.do{arg tab;
			tab.view.children.notNil.if{
				if (tab.view.children[0].class.name==\FlowView){
					//v.children[0].bounds_(v.bounds);
					//this is redundant, but fixes strange behavior for some reason
					//v.children[0].bounds_(v.children[0].bounds.moveBy(2,2));
					tab.view.children[0].reflowAll;
				};
			};
		};
		refreshAction.value(this);

	}





	updateViewRectsTop{
		tabViews.do{ arg tab, i;
			followEdges.if{
				tab.widget.bounds_( Rect(
						left + ( ([0]++this.tabWidths).integrate.at(i) ) + i,
						top,
						tab.tbwdth,
						tbht)
					);
			}{
				tab.widget.bounds_( Rect(
						left + (i*tbht) + i,
						top ,
						tbht,
						this.tabWidths.maxItem)
					);
			};
			tab.widget.resize = switch(resize)
				{1}{1}
				{2}{1}
				{3}{3}
				{4}{1}
				{5}{1}
				{6}{3}
				{7}{7}
				{8}{7}
				{9}{9};
		};
		followEdges.if{
			tabViews.do{ arg tab, i;
				tab.view.bounds = Rect(
					left,
					top + tbht,
					view.bounds.width,
					view.bounds.height-tbht);

				tab.closeRect=Rect(tab.widget.bounds.width-clickbox, 0, clickbox, clickbox);
				tab.closable.if{tab.detRect=Rect(tab.widget.bounds.width-clickbox-clickbox, 0, clickbox, clickbox)}
					{tab.detRect=Rect(tab.widget.bounds.width-clickbox, 0, clickbox, clickbox)};
			};

		}{
			tabViews.do{ arg tab, i;
				tab.view.bounds = Rect(
					left,
					top + this.tabWidths.maxItem,
					view.bounds.width,
					view.bounds.height-this.tabWidths.maxItem);
				tab.closeRect=Rect(0,0,clickbox,clickbox);
				tab.closable.if{tab.detRect=Rect(0,clickbox.neg,clickbox,clickbox)}
					{tab.detRect=Rect(0,0,clickbox,clickbox)};
			};
		};
		this.updateFocus;
	}

	updateViewRectsLeft{
		tabViews.do{ arg tab, i;
			followEdges.not.if{
				tab.widget.bounds_( Rect(
						left,
						top + (i*tbht) + i,
						this.tabWidths.maxItem,
						tbht)
					);
			}{
				tab.widget.bounds_( Rect(
						left,
						top + ( ([0]++this.tabWidths).integrate.at(i) )  + i,
						tbht,
						tab.tbwdth)
					);
			};

			tab.widget.resize = switch(resize)
				{1}{1}
				{2}{1}
				{3}{3}
				{4}{1}
				{5}{1}
				{6}{3}
				{7}{7}
				{8}{7}
				{9}{9};
		};

		followEdges.not.if{
			tabViews.do{arg tab, i;
				tab.view.bounds = Rect(
					left + this.tabWidths.maxItem,
					top,
					view.bounds.width-this.tabWidths.maxItem,
					view.bounds.height);
					tab.closeRect=Rect(tab.widget.bounds.width-clickbox, 0, clickbox, clickbox);
					tab.closable.if{tab.detRect=Rect(tab.widget.bounds.width-clickbox-clickbox, 0, clickbox, clickbox)}
						{tab.detRect=Rect(tab.widget.bounds.width-clickbox, 0, clickbox, clickbox)};
			};
		}{
			tabViews.do{arg tab, i;
			tab.view.bounds = Rect(
				left + tbht,
				top,
				view.bounds.width-tbht,
				view.bounds.height);
				tab.closeRect=Rect(0, 0, clickbox, clickbox);
					tab.closable.if{tab.detRect=Rect(0, clickbox.neg, clickbox, clickbox)}
						{tab.detRect=Rect(0, 0, clickbox, clickbox)};
			};
		};

		this.updateFocus();
	}

	updateViewRectsBottom{
		tabViews.do{ arg tab, i;
			followEdges.if{

				tab.widget.bounds_( Rect(
						left + ( ([0]++this.tabWidths).integrate.at(i) ) + i,
						top+view.bounds.height-tbht,
						tab.tbwdth,
						tbht)
					);
			}{
				tab.widget.bounds_( Rect(
						left + (i*tbht) + i,
						top+view.bounds.height-this.tabWidths.maxItem ,
						tbht,
						this.tabWidths.maxItem)
					);
			};

			tab.widget.resize = switch(resize)
				{1}{1}
				{2}{1}
				{3}{3}
				{4}{7}
				{5}{7}
				{6}{9}
				{7}{7}
				{8}{7}
				{9}{9};
		};

		followEdges.if{
			tabViews.do{arg tab, i;
				tab.view.bounds = Rect(
					left,
					top,
					view.bounds.width,
					view.bounds.height-tbht);
					tab.closeRect=Rect(tab.widget.bounds.width-clickbox, 0, clickbox, clickbox);
					tab.closable.if{tab.detRect=Rect(tab.widget.bounds.width-clickbox-clickbox, 0, clickbox, clickbox)}
						{tab.detRect=Rect(tab.widget.bounds.width-clickbox, 0, clickbox, clickbox)};
			};
		}{
			tabViews.do{ arg tab, i;
				tab.view.bounds = Rect(
					left,
					top ,
					view.bounds.width,
					view.bounds.height-this.tabWidths.maxItem);
					tab.closeRect=Rect(0, 0, clickbox, clickbox);
					tab.closable.if{tab.detRect=Rect(0, 0, clickbox, clickbox)}
						{tab.detRect=Rect(0, clickbox.neg, clickbox, clickbox)};
			};
		};


		this.updateFocus;
	}

	updateViewRectsRight{
		tabViews.do{ arg tab, i;
			followEdges.not.if{
				tab.widget.bounds_( Rect(
						view.bounds.width + left - this.tabWidths.maxItem,
						top + (i*tbht) + i,
						this.tabWidths.maxItem,
						tbht)
					);
			}{
				tab.widget.bounds_( Rect(
						view.bounds.width + left - tbht,
						top + ( ([0]++this.tabWidths).integrate.at(i) )  + i,
						tbht,
						tab.tbwdth)
					);
			};

			tab.widget.resize = switch(resize)
				{1}{1}
				{2}{3}
				{3}{3}
				{4}{3}
				{5}{3}
				{6}{3}
				{7}{9}
				{8}{9}
				{9}{9};
		};
		followEdges.not.if{
			tabViews.do{arg tab, i;
				tab.view.bounds = Rect(
					left,
					top,
					view.bounds.width-this.tabWidths.maxItem,
					view.bounds.height);
					tab.closeRect=Rect(tab.widget.bounds.width-clickbox,0, clickbox, clickbox);
					tab.closable.if{tab.detRect=Rect(tab.widget.bounds.width-clickbox-clickbox,0, clickbox, clickbox)}
						{tab.detRect=Rect(tab.widget.bounds.width-clickbox,0, clickbox, clickbox)};
			};
		}{
			tabViews.do{arg tab, i;
				tab.view.bounds = Rect(
					left,
					top,
					view.bounds.width-tbht,
					view.bounds.height
					);
					tab.closeRect=Rect(tab.widget.bounds.width-clickbox, tab.widget.bounds.height-clickbox, clickbox, clickbox);
					tab.closable.if{tab.detRect=Rect(tab.widget.bounds.width-clickbox,
							tab.widget.bounds.height-clickbox-clickbox, clickbox, clickbox)}
						{tab.detRect=Rect(tab.widget.bounds.width-clickbox, tab.widget.bounds.height-clickbox, clickbox, clickbox)};
			};
		};

		this.updateFocus();

	}


	tabWidths{
		^tabViews.collect{|tab| tab.tbwdth};
	}

	tabPosition_{arg symbol; // \left, \top, \right, or \bottom
	 	tabPosition=symbol;
		this.refresh();

	}

	followEdges_{arg bool;
	 	followEdges=bool;
		this.refresh();

	}

	resize_{arg int;
		resize = int;
		view.resize_(int);
		tabViews.do{|tab| tab.view.resize_(int)};
		this.refresh;
	}

	focus{arg index;
		activeTab = tabViews[index];
		this.updateFocus;
		this.doActions;
		focusHistory = activeTab;
	}

	labelColors_{arg colorArray;
		labelColors = colorArray;
		this.refresh();
	}

 	unfocusedColors_{arg colorArray;
 		unfocusedColors = colorArray;
 		this.refresh();
 	}

	backgrounds_{arg colorArray;
		backgrounds = colorArray;
		this.refresh();
	}

	stringColors_{arg colorArray;
		stringColors = colorArray;
		this.refresh()
	}
	stringFocusedColors_{arg colorArray;
		stringFocusedColors = colorArray;
		this.refresh()
	}
	labelPadding_{arg int;
		labelPadding = int;
		this.refresh;
	}

	tabWidth_{arg int;
		tabWidth=int;
		tabViews.do{|v| v.tabWidth=int};
		//this.refresh();
	}

	tabHeight_{arg val;
		tabHeight = val;
		this.refresh();
	}

	tabCurve_{arg int;
		tabCurve = int;
		this.refresh();
	}

	font_{ arg fnt;
		 font = fnt;
		 this.refresh();

	}

	focusFrameColor_{arg color;focusFrameColor=color; tabViews.do{arg v; v.focusColor=focusFrameColor}}


	removeAt{ arg index;
		tabViews[index].unfocusAction.value(tabViews[index]);
		tabViews[index].pr_removeWidgets;
		this.pr_removeTab(index);
	}

	pr_removeTab{ arg index;
		tabViews.removeAt(index);
		this.pr_refreshIndex;
		if(tabViews.size>0){
		this.focus(min(tabViews.size-1,focusHistory.index));

		focusHistory=tabViews.at(max(tabViews.size-1,index-1));
		};
		this.refresh();
		view.refresh;

	}

	pr_refreshIndex{

		tabViews.do{arg tab, i;
			tab.pr_setIndex(i);
		};

	}

	pr_setHandlers{
		view.canReceiveDragHandler_({arg view;
			var parents, currentDrag, ret=false;
			currentDrag = GUI.view.currentDrag;

			// Current drag must be a TabbedViewTab,
			// and the reciever may not be a child of the current drag
			if(currentDrag.class==TabbedViewTab){
				parents = view.getParents();
				((parents.indexOf(currentDrag.view)).isNil && (view!=currentDrag.view.parent)).if{ret=true};
			};
			ret;
		});
		//widget.canReceiveDragHandler.addFunc({true});
		view.receiveDragHandler_({
			View.currentDrag.setParent(this);
		});

	}

	// private utility. gets the closest tab index of a given xy position
	closestIndexOf{arg x, y;
		var dropindex,pos=0;

		if ((tabPosition==\left) ||( tabPosition==\right)){
			tabViews.do{arg tab;
				var top=tab.widget.bounds.top,
				bottomhalf=tab.widget.bounds.top + (tab.widget.bounds.height/2);

				if ((y>=top) && (y<=bottomhalf)){dropindex=tab.index};
				if ((y>=(top+(tab.widget.bounds.height/2))) &&
					(y<=(top+tab.widget.bounds.height))){dropindex=tab.index;pos=1};
			};
		}{
			tabViews.do{arg tab;
				var left=tab.widget.bounds.left,
				righthalf=tab.widget.bounds.left + (tab.widget.bounds.width/2);

				if ((x>=left) && (x<=righthalf)){dropindex=tab.index};
				if ((x>=(left+(tab.widget.bounds.width/2))) &&
					(x<=(left+tab.widget.bounds.width))){dropindex=tab.index;pos=1};
			};
		};
		^[dropindex,pos];
	}

	// this makes icons easier to overide
	defineIcons{
		 detachIcon = {|pen,bounds|
			var rect=Rect.fromRect(bounds).insetBy(2,2);
			pen.width=0.3;
			pen.color_(Color.grey.alpha_(0.2));
			pen.addRect(rect);
			pen.fill;
			pen.width=2;
			pen.color_(Color.white).alpha_(0.8);
			pen.line(Point(rect.left+2, rect.top+(rect.height/2)),Point(rect.right-2,  rect.top+(rect.height/2)));

			pen.stroke;
			pen.width=1;
		};
		closeIcon = {|pen,bounds|
			var rect=Rect.fromRect(bounds).insetBy(2,2);
			pen.color_(Color.grey).alpha_(0.2);
			pen.addOval(rect);
			pen.fill;
			pen.color_(Color.white).alpha_(0.8);
			rect=rect.insetBy(3,3);
			pen.line(rect.leftTop,rect.rightBottom);
			pen.line(rect.rightTop,rect.leftBottom);
			pen.stroke;
		};

	}

	clone{ arg parent,x=0,y=0;
		var t = this.class.new(parent, view.bounds);
		parent.isNil.if{
			t.window.setTopLeftBounds(Rect(view.absoluteBounds.left+20,view.absoluteBounds.top+20,
			view.bounds.width,view.bounds.height).moveBy(x,y));
			t.window.alwaysOnTop=alwaysOnTop;
			t.window.userCanClose=detachedClosable;
			( \TabbedViewView.asClass.notNil ).if{
				t.view.onBeginClose={
					var tempViews;
					tempViews=t.tabViews.collect{|t| t};
					tempViews.do{arg tab;
						var tempFocus;
						tab.homeView.notNil.if{
							tab.homeView.view.isClosed.not.if{
								tempFocus = tab.homeView.activeTab;
								tab.setParent(tab.homeView,tab.homeView.tabViews.size);
								tempFocus.focus;
							}
						};

					};
				};
			}{
				"Please Activate TabbedView2_QT Quark, to prevent losing tabs when closing a window.".warn;
			};
		};
		t.labelColors = labelColors;
		t.unfocusedColors = unfocusedColors;
		t.backgrounds = backgrounds;
		t.stringColors = stringColors;
		t.stringFocusedColors = stringFocusedColors;
		t.focusFrameColor = focusFrameColor;
		t.unfocusTabs = unfocusTabs;
		t.tabWidth =  tabWidth;
		t.tabHeight = tabHeight;
		t.tabCurve = tabCurve;
		t.font = font;
		t.tabPosition = tabPosition;
		t.followEdges = followEdges;
		t.closeIcon = closeIcon;
		t.labelPadding = labelPadding;
		t.swingFactor = swingFactor;
		t.clickbox = clickbox;
		t.dragTabs = dragTabs;


		^t;
	}

	views{
		^tabViews;
	}

}
