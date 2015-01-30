/*
    Unit Library
    The Game Of Life Foundation. http://gameoflife.nl
    Copyright 2006-2011 Miguel Negrao, Wouter Snoei.

    GameOfLife Unit Library: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    GameOfLife Unit Library is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with GameOfLife Unit Library.  If not, see <http://www.gnu.org/licenses/>.
*/

+ Rect {
	asControlInput { ^this.asArray }
	
	positiveExtent {
		if( width.isNegative ) {
			left = left + width;
			width = width.neg;
		};
		if( height.isNegative ) {
			top = top + height;
			height = height.neg; 
		};
	}
	
	linlin { |inMin, inMax, outMin, outMax, clip|
		^Rect.fromPoints( 
			this.leftTop.linlin( inMin, inMax, outMin, outMax ),
			this.rightBottom.linlin( inMin, inMax, outMin, outMax )
		);
	}
	
	clip { |rect|
		var current;
		rect = rect.asRect;
		current = this.copy;
		current.leftTop = this.leftTop.clip( rect.leftTop, rect.rightBottom );
		current.rightTop = this.rightTop.clip( rect.leftTop, rect.rightBottom );
		current.rightBottom = this.rightBottom.clip( rect.leftTop, rect.rightBottom );
		current.leftBottom = this.rightBottom.clip( rect.leftTop, rect.rightBottom );
		^current;
	}
	
	centeredWidth_ { |wd|
		var center;
		center = this.center;
		left = center.x - (wd/2);
		width = wd;
	}
	
	centeredHeight_ { |hg|
		var center;
		center = this.center;
		top = center.y - (hg/2);
		height = hg;
	}
	
	centeredExtent_ { |extent|
		var center;
		center = this.center;
		left = center.x - (extent.x/2);
		top = center.y - (extent.y/2);
		width = extent.x;
		height = extent.y;
	}
	
	realLeft_ { |newLeft|
		var currentLeft;
		currentLeft = this.left;
		left = newLeft;
		width = width + (currentLeft - left);
	}
	
	realRight_ { |newRight|
		var currentRight;
		currentRight = this.right;
		width = width + (newRight - currentRight);
	}
	
	realTop_ { |newTop|
		var currentTop;
		currentTop = this.top;
		top = newTop;
		height = height + (currentTop - top);
	}
	
	realBottom_ { |newBottom|
		var currentBottom;
		currentBottom = this.bottom;
		height = height + (newBottom - currentBottom);
	}
	
	leftTop_ { |point|
		this.realLeft = point.x;
		this.realTop = point.y;
	}
	
	rightTop_ { |point|
		this.realRight = point.x;
		this.realTop = point.y;
	}
	
	rightBottom_ { |point|
		this.realRight = point.x;
		this.realBottom = point.y;
	}
	
	leftBottom_ { |point|
		this.realLeft = point.x;
		this.realBottom = point.y;
	}
	
}