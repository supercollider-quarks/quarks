Splines
=======

Splines for the [SuperCollider](http://github.com/supercollider/supercollider) music programming environment.

* BezierSpline
    - BezierSplines may have an unlimited number of control points.
* LinearSpline
* BSpline

These are representations of the mathematical defintion of splines (a collection of Points) with supporting functions for performing the interpolations.  Unlike Envelopes they are not limited to left-to-right time representations.

Uses
----

Splines can be used as controllers for parameter automation or sequencing. They may be multi-dimensional and be used to control many parameters with a unified time domain.  States and presets can be stored in multi-dimensional points and interpolated between or sequenced in time.


* SplineGen 
    - kr     - like EnvGen but for Splines
    - ar     - also possible to loop it and run it at audio frequencies
    - readKr - use a position input to freely modulate along the X axis
    - xyKr   - travels along the spine and emits modulation controls for each dimension

* SplineMapper - use a spline as a transfer function or waveshaper. kr/ar
    
* SplineOsc - play a spline as an oscillator. kr/ar

Mx also includes SplineFr that plays a Spline at a frame rate on the client and sends controls to the server.  This class might be moved into splines.


GUI
---

Spline GUIs may be placed on any window or view and even on another UserView. They can be zoomed in the X dimension for time-domain splines (for instance automation curves).

Double click to create a new point. Click a point to select it, hit delete to delete it.  

Hold down shift-control while moving a point to limit movements to the vertical axis.  Hold down control to limit it to the horizontal.

Bezier Splines have control points — hold down ALT while clicking to create or edit those.

	l = LinearSpline({ { 4.0.rand } ! 2 } ! 9);
	l.gui


	l = LinearSpline({ { 4.0.rand } ! 6 } ! 12);
	v = VectorSplineGui(l).gui(nil,1000@1000);


    // used like a long envelope
    b = LinearSpline( Array.fill(60,{ arg i; [ i,1.0.rand ] }) );
    b.gui(nil,1200@300);
    {
    	PinkNoise.ar * SplineGen(b).kr(doneAction:2)
    }.play
    
    // travel along the spine of the spline outputting an x and a y control
    {
	    # f , w = SplineGen(b,0,loop:true).xyKr(MouseY.kr(0.1,20),32);
	    Pulse.ar(f.clip(40,500),w.clip(0.0,1.0),0.4).dup
    }.play
    
* VectorSplineGui - for editing multi-dimensional splines that consist of a series of vectors — each point is a vector of parameters.  It interperets a spline as an ordered series of vectors.

