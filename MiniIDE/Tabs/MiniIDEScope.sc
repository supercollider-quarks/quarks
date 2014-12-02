/*
MiniIDEScope
by Jonathan Siemasko
Contact: schemawound@yahoo.com
Homepage: http://schemawound.com/
*/

MiniIDEScope : MiniIDETab{
	var scope, phaseScope, freq, meter;
	var cmdPeriodDelay = 0.5; //delay to fix issue with scope restarting only 50% of the time.
	var restartScopesFunc;
	classvar <tabLabel = \Scope;

	*new{|bounds|
		^super
			.new(bounds)
		    .init();
	}

	init{
		var freqView, scopeView;
		//Scope restart functions - will restart scopes after a cmdPeriod
		restartScopesFunc = {SystemClock.sched(cmdPeriodDelay,{scope.run; phaseScope.run})};
		CmdPeriod.add(restartScopesFunc);
		//WINDOW
		window
			.onClose_({
			    scope.free;
			    phaseScope.free;
			    meter.free;
			    freq.kill;
			    CmdPeriod.remove(restartScopesFunc);  //If you don't remove this function from CmdPeriod you will get an error during the first CmdPeriod after GUI is closed
		    });
		//VIEW
		view = window.view;
		scopeView = FlowView(View(), bounds:512@250);
		//SCOPE
		scope = Stethoscope
			.new(Server.default, view:scopeView);
		//PHASESCOPE
		phaseScope = Stethoscope
			.new(Server.default, view:scopeView);
		if(phaseScope.isNil == false,  {phaseScope.style_(2)}); //In some cases this scope will no get created correctly, doing this to keep MiniIDE from choking.
		//FREQSCOPE
		freqView = View();
		freq = FreqScopeView
			.new(freqView)
			.active_(true);
		//METERS
		meter = ServerMeterView.new(Server.default, View(), 200@200, 12, 12).view;
		layout = VLayout(scopeView.view, freqView, meter);
	}
}