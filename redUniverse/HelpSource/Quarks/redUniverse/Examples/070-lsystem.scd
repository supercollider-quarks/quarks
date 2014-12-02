//simple plotting of a lsystem

(
	var width= 300, height= 512, w,
		cnt= 0, lsys, lplot;
	
	//--world
	w= RedWindow("lsystem test", Rect(128, 64, width, height)).front;
	
	//lsys= RedLSystem("F", ($F: "-[-F+F+F]+[+F-F-F]"));
	//lsys= RedLSystem("F", ($F: "F[+F--F]+F"));
	lsys= RedLSystem("F", ($F: "F+F--F+F"));
	5.do{lsys.next};
	lplot= RedLTurtle(lsys, 5, 20);		//length, theta
	
	//--loop
	w.draw{
		cnt= cnt+0.3;		//speed
		lplot.theta= cnt.fold(-90, 90);
		lsys.reset;
		lsys.next;
		lsys.next;
		lsys.next;
		lsys.next;
		
		//--render
		Pen.strokeColor= Color.white;
		Pen.translate(width*0.5, height*0.8);
		lplot.draw;
	};
	w.animate= true;
	CmdPeriod.doOnce({if(w.isOpen, {w.close})});
)