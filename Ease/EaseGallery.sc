/*
EaseGallery.new
*/

EaseGallery {
	*new {
		var width= 790, height= 732;
		var boxWidth= 190, boxHeight= 60;
		var gap= 6;
		var win= Window("EaseGallery", Rect(100, 100, width, height), false).front;
		var usr= UserView(win, Rect(0, 0, width, height));
		var drawBox= {|pos, class ...arguments|
			var rect= Rect(0, 0, boxWidth, boxHeight);
			var arr= Array.fill(boxWidth, {|i| i/(boxWidth-1)});
			var argStr= "("++"".ccatList(arguments).copyToEnd(2)++")";
			Pen.use{
				Pen.translate(pos.x, pos.y);
				Pen.fillColor= Color.white;			//box background colour
				Pen.fillRect(rect);
				Pen.strokeColor= Color.black;		//box border colour
				Pen.strokeRect(rect);
				Pen.fillColor= Color.black;
				Pen.stringCenteredIn(class.name.asString++argStr, rect-Point(0, boxHeight/2));
				Pen.fillColor= Color.blue;			//data line colour
				arr.do{|y, x|
					y= class.value(y, *arguments);	//easing
					Pen.fillRect(Rect.aboutPoint(Point(x, (1-y)*boxHeight), 1, 1));
				};
			};
		};
		usr.background= Color.grey;
		usr.drawFunc= {
			var pos;
			Pen.width= 1;
			
			pos= Point(gap, boxHeight+gap*0+gap);
			drawBox.value(pos, EaseInQuad);
			pos= pos+Point(boxWidth+gap, 0);
			drawBox.value(pos, EaseOutQuad);
			pos= pos+Point(boxWidth+gap, 0);
			drawBox.value(pos, EaseInOutQuad);
			pos= pos+Point(boxWidth+gap, 0);
			drawBox.value(pos, EaseOutInQuad);
			
			pos= Point(gap, boxHeight+gap*1+gap);
			drawBox.value(pos, EaseInCubic);
			pos= pos+Point(boxWidth+gap, 0);
			drawBox.value(pos, EaseOutCubic);
			pos= pos+Point(boxWidth+gap, 0);
			drawBox.value(pos, EaseInOutCubic);
			pos= pos+Point(boxWidth+gap, 0);
			drawBox.value(pos, EaseOutInCubic);
			
			pos= Point(gap, boxHeight+gap*2+gap);
			drawBox.value(pos, EaseInQuart);
			pos= pos+Point(boxWidth+gap, 0);
			drawBox.value(pos, EaseOutQuart);
			pos= pos+Point(boxWidth+gap, 0);
			drawBox.value(pos, EaseInOutQuart);
			pos= pos+Point(boxWidth+gap, 0);
			drawBox.value(pos, EaseOutInQuart);
			
			pos= Point(gap, boxHeight+gap*3+gap);
			drawBox.value(pos, EaseInQuint);
			pos= pos+Point(boxWidth+gap, 0);
			drawBox.value(pos, EaseOutQuint);
			pos= pos+Point(boxWidth+gap, 0);
			drawBox.value(pos, EaseInOutQuint);
			pos= pos+Point(boxWidth+gap, 0);
			drawBox.value(pos, EaseOutInQuint);
			
			pos= Point(gap, boxHeight+gap*4+gap);
			drawBox.value(pos, EaseInSine);
			pos= pos+Point(boxWidth+gap, 0);
			drawBox.value(pos, EaseOutSine);
			pos= pos+Point(boxWidth+gap, 0);
			drawBox.value(pos, EaseInOutSine);
			pos= pos+Point(boxWidth+gap, 0);
			drawBox.value(pos, EaseOutInSine);
			
			pos= Point(gap, boxHeight+gap*5+gap);
			drawBox.value(pos, EaseInExpo);
			pos= pos+Point(boxWidth+gap, 0);
			drawBox.value(pos, EaseOutExpo);
			pos= pos+Point(boxWidth+gap, 0);
			drawBox.value(pos, EaseInOutExpo);
			pos= pos+Point(boxWidth+gap, 0);
			drawBox.value(pos, EaseOutInExpo);
			
			pos= Point(gap, boxHeight+gap*6+gap);
			drawBox.value(pos, EaseInCirc);
			pos= pos+Point(boxWidth+gap, 0);
			drawBox.value(pos, EaseOutCirc);
			pos= pos+Point(boxWidth+gap, 0);
			drawBox.value(pos, EaseInOutCirc);
			pos= pos+Point(boxWidth+gap, 0);
			drawBox.value(pos, EaseOutInCirc);
			
			pos= Point(gap, boxHeight+gap*7+gap);
			drawBox.value(pos, EaseInBounce, 1.70158);
			pos= pos+Point(boxWidth+gap, 0);
			drawBox.value(pos, EaseOutBounce, 1.70158);
			pos= pos+Point(boxWidth+gap, 0);
			drawBox.value(pos, EaseInOutBounce, 1.70158);
			pos= pos+Point(boxWidth+gap, 0);
			drawBox.value(pos, EaseOutInBounce, 1.70158);
			
			pos= Point(gap, boxHeight+gap*8+gap);
			drawBox.value(pos, EaseInBack, 1.70158);
			pos= pos+Point(boxWidth+gap, 0);
			drawBox.value(pos, EaseOutBack, 1.70158);
			pos= pos+Point(boxWidth+gap, 0);
			drawBox.value(pos, EaseInOutBack, 1.70158);
			pos= pos+Point(boxWidth+gap, 0);
			drawBox.value(pos, EaseOutInBack, 1.70158);
			
			pos= Point(gap, boxHeight+gap*9+gap);
			drawBox.value(pos, EaseInElastic, 1, 1);
			pos= pos+Point(boxWidth+gap, 0);
			drawBox.value(pos, EaseOutElastic, 1, 1);
			pos= pos+Point(boxWidth+gap, 0);
			drawBox.value(pos, EaseInOutElastic, 1, 1);
			pos= pos+Point(boxWidth+gap, 0);
			drawBox.value(pos, EaseOutInElastic, 1, 1);
			
			pos= Point(gap, boxHeight+gap*10+gap);
			drawBox.value(pos, EaseInAtan, 15);
			pos= pos+Point(boxWidth+gap, 0);
			drawBox.value(pos, EaseOutAtan, 15);
			pos= pos+Point(boxWidth+gap, 0);
			drawBox.value(pos, EaseInOutAtan, 15);
			pos= pos+Point(boxWidth+gap, 0);
			drawBox.value(pos, EaseNone);
		};
		^win;
	}
}
