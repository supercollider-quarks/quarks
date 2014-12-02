// A gentle present by Sciss


ImageView {
	var view, <path, jObject, jIcon, jImage, fSetPath, fReload;

	*new { arg parent,Êbounds;
		^super.new.prInit( parent, bounds );
	}
	
	prInit { arg parent, bounds;
		if( GUI.current.id === \cocoa, {
			view = \SCMovieView.asClass.new( parent, bounds );
				view.showControllerAndAdjustSize(false, true);
			fSetPath = { arg newPath; view.path = newPath; view.refresh };
			fReload = { fSetPath.value( path )};
		}, {
			jObject =  \JavaObject.asClass.new( 'javax.swing.JLabel' );
			view = \JSCPlugView.asClass.new( parent, bounds, jObject );
			jIcon = \JavaObject.asClass.new( 'javax.swing.ImageIcon' );
			jObject.setIcon( jIcon );
			fSetPath = { arg newPath; var tk, oldImage;
				oldImage = jImage;
				tk = JavaObject.basicNew( 'toolkit', SwingOSC.default );
				if( newPath.notNil, {
					jImage = JavaObject.newFrom( tk, \createImage, newPath );
				}, {
					jImage = nil;
				});
				jIcon.setImage( jImage );
				if( oldImage.notNil, { oldImage.flush; oldImage.destroy });
				jObject.repaint;
			};
			fReload = {
				if( jImage.notNil, {
					jImage.flush;
					jObject.repaint;
				});
			};
			view.onClose = {
				if( jImage.notNil, {ÊjImage.flush; jImage.destroy });
				jIcon.destroy;
			};
		});
	}
	
	doesNotUnderstand { arg ... args;
		var result = view.perform( *args );
		^if( result == view, this, result );
	}
	
	path_ { arg newPath;
		fSetPath.value( newPath );
		path = newPath;
	}
	
	reload {
		fReload.value;
	}
}   