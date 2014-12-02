// 06/2006 blackrain@realizedsound.net
// made by master blackrain, modified by ixi for aesthetic and functional purposes


XiiVuView : SCViewHolder {
	var userview;
	var <value=0;

	*new{ arg parent, bounds;
		^super.new.initVuView(parent, bounds);
	}
	
	initVuView { arg parent, argbounds;
		var bounds;
		bounds = Rect(0, 0, argbounds.width, argbounds.height);
		this.view_(GUI.userView.new(parent, argbounds))
			.drawFunc_({
				GUI.pen.color = Color.black.alpha_(0.4);
				GUI.pen.width = 1;
				GUI.pen.strokeRect(Rect(bounds.left+0.5, 
					bounds.top+0.5, bounds.width-0.5, bounds.height-0.5));
		
				// center
				//Color.black.alpha_(0.2).set;
				GUI.pen.color = XiiColors.darkgreen;
				GUI.pen.addWedge(bounds.center.x @ (bounds.top + bounds.height - 1), 
					bounds.height * 0.20, 0, -pi);
				GUI.pen.perform(\fill);
		
				// scale
				//Color.black.alpha_(0.2).set;
				GUI.pen.color = XiiColors.darkgreen;
				GUI.pen.addAnnularWedge(bounds.center.x @
					(bounds.top + bounds.height - 1), 
					bounds.height * 0.8, bounds.height * 0.95, -0.75pi, 0.5pi);
				GUI.pen.perform(\fill);
		
				// dial
				GUI.pen.color = Color.black(0.8, 0.8);
				GUI.pen.width = 1;
				GUI.pen.moveTo(bounds.center.x @ (bounds.top + bounds.height - 1));
				GUI.pen.lineTo(Polar.new(bounds.height * 0.95, 
					[-0.75pi, -0.25pi, \linear].asSpec.map(value)).asPoint +
						(bounds.center.x @ (bounds.top + bounds.height)));
				GUI.pen.stroke;
			});
	}	
/*
	
	draw {
		// frame
		
		GUI.pen.color = Color.black.alpha_(0.4);
		GUI.pen.width = 1;
		GUI.pen.strokeRect(Rect(this.bounds.left-0.5, 
			this.bounds.top-0.5, this.bounds.width, this.bounds.height));

		// center
		//Color.black.alpha_(0.2).set;
		GUI.pen.color = XiiColors.darkgreen;
		GUI.pen.addWedge(this.bounds.center.x @ (this.bounds.top + this.bounds.height - 1), 
			this.bounds.height * 0.20, 0, -pi);
		GUI.pen.perform(\fill);

		// scale
		//Color.black.alpha_(0.2).set;
		GUI.pen.color = XiiColors.darkgreen;
		GUI.pen.addAnnularWedge(this.bounds.center.x @
			(this.bounds.top + this.bounds.height - 1), 
			this.bounds.height * 0.8, this.bounds.height * 0.95, -0.75pi, 0.5pi);
		GUI.pen.perform(\fill);

		// dial
		GUI.pen.color = Color.black(0.8, 0.8);
		GUI.pen.width = 1;
		GUI.pen.moveTo(this.bounds.center.x @ (this.bounds.top + this.bounds.height - 1));
		GUI.pen.lineTo(Polar.new(this.bounds.height * 0.95, 
			[-0.75pi, -0.25pi, \linear].asSpec.map(value)).asPoint +
				(this.bounds.center.x @ (this.bounds.top + this.bounds.height)));
		GUI.pen.stroke;
	}
*/

	value_ { arg val;
		value = val;
		this.view.refresh;
	}
	
	/*
	relativeOrigin_ {arg bool;
		userview.relativeOrigin_(bool)
	}
	*/
	
	canFocus_ {arg bool;
		this.view.canFocus_(bool);
	}
	
}
