
(
a = HelpDoc(
	"HelpDocHeader", 
		[
			HelpDocHeader(
				"HelpDocHeader", 
				"header of a help documentation formatter", 
				"this is without doubt the most beautiful sound ever heard the world."
			)
		]
);
)


a.makeDoc;
a.asDict;
a.asXML;"";
a.makeGUI;

// noop.
// primitive not bound: prSaveAsPlist
a.writeAsPlist("HelpDoc.plist");
unixCmd("open HelpDoc.plist");

