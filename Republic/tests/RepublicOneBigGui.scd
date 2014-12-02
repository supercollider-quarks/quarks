// An option for having most of the guis for a Republic performance 
// in a single big window - for discussion, and just a visual dummy atm.

// install first:

Quarks.install("FileListView");

	// just a dummy window: 
(
q = q ? ();
w = Window("Republic mit Alles", Rect(0,100,800,800)).front;
w.addFlowLayout;

	// chat fenster could be smaller.
q.rg = RepublicGui(nil, 8, w);
q.rg.zone.resize_(1);
q.rg.zone.bounds_(q.rg.zone.bounds.height_(500));

q.tg = TdefAllGui(8, parent: w);

q.pg = PdefAllGui(8, parent: w);
q.pg.zone.bounds_(q.pg.zone.bounds.moveTo(510, 5));

Tdef(\next); Tdef(\stop);
Pdef(\ida); Pdef(\knowhere);

	// file list views for selecting files to open - or load.
	// maybe add 3 buttons for default names/places
q.fileZone = CompositeView(w, Rect(0,0, 240, 300));
q.fileZone.bounds_(Rect(0, 500, 240, 300));

	// does nothing interesting yet.
q.fl1 = FileListView(q.fileZone, Rect(0,0,120, 180), "SCClassLibrary");
q.fl2 = FileListView(q.fileZone, Rect(120,0,120, 180), "SCClassLibrary/");


	// server window is still separate
s.makeWindow.window.bounds_(Rect(10,10, 288, 98));

	// HistoryGui too - could change with HistoryRepGui
q.hw = History.makeWin;
// q.hw.w.bounds_(Rect(10, 50, 240, 300));

w.front;


// replace dummy Republic post view with RePost
q.pr = TextView(w, Rect(0,0,500,200));
q.pr.string_("... Republic posts here ...");
q.pr.background_(Color.grey(0.8)).stringColor_(Color.black);

	// example buttons
(
q.butzone.remove;
q.butzone = CompositeView(w, Rect(0,0,500, 20));
q.butzone.addFlowLayout(2@2, 0@0);
q.butzone.bounds_(Rect(240, 415,500, 20));
q.butzone.background_(Color.grey(0.6));
q.pr.bounds_(Rect(240, 210,530,200));

["Synth", "Tdef", "Pdef", "Ndef"].do { |name|
	Button(q.butzone, Rect(0,0,50, 20)).states_([[name]]);
};
	// some utility buttons for the guis on other windows
Button(q.butzone, Rect(0,0,50, 20)).states_([["r.s"]]).action_ { s.makeWindow };
Button(q.butzone, Rect(0,0,50, 20)).states_([["Hist"]]).action_ { q.hw.w.front };
Button(q.butzone, Rect(0,0,50, 20)).states_([["post"]]).action_ { Document.listener.front };
);
q.ed = TextView(w, Rect(0,0,500,200));
q.ed.string_("... Edit code here ...");
q.ed.bounds_(Rect(240, 440, 530, 350));

)
