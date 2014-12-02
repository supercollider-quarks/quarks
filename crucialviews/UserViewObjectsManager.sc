

/*
given an identity dict of objects
this manages selecting, moving, deleting, duplicating, double-clicking and key commands
for gui representations of those objects on a user view

supports standard familiar app behavior like shift-clicking to select multiple object, option-dragging to copy, delete-key to delete

each object is added to the manager along with a renderFunc that will be called to render the object's display.
the Pen will be offset so the renderFunc should draw the object at 0,0


todo:
	selection rectangle
	copy icon not showing
	no copy icon if no copy func
	keydown action
	enable/disable x/y dragging
	other modifiers mouse move action
		so control drag can be used to change size
		
could do zooming as well
but fonts do not scale well

*/


UserViewObjectsManager {

	var <>view,<>bounds;
	var <>onDoubleClick,<>onMoved,<>onDelete,<>onCopy,<>keyDownAction;
	var <>selectedStrokeColor;
	
	var <>objects,<>selected, moving,inAction=false,mouseDownPoint,mouseDownWasAHit=false;
	var stack,selectionRect,copyIcon,ghosts;
	
	*new { arg userView,bounds;
		^super.newCopyArgs(userView,bounds ?? {userView.bounds}).init
	}
	init {
		objects = IdentityDictionary.new;
		stack = List.new;
		selected = List.new;
		selectedStrokeColor = Color.blue;
		bounds = bounds.moveTo(0,0);
	}

	add { arg obj, renderFunc, bounds;
		var uv;
		uv = UVObj(obj,renderFunc,bounds);
		uv.selectedStrokeColor = selectedStrokeColor;
		uv.pen = GUI.pen;
		objects[obj] = uv;
		stack.add( uv );
	}
	remove { arg obj;
		objects.removeAt(obj);
		stack.remove(obj)
	}	
	setBounds { arg obj,bounds;
		objects[obj].bounds = bounds;
	}
	getBounds { arg obj;
		^objects[obj].bounds
	}
	
	isSelected { arg obj; ^selected.includes(obj) }
	select { arg obj;
		selected.add(obj);
		objects[obj].selected = true;
	}
	deselect { arg obj;
		selected.remove(obj);
		objects[obj].selected = false;
	}
	deselectAll {
		selected.do({ arg obj; objects[obj].selected = false });
		selected = List.new;
	}
	
	refresh {
		view.refresh;
	}
	value {
		var sels;
		if(selected.size > 0,{
			GUI.pen.use {
				GUI.pen.alpha = 0.5;
				stack.do { arg uv;
					if(uv.selected,{
						sels = sels.add(uv)
					},{
						uv.value
					})
				};
			};
			sels.do(_.value);
		},{
			stack.do(_.value)
		});
		ghosts.do({arg g; g.value });
		selectionRect.value;
		copyIcon.value;
	}
	
	bindAll {
		view.drawFunc = this;
		view.mouseDownAction = Message(this,'mouseDownAction');
		view.mouseUpAction = Message(this,'mouseUpAction');
		view.mouseMoveAction = Message(this,'mouseMoveAction');
	}
	mouseDownAction { |uvw, x, y,modifiers, buttonNumber, clickCount|
		
		var p;
		p = x@y;
		mouseDownPoint = nil;
		mouseDownWasAHit = false;
		//[p,bounds].debug;
		if(bounds.containsPoint(p).not,{
			^false.debug("out of bounds")
		});
		
		mouseDownPoint = p;
		if(stack.any({ arg uv;
			var hit;
			hit = uv.bounds.containsPoint(p);
			if(hit,{
				if(clickCount == 2,{
					// translate p to relative to obj
					onDoubleClick.value(uv.obj, p - uv.bounds.origin, modifiers, buttonNumber );
				},{
					mouseDownWasAHit = true;
					if(this.isSelected(uv.obj).not,{
						if(selected.notEmpty and: {modifiers.isShift.not},{
							this.deselectAll;
						});
						this.select(uv.obj);
						this.refresh
					})
				})
			});
			hit
		}).not,{
			if(modifiers.isShift.not,{
				this.deselectAll.refresh;
			})
		});
		^true
	}
	mouseUpAction { |uvw, x, y,modifiers|
		var p,vector,newSelection;
		p = x@y;
		if(bounds.containsPoint(p).not or: {mouseDownPoint.isNil},{
			if(inAction,{
				this.resetState;
			});
			^false
		});
		vector = p - mouseDownPoint;
		newSelection = List.new;
		selectionRect = nil;
		if(moving.notNil,{
			if(modifiers.isAlt,{
				selected.do { arg obj;
					var newObj;
					newObj = onCopy.value(obj,vector);
					if(newObj.notNil,{
						newSelection.add(newObj)
					})
				};
				this.deselectAll;
				selected = newSelection;
			},{
				selected.do { arg obj;
					onMoved.value(obj,vector);
				};
			});
			this.resetState;
		});
		this.refresh;
	}
	mouseMoveAction { arg uvw,x,y,modifiers;
		var p,vector,newSelection,sr;
		p = x@y;
		vector = p - mouseDownPoint;
		if(bounds.containsPoint(p).not,{
			if(inAction,{
				this.resetState;
				this.refresh;
			});
			^false
		});
		if(inAction.not,{
			// start action if vector has moved and we are still
			if(vector.rho >= 3,{
				inAction = true;
			})
		});
		if(inAction.not,{
			^false
		});
		if(mouseDownWasAHit,{
			if(moving.isNil,{
				moving = selected.copy;
				ghosts = moving.collect { arg obj;
							var r;
							r = objects[obj].copy;
							r.bounds = r.bounds.moveBy(vector.x,vector.y);
							r.ghost = true;
							r
						};
			},{
				ghosts.do { arg uvObj;
					uvObj.bounds = objects[uvObj.obj].bounds.moveBy(vector.x,vector.y)
				};
			});
		},{
			// draw selection rectangle
			// add/remove items from selection
			// find all objects entering or leaving rect from last mouseMove
			// any of those not in selected, add to selected
			// any of selected not in those, remove from selected
			sr = Rect.fromPoints(mouseDownPoint,p);
			if(selectionRect.isNil,{
				selectionRect = PenCommandList.new;
				selectionRect.add( 'fillColor_', Color(0.5, 0.5, 0.5, 0.2) );
				selectionRect.add( 'strokeColor_', Color(0.5, 0.5, 0.5, 0.4) );
				selectionRect.add( 'addRect', sr);
				selectionRect.add( 'draw',3);
			},{
				selectionRect.list[2] = [ 'addRect', sr ];
			});
					
		});
		if(modifiers.isAlt,{
			if(copyIcon.isNil,{
				copyIcon = PenCommandList.new;
				copyIcon.add( 'stringAtPoint',"+",p + Point(4,4));
			},{
				copyIcon.list[0] = ['stringAtPoint',"+",p + Point(4,4)];
			})
		},{
			copyIcon = nil;
		});
		this.refresh;
//		if not in bounds
//			return
//		inAction = true
//		if selected not empty
//			if moving.isNil
//				put selected in moving 
//				add to ghost list pcl
//			update rects in pcl to objectBounds
//			toggle selection status of any rects entering or leaving since last mouseMove
//				find all objects in rect
//				any of those not in selected, add to selected
//				any of selected not in those, remove from selected
//		else
//			add or set selection rect from mouseDownPoint to current
//			toggle selection status of any rects entering or leaving since last mouseMove
//		set copy icon = is option
	}
	resetState {
		moving = nil;
		inAction = false;
		mouseDownWasAHit = false;
		ghosts = nil;
		copyIcon = nil;
		selectionRect = nil;
	}		
			
				
/*
	mouseDownAction
		check for hits
			if double, fire double
			if single, 
				if selected
					//deselect - wait for mouse up
				else
					if selection not empty and not shift
						deselect all
					select
		else
			if not shift
				deselect all
							
	mouseMoveAction
		if not in bounds
			return
		inAction = true
		if selected not empty
			if moving.isNil
				put selected in moving 
				add to ghost list pcl
			update rects in pcl to objectBounds
			toggle selection status of any rects entering or leaving since last mouseMove
				find all objects in rect
				any of those not in selected, add to selected
				any of selected not in those, remove from selected
		else
			add or set selection rect from mouseDownPoint to current
			toggle selection status of any rects entering or leaving since last mouseMove
		set copy icon = is option
		
	mouseUpAction
		if not in bounds
			// may leave garbage if actions are uncomplete
			if inAction
				remove moving
				remove selection rect from stack
			return
		remove selection rect from stack
		if moving.notNil
			if option
				call onCopy on each object with new bounds
			else
				call onMoved on each object with new bounds
		
	keyDownAction
		if mouse not in focus
			return false
		if delete
			delete selected
			return true
		return false
		
*/
}				
						

UVObj {

	var <>obj,<>renderFunc,<>bounds;
	var <>selected=false,<>ghost=false;
	var <>selectedStrokeColor,<>pen;
	
	*new { arg obj,renderFunc,bounds;
		^super.newCopyArgs(obj,renderFunc,bounds)
	}
	value {
		pen.use {
			if(ghost,{
				pen.alpha = 0.5;
				//pen.setShadow; Qt not yet impemented
			});
			if(selected,{
				pen.strokeColor = selectedStrokeColor;
				pen.strokeRect(bounds.insetBy(-1,-1))
			});
			pen.translate(bounds.origin.x,bounds.origin.y);
			renderFunc.value;
		}
	}
}
			
