
/*
adding inlet/outlet patching functionality to BoxMatrix 

each box can have an inlets and outlets list:

	outlets:
		(name: color: beginDrag: )
	inlets:
		(name: color: canReceiveDrag: receiveDrag:)

*/
/*
		
BoxPatchingMatrix : BoxMatrix {
	
	var draggingOutlet;
	var ioHeight = 10;
	
	startDrag { arg x,y;
		var dp,b,box,outletArea,outlets,outlet;
        dp = this.boxPoint(x,y);
        box = this.getBox(dp);
        draggingXY = x@y;
        b = this.getBounds(dp);
        outletArea = Rect.newSides( b.left, b.bottom - ioHeight, b.right,b.bottom);
        if(outletArea.containsPoint(x@y),{
	        outlets = box['outlets'] ? [];
	        if(outlets.size > 0,{
		        outlet = ((x - outletArea.left).asFloat / (outletArea.width.asFloat / outlets.size)).floor.asInteger;
				draggingOutlet = [outlets[outlet]['beginDrag'].value(),dp,outlet];
				focusedPoint = nil;
		        this.view.refresh;
				^this
	        })
        });
		draggingPoint = dp;      
        this.transferFocus(draggingPoint);
        this.view.refresh;
	}
    receiveBoxDrag { arg toBoxPoint,modifiers;
	    if(draggingOutlet.notNil,{
		    box = this.getBox(toBoxPoint);
		    b = this.getBounds(toBoxPoint);
			inletArea = Rect.newSides( b.left, b.top,b.right,b.top + ioHeight);
		    inlets = box['inlets'] ? [];
		    if(inlets.size > 0,{
			    inlet = ((x - inletArea.left).asFloat / (inletArea.width.asFloat / inlets.size)).floor.asInteger;
			    # draggedObject, fromBoxPoint, outlet = draggingOutlet;
			    inlets[inlet]['receiveDrag'].value( draggedObject, fromBoxPoint,outlet );
			    // mark it patched
				^this
		    })						    		
	    });
	    // not an outlet, dragging to the box itself
	    this.handle(toBoxPoint,'onBoxDrag',[draggingPoint,modifiers]);
        draggingPoint = draggingOutlet = nil;
        this.transferFocus(toBoxPoint);
    }
    onOutletDrag_ { arg func;
        this.setHandler('onBoxDrag',{ arg toBox,draggingPoint,modifiers;
	         func.value(this.at(this.boxPoint(draggingPoint.x,draggingPoint.y)),toBox,modifiers,draggingPoint) 
	    });
        if(dragOn == inf,{
            dragOn = 4
        });
    }
    
}
*/

