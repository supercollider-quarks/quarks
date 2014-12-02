// wslib 2006-2011

/*
Deprecated as of 02-2013. These methods were used by WFSCollider v1, which has now been replaced by v2. I can't think of any other place where these methods could be useful, as they are merely shortcuts, and also may cause confusion because of their name.
*/

/*
creates a crossfade area from a line. Fades out or fades in when approaching the
center position (default: 0). Creates a value between 0 and 1, for amplitude. 
	center (0): center of the area to fade in / out
	range (0.5): the range/length of the crossfade
	silent (0.25): the area where the fadeOut is silent, fadeIn is 1     
	       
  (fadeOut)
1 _______                          ________ 
         \                        /|
          \                      / |
           \                    /  |
            \       center     /   |
             \        |       /    |
0             \_______|______/     |        
                      |      |     |
                      |silent|range|
                    
fadeIn/fadeOut: linear fade
cosFadeIn/cosFadeOut: cosine (equal power) fade
sqrtFadeIn/sqrtFadeOut: square root (equal power less smooth) fade

examples: 
// a line from -1 to 1 converted to a crossfade with default settings:
// the blue line is the fadeOut

[ (-1,-0.99..1).fadeIn, (-1,-0.99..1).fadeOut ].plot2.superpose_(true);

[ (-1,-0.99..1).cosFadeIn, (-1,-0.99..1).cosFadeOut ].plot2.superpose_(true);

[ (-1,-0.99..1).sqrtFadeIn, (-1,-0.99..1).sqrtFadeOut ].plot2.superpose_(true);

// with different settings (range: 1, silent: 0)
[ (-1,-0.99..1).cosFadeIn( 0, 1, 0 ), (-1,-0.99..1).cosFadeOut( 0, 1, 0 ) ].plot2.superpose_(true);

*/

+ UGen {
		
	fadeOut { |center = 0, range = 0.5, silent = 0.25| // range should not be 0
		this.deprecated( thisMethod );
		^( (this-center).excess( silent ).abs.min( range ) / range );
		}
	
	cosFadeOut { |center = 0, range = 0.5, silent = 0.25| // range should not be 0
		this.deprecated( thisMethod );
		^( this.fadeOut( center, range, silent ) * 0.5pi).sin;
		}
		
	sqrtFadeOut { |center = 0, range = 0.5, silent = 0.25| // range should not be 0
		this.deprecated( thisMethod );
		^this.fadeOut( center, range, silent ).sqrt;
		}
		
	fadeIn { |center = 0, range = 0.5, on = 0.25| // range should not be 0
		this.deprecated( thisMethod );
		^(1 - this.fadeOut( center, range, on ));
		}
			
	cosFadeIn { |center = 0, range = 0.5, on = 0.25|
		this.deprecated( thisMethod );
		^( this.fadeOut( center, range, on ) * 0.5pi).cos;
		}
		
	sqrtFadeIn { |center = 0, range = 0.5, silent = 0.25| // range should not be 0
		this.deprecated( thisMethod );
		^this.fadeIn( center, range, silent ).sqrt;
		}
		
	}
	
+ Number {
		
	fadeOut { |center = 0, range = 0.5, silent = 0.25| // range should not be 0
		this.deprecated( thisMethod );
		^( (this-center).excess( silent ).abs.min( range ) / range );
		}
	
	cosFadeOut { |center = 0, range = 0.5, silent = 0.25| // range should not be 0
		this.deprecated( thisMethod );
		^( this.fadeOut( center, range, silent ) * 0.5pi).sin;
		}
		
	sqrtFadeOut { |center = 0, range = 0.5, silent = 0.25| // range should not be 0
		this.deprecated( thisMethod );
		^this.fadeOut( center, range, silent ).sqrt;
		}
		
	fadeIn { |center = 0, range = 0.5, on = 0.25| // range should not be 0
		this.deprecated( thisMethod );
		^(1 - this.fadeOut( center, range, on ));
		}
			
	cosFadeIn { |center = 0, range = 0.5, on = 0.25|
		this.deprecated( thisMethod );
		^( this.fadeOut( center, range, on ) * 0.5pi).cos;
		}
		
	sqrtFadeIn { |center = 0, range = 0.5, silent = 0.25| // range should not be 0
		this.deprecated( thisMethod );
		^this.fadeIn( center, range, silent ).sqrt;
		}
		
	}

+ SequenceableCollection {
		
	fadeOut { |center = 0, range = 0.5, silent = 0.25| // range should not be 0
		this.deprecated( thisMethod );
		^( (this-center).excess( silent ).abs.min( range ) / range );
		}
	
	cosFadeOut { |center = 0, range = 0.5, silent = 0.25| // range should not be 0
		this.deprecated( thisMethod );
		^( this.fadeOut( center, range, silent ) * 0.5pi).sin;
		}
		
	sqrtFadeOut { |center = 0, range = 0.5, silent = 0.25| // range should not be 0
		this.deprecated( thisMethod );
		^this.fadeOut( center, range, silent ).sqrt;
		}
		
	fadeIn { |center = 0, range = 0.5, on = 0.25| // range should not be 0
		this.deprecated( thisMethod );
		^(1 - this.fadeOut( center, range, on ));
		}
			
	cosFadeIn { |center = 0, range = 0.5, on = 0.25|
		this.deprecated( thisMethod );
		^( this.fadeOut( center, range, on ) * 0.5pi).cos;
		}
		
	sqrtFadeIn { |center = 0, range = 0.5, silent = 0.25| // range should not be 0
		this.deprecated( thisMethod );
		^this.fadeIn( center, range, silent ).sqrt;
		}
		
	}
