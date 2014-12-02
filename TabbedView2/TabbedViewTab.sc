/******* by jostM http://www.glyph.de *******/
/******* Part of TabbedView2 Quark *******/
/******* Is used by TabbedView2  *******/

TabbedViewTab : SCViewHolder{

	var  <>tabbedView,<index,context, <>label, <>closable=false, <>widget, <>homeView,
	<>focusAction,<>unfocusAction,<tabWidth=\auto, <>tbwdth=10,<useDetachIcon=false,
	<>rightClickDetach=true, <>labelColor,  <>unfocusedColor, <>stringColor,tempView,
	<>stringFocusedColor,tempedges,<>onRemove,<>onChangeParent,<>onAfterChangeParent,
	<>userDrawFunction,downtmp,<>closeRect,<>detRect,<lock=false,clicks=0,deletelock=true;

	*new {  arg tabbedView,label="label",index,scroll=false;
		^super.new.init(tabbedView,label.asString,index,scroll);

	}
	init{arg ...args;
		var tab, calcTabWidth,scroll,rotateRect;
		#tabbedView,label,index,scroll = args;

		// the GUI kit. you need to define this on creation in case of kit switching.
		context = tabbedView.context;
		widget = context.userView.new(tabbedView.view, Rect(10,10,10,10)); //real bounds are set later
		widget.enabled = true;
		widget.focusColor=tabbedView.focusFrameColor;
		userDrawFunction = {|pen,drawRect,tabPosition,followEdges|};

		scroll.if{
			this.view = context.scrollView.new(tabbedView.view).resize_(5)
		}{
			this.view = context.compositeView
			.new(tabbedView.view,tabbedView.view.bounds).resize_(5)
		}; //bounds are set later
		homeView=tabbedView;
		this.refreshEventHandlers;

		^this;
	}

	tabWidth_{arg int;
		tabWidth=int;
		tabbedView.refresh;
	}

	refreshEventHandlers{
		var rotateRect;

		widget.mouseUpAction_({arg view, x, y, modifiers;
			widget.mouseMoveAction=nil;
			if(clicks>1 && tabbedView.lockEdges.not){
				tabbedView.followEdges_(tempedges.not);
				clicks=0;
			};

			closable.value(this).if{
				if (closeRect.containsPoint(Point(x,y) )&& lock.not){ // lock prevents accidental deletion after regular drag
					deletelock.not.if{ // deletelock prevents accidental deletion after right cklick detach
						{this.remove}.defer(0.05);
					}
				};
			};
			deletelock=true; // deletelock prevents accidental deletion after right cklick detach
			lock=false; // lock prevents accidental deletion after regular drag
			tempView.notNil.if{tempView.remove;tempView=nil;tabbedView.refresh};
		});




		widget.mouseDownAction_({ |v,x,y,modifiers,clickCount|
			// rightClick Detach
			widget.mouseMoveAction.isNil.if{
				this.refreshMouseMoveHandler;
			};
			(context.name==\SwingGUI).if{clickCount=clickCount-1};
			(clickCount.booleanValue && rightClickDetach.value(this)).if{
				this.detachTab;
			}{

				tempedges = tabbedView.followEdges;
				clicks=clicks+1;
				downtmp=x@y;
				this.focus;
				// this is only for swing, in order to prevent ugly frame.
				tabbedView.unfocusTabs.if{widget.focus(false)};
				closable.value(this).if{  // prepare for deleting
					if (closeRect.containsPoint(Point(x,y) )&& lock.not){ // lock prevents accidental deletion after regular drag
						deletelock=false; // deletelock prevents accidental deletion after right cklick detach
					};
				};


				// clickCount
				if(clicks<2){{{clicks=0;}.defer(0.6);}.fork;};


				// icon Detach
				useDetachIcon.if{
					if (detRect.containsPoint(Point(x,y) )&& lock.not){ // lock prevents accidental deletion after regular drag
						this.detachTab;
					};
				};
			};
		});

		widget.canReceiveDragHandler_({arg view;
			var parents, currentDrag, ret=true;
			currentDrag = GUI.view.currentDrag;
			this.focus(index);
			// this is only for swing, in order to prevent ugly frame.
			tabbedView.unfocusTabs.if{widget.focus(false);true;};
			// Drag between tabs if the GUI Kit allows it

			// If a TabbedViewTab,
			// then reciever may not be a child of the current drag, nor may the the TabbedViews be the same.
			// !important, since this would cause a serious crash
			if(currentDrag.class==TabbedViewTab){
				parents = view.getParents();
				((parents.indexOf(currentDrag.view)).notNil || (view.parent==currentDrag.view.parent)).if{ret=false};
			};
			// All other drag accepted (but a handler must be defined
			ret;
		});
		(context.name==\QtGUI).if
		{this.pr_interTabDragActions}
		{
			widget.beginDragAction_({ this
			});
			widget.receiveDragHandler_({arg v, x,y;
			});
		};

	}

	refreshMouseMoveHandler{
		var rotateRect;
		rotateRect = {arg tempbounds;
			var rect= Rect(downtmp.x,downtmp.y,tempbounds.height,tempbounds.width);
			downtmp=Point(downtmp.y,downtmp.x);
			rect;
		};


		widget.mouseMoveAction=({ |v,x,y,modifiers|
			var tempTabs=[],  receivingindex,f_edges,temprect,tempbounds,center,rect;

			tabbedView.dragTabs.value(tabbedView).if{ // is draggable?

				// dummy view for draging tabs
				tempView.isNil.if{
					tempView = CompositeView.new(tabbedView.view, widget.bounds)
					.background_(labelColor.copy.alpha_(0.4)).visible_(false);
				};

				// create a temporary view as a drag indicator.
				tempView.visible_(true);
				tempView.bounds_(tempView.bounds.moveTo(x-downtmp.x+widget.bounds.left,
					y-downtmp.y+widget.bounds.top) );
				// find a the index to drop the tab
				receivingindex = tabbedView.closestIndexOf(widget.bounds.left+x,widget.bounds.top+y);
				// if you found an index
				if(receivingindex[0].notNil && (receivingindex[0]!=this.index)){
					// first make a new array without the tab
					tabbedView.tabViews.do{arg tab, i;
						if(tab.index != this.index){
							tempTabs=tempTabs.add(tab);
							lock=true;}; // lock for regular dragging (prevents accidental deletion)
					};
					// then insert the tab in the right place
					tabbedView.tabViews=tempTabs.insert(receivingindex.sum,this);
					tabbedView.tabViews.do{arg tab,i; // re-index the tabs
						tab.pr_setIndex(i);
					};
					this.focus(this.index);
					tabbedView.refresh;
				};

				// tab position switching
				tabbedView.lockPosition.value(tabbedView).not.if{
					center = tempView.bounds.center;
					rect=view.bounds;
					switch (true)
					{Rect(rect.width-20,rect.top-10,60,rect.height-20).contains(center)}{
						((tabbedView.tabPosition != \left)
							&& (tabbedView.tabPosition != \right))
						.if{tempView.bounds=rotateRect.value(tempView.bounds)};
						tabbedView.tabPosition_(\right);
					}
					{Rect(rect.left+10,rect.top-40,rect.width-20,60).contains(center)}{
						((tabbedView.tabPosition != \top)
							&& (tabbedView.tabPosition != \bottom))
						.if{tempView.bounds=rotateRect.value(tempView.bounds)};
						tabbedView.tabPosition_(\top);
					}
					{Rect(rect.left+10,rect.height-20,rect.width-20,60).contains(center)}{
						((tabbedView.tabPosition != \top)
							&& (tabbedView.tabPosition != \bottom))
						.if{tempView.bounds=rotateRect.value(tempView.bounds)};
						tabbedView.tabPosition_(\bottom);
					}
					{Rect(rect.left-40,rect.top-10,60,rect.height-20).contains(center)}{
						((tabbedView.tabPosition != \left)
							&& (tabbedView.tabPosition != \right))
						.if{tempView.bounds=rotateRect.value(tempView.bounds)};
						tabbedView.tabPosition_(\left)
					};
				};
			};
		});


	}

	// Drag between tabs
	pr_interTabDragActions{
		widget.beginDragAction_({ this
		});

		widget.receiveDragHandler_({arg v, x,y;
			var ind = tabbedView.closestIndexOf(widget.bounds.left+x, widget.bounds.top+y);
			View.currentDrag.setParent(tabbedView,ind.sum);
		});
	}
	clearEventHandlers{
		//widget.mouseDownAction=nil;
		widget.mouseUpAction=nil;
		widget.mouseMoveAction=nil;
		widget.canReceiveDragHandler=nil;
		widget.receiveDragHandler=nil;
	}

	setParent{ arg newparent, index;
		var tempunfocus = unfocusAction ;
		clicks=0;
		//this.clearEventHandlers;
		index = index ? tabbedView.tabViews.size;
		(context.name==\QtGUI).if{ // Only if kit allows setting parents
			tempView.notNil.if{tempView.remove;tempView=nil};
			if(newparent !=tabbedView){
				onChangeParent.value(this);
				unfocusAction = nil;
				view.setParent(newparent.view);
				widget.setParent(newparent.view);
				tabbedView.pr_removeTab(this.index);
				tabbedView.pr_refreshIndex;
				tabbedView.refresh;
				tabbedView.tabViews.size.booleanValue.not.if{
					tabbedView.window.notNil.if{
						tabbedView.window.close;
					};
				};

				//switch takes place here
				tabbedView=newparent;
				tabbedView.tabViews=tabbedView.tabViews.insert(index,this);

				tabbedView.pr_refreshIndex;
				tabbedView.refresh;
				unfocusAction=tempunfocus;
				this.view.visible=true;
				this.widget.visible=true;
				this.focus;
				this.refreshEventHandlers;
				this.onAfterChangeParent.value(this);
			};
		};
	}

	detachTab{
		(context.name==\QtGUI).if{


			this.setParent(tabbedView.clone(nil,widget.bounds.left,widget.bounds.top));
			// for some reason, this is necessary,  the cloned view does not recieve drags,
			// unless its widget was clicked first. so I clickit once here after a defer.

			{this.widget.mouseDown(4,4);this.widget.mouseMove(0,0);this.widget.mouseUp(0,0)}.defer(0.2);

		};

	}

	pr_removeWidgets{
		widget.remove;
		view.remove;
	}
	pr_setIndex{|i|
		index=i;
	}


	remove{
		onRemove.value(this);
		tabbedView.removeAt(index);

	}

	focus{
		tabbedView.focus(this.index);
	}

	useDetachIcon_{arg boolen=true;
		(context.name==\QtGUI).if{useDetachIcon=boolen}{useDetachIcon=false; "TabbedViewTab:useDetachIcon_ only works under QT GUI".warn};
	}

}

