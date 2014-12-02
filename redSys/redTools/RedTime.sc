//redFrik

RedTime {
	var <>h, <>m, <>s;
	*new {|h= 0, m= 0, s= 0| ^super.newCopyArgs(h, m, s)}
	*newFromSeconds {|sec|
		var sx, mx, my, hx;
		sx= sec%60;
		my= sec%3600-sx;
		mx= my.div(60);
		hx= (sec-my).div(3600)%24;
		^this.new(hx, mx, sx)
	}
	*newFromMinutes {|min| ^this.newFromSeconds(min*60)}
	*newFromHours {|hour| ^this.newFromMinutes(hour*60)}
	*newLocalTime {^this.new(0, 0, 0).setLocalTime}
	setLocalTime {
		var d;
		d= Date.localtime;
		h= d.hour;
		m= d.minute;
		s= d.second;
	}
	asSeconds {^(h*3600)+(m*60)+s}
	asMinutes {^this.asSeconds/60}
	asHours {^this.asMinutes/60}
	+ {|redTime| ^RedTime.newFromSeconds((this.asSeconds+redTime.asSeconds))}
	- {|redTime| ^RedTime.newFromSeconds((this.asSeconds-redTime.asSeconds))}
	addSec {|sec| ^RedTime.newFromSeconds(this.asSeconds+sec)}
	addMin {|min| ^RedTime.newFromMinutes(this.asMinutes+min)}
	addHour {|hour| ^RedTime.newFromHours(this.asHours+hour)}
	asArray {^[h, m, s]}
	== {|redTime| ^redTime.isKindOf(RedTime) and: {this.asArray==redTime.asArray}}
	printOn {|stream| stream<<"RedTime("<<*[h, m, s]<<")"}
	storeArgs {^[h, m, s]}
}
