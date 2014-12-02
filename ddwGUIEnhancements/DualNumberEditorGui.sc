
DualNumberEditorGUI : NumberEditorGui {
	var <fractsl;
	
	slider { arg layout, x=100,y=15;
		var r;
		slv = GUI.slider.new(layout, Rect(0,0,100,15));
		slv.setProperty(\value,model.spec.unmap(model.int));
		slv.action_({arg th; 
			model.activeValue_(model.intspec.map(th.value)
				+ model.fractspec.map(fractsl.value))
				.changed(slv)
		});

		fractsl = GUI.slider.new(layout, Rect(0,0,100,15));
		fractsl.setProperty(\value,model.spec.unmap(model.fract));
		fractsl.action_({arg th; 
			model.activeValue_(model.intspec.map(slv.value)
				+ model.fractspec.map(th.value))
				.changed(fractsl)
		});		
	}

	update {arg changed,changer; // always has a number box
		if(changer !== numv,{
			{ numv.value_(model.poll); }.defer;
		});
		if(changer !== slv and: {slv.notNil},{
			{	slv.value_(model.intspec.unmap(model.int));
				fractsl.value_(model.fractspec.unmap(model.fract));
			}.defer;
		});
	}

}

NumericRangeGui : ObjectGui {
	var	view;
	guiBody { arg lay, name;
		name.notNil.if({
			GUI.staticText.new(lay, Rect(0, 0, 100, 20)).string_(name);
		});
		view = GUI.rangeSlider.new(lay, Rect(0, 0, 200, 20))
			.action_({ arg v;
				model.lo_(model.spec.map(v.lo));
				model.hi_(model.spec.map(v.hi));
			})
			.lo_(model.spec.unmap(model.lo))
			.hi_(model.spec.unmap(model.hi));
	}
	remove {
		view = nil;
	}
}
