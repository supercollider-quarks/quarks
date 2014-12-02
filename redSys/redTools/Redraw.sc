//redFrik

Redraw {
	var <win, <mouse, mod, pmouse, userView, strokeColor, width;
	*new {|bounds|
		^super.new.initRedraw(bounds);
	}
	initRedraw {|bounds|
		bounds= bounds ?? {Rect(300, 300, 500, 500)};
		win= Window("redraw", bounds, false);
		userView= UserView(win, Rect(0, 0, bounds.width, bounds.height));
		userView.clearOnRefresh= false;
		userView.mouseDownAction= {|v, x, y, m|
			mod= m;
			if((mod.bitAnd(131072)==131072).not, {	//if not shift
				pmouse= nil;
			});
			mouse= Point(x, y);
			userView.refresh;
		};
		userView.mouseMoveAction= {|v, x, y, m|
			mouse= Point(x, y);
			userView.refresh;
		};
		userView.mouseUpAction= {|v, x, y, m|
			mouse= Point(x, y);
			userView.refresh;
		};
		userView.drawFunc= {
			Pen.width= width;
			Pen.strokeColor= strokeColor;
			if(pmouse.notNil, {
				if(pmouse==mouse, {
					Pen.addRect(Rect.aboutPoint(mouse, 0.5, 0.5));
				}, {
					case
						{mod.bitAnd(262144)==262144} {//ctrl
							Pen.addOval(Rect.fromPoints(mouse, pmouse));
						}
						{mod.bitAnd(524288)==524288} {//alt
							Pen.addRect(Rect.fromPoints(mouse, pmouse));
						}
						{						//no mod
							Pen.line(pmouse, mouse);
						};
				});
			});
			Pen.stroke;
			pmouse= mouse;
		};
		this.strokeColor= Color.black;
		this.background= Color.white;
		this.width= 1;
		win.front;
	}
	background_ {|col|
		userView.background= col;
	}
	strokeColor_ {|col|
		strokeColor= col;
	}
	width_ {|val|
		width= val;
	}
	clear {
		userView.clearDrawing;
		userView.refresh;
	}
	close {
		if(win.isClosed.not, {win.close});
	}
}
