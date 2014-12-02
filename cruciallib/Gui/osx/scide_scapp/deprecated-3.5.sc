
+ SCCompositeView {

	// shouldnt really do this to an h or v layout
	resizeToFit {
		var r;
		r = Rect(0,0,0,0);
		this.children.do({ |kid|
			r = r.union(kid.bounds)
		});
		this.bounds = r;
	}
}


