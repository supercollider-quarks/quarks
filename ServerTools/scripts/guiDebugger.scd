
var mockupChildren,intersects;

~guiDebugger = {
	var g,menu;
	menu = PageLayout.new;
	SimpleLabel(menu,"Select an open window to debug:");
	GUI.window.allWindows.do({ |w|
		SimpleButton(menu.startRow,w.name,{
			var f;
			f = PageLayout("GUI debugger" + w.name,Rect(0,0,1200,6000));
			g.value(w.view,f);
		},200)
	});
	menu.resizeToFit;

	g = { arg view,f, indent=0;
		var oldColor,x;
		f.startRow;
		GUI.staticText.new(f,(25*indent)@17).background_(Color.clear);
		if(view.respondsTo(\background),{
			if(view.isKindOf(SCViewHolder),{
				oldColor = view.background;
			},{
				oldColor = view.background;
			});
			Button(f,Rect(0,0,150,17))
				.states_([ [view.asString, Color.black,Color.white], [view.asString,Color.black,Color.cyan] ])
				.action_({ arg butt;
					if(butt.value == 1,{
						// various views wont be that noticeable
						// some highlight/focus is needed
						view.background = Color.cyan;
						if(view.canFocus ? false,{view.focus});
					},{
						view.background = oldColor ? Color.clear;
					});
				});
			InspButton.icon(view,f);
		},{
			InspButton(view,f);
		});
		x = SimpleLabel(f,view.bounds.asString,100);
		if(view.respondsTo(\parent)
			and: {view.parent.notNil}
			and: {view.parent.absoluteBounds.containsRect(view.absoluteBounds).not},{
				if(view.parent.isKindOf(QScrollView).not or: {view.parent.innerBounds.containsRect(view.absoluteBounds).not},{
					x.background_(Color.red);
					SimpleLabel(f,"View exceeds parent bounds !" + view.parent + view.parent.bounds);
					(view.asString + view.bounds + "bounds exceeds parent !" + view.parent + view.parent.bounds).warn;
				})
		});
		if(view.respondsTo(\children),{
			view.children.do({ |kiddy,i|
				if(i == 0,{ // has at least one
					SimpleButton(f,"Plot children",{
						mockupChildren.value(view)
					});
				});
				g.value(kiddy,f,indent + 1);
			});
		});
	};
};


mockupChildren = { arg view;
	/*  show colored squares (possibly overlapping) for each child of the view */
	var w,o,b;
	b = view.absoluteBounds;
	w = Window(view.asString,b);
	o = b.origin;
	view.children.do { arg child,i;
		var cb,color,ab,name;
		cb = ab = child.absoluteBounds;
		cb.origin = cb.origin - o;
		name = i.asString + child.asString + ab;
		if(view.children.copyRange(0,i-1).any({ arg bro; cb.intersects(bro.absoluteBounds) }),{
			color = Color.red;
			color.alpha = 0.9;
			name = "!!" ++ name + "overlaps previous!!";
			name.warn;
		},{
			color = Color.rand.alpha_(0.4)
		});
		StaticText(w,cb).string_(name).background_(color)
	};
	w.front;
};

~childrenExceedingParents = { arg view,list;
	/*
		returns a list of any children (deep) that exceed the bounds of their parents
		for unit testing purposes.
		use scripts/guiDebugger.scd to investigate further
	*/
	list = list ?? {List.new};
	if(view.respondsTo(\parent)
		and: {view.parent.notNil}
		and: {view.parent.absoluteBounds.containsRect(view.absoluteBounds).not},{
			list.add( view )
	});
	if(view.respondsTo(\children),{
		view.children.do({ |child|
			~childrenExceedingParents.value(child,list)
		});
	});
	list
}

