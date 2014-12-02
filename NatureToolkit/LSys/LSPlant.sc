LSPlant
{
	var <>initHeading, <>pointStack, winBounds, <>angle,
	<>initPosition, <>drawSize, <>headingStack, offspring;
	var win, view, drawRoutine, <>speed, colorFunc;
	var willDraw, penDown;
	
	*new
	{|argStartPoint, argInitHeading, argAngle, argWinBounds, argColor|
	
		^super.new.init
			( 
				argStartPoint, 
				argInitHeading, 
				argAngle, 
				argWinBounds,
				argColor
			);
	}
	
	init
	{|argStartPoint, argInitHeading, argAngle, argWinBounds, argColor|
	
		angle = argAngle ? 22.5;
		initPosition = argStartPoint ? (250@450);
		pointStack = List[initPosition];
		initHeading = if(argInitHeading.notNil, { 360 - argInitHeading; }, { 0 });
		winBounds = argWinBounds ? Rect(300, 300, 500, 500);
		headingStack = List[initHeading];
		drawSize = 8;
		colorFunc = argColor ? Color.white;
		
		willDraw = false;
		penDown = false;
		
		speed = 200;
		
		win = Window("LSPlant", winBounds).onClose_(this.stopDraw);
		view = UserView(win, win.view.bounds)
			.clearOnRefresh_(false)
			.background_(Color.black)
			.drawFunc_
			({
				var dx, dy, nextP;
				
				if(willDraw,
				{
					dx = ((headingStack.last * (pi / 180)).sin * drawSize).round;
					dy = ((headingStack.last * (pi / 180)).cos * drawSize).round;
					nextP = (pointStack.last.x - dx)@(pointStack.last.y - dy);
					if(penDown,
					{
						Pen.color = colorFunc.value;
						Pen.line(pointStack.last, nextP);
						Pen.stroke;
						penDown = false;
					});
					pointStack[pointStack.size - 1] = nextP;
					willDraw = false;
				});
			});
		
		drawRoutine = Routine
			({
				offspring.do
				({|item|
				
					block
					({|break|
					
						//item.postln;
						//pointStack.postln;
						if((item >= $A) and: { item <= $Z; },
						{
							willDraw = true;
							penDown = true;
							view.refresh;
							speed.reciprocal.wait;
							break.value;
						});
						
						if((item >= $a) and: { item <= $z; },
						{
							willDraw = true;
							penDown = false;
							view.refresh;
							speed.reciprocal.wait;
							break.value;
						});
							
						item.switch
						(
							$+,
							{
								headingStack[headingStack.size - 1] = 
									(headingStack.last + angle);
							},
							$-,
							{
								headingStack[headingStack.size - 1] = 
									(headingStack.last - angle);
							},
							$[, //][
							{
								pointStack.add(pointStack.last);
								headingStack.add(headingStack.last);
							},
							$],
							{
								pointStack.removeAt(pointStack.size - 1);
								headingStack.removeAt(headingStack.size - 1);
							}
						);		
											
					});
				});
			});
		
		//win.front;		
	}
	
	draw
	{|argStr|
	
		if(argStr.notNil,
		{
			win.front;
			offspring = argStr;
			drawRoutine.play(AppClock);
		});
			
		
	}
	
	stopDraw
	{
		drawRoutine.stop;
		drawRoutine.reset;
	}
	
	pauseDraw
	{
		drawRoutine.stop;
	}
}