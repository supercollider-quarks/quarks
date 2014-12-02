/*
Problem: font setting does not really work yet on OS X. Seems to work with Post Window though.
*/


HelpDoc {
	
	var <>name, <>segments;
	
	*new { |name, segments|
		^super.newCopyArgs(name, segments);
	}
	
	makeGUI {
		// ...
		var window = Window(name ? "Untitled 01", Rect(40, 40, 510, 400));
		window.addFlowLayout;
		Button(window, Rect(0, 0, 60, 30)).states_([["post doc"]]).action_({ this.makeDoc });
		Button(window, Rect(0, 0, 60, 30)).states_([["post XML"]]).action_({ this.asXML });
		window.view.decorator.nextLine;
		segments.do { |seg| 
			seg.addToGUI(window);
			window.view.decorator.nextLine;
		};
		window.front;
	}
	
	asDict {
		^(category: \helpdoc, name: name, segments: segments.collect(_.asDict))
	}
	
	asXML { |stream|
		stream = stream ? HelpXMLStream; // use a different one later
		stream << "<?xml version=\"1.0\" encoding='UTF-8'?>";
		stream.nl;
		stream.openTag("SCHelp");
		stream.nl;
		segments.do { |seg|
			seg.asXML(stream);
		};
		stream.closeTag("SCHelp");
	}
	
	// replace these with platform specific settings
	
	makeDoc {
		var doc = Document(name ? "Untitled 01");
		segments.do { |seg| seg.addToDoc(doc) };
	}
	
	// todo: write as XML, see XML Quark
	// & write as HTML
	
	*bigFont {
		^this.normalFont.size_(18)
	}
	*boldFont {
		^this.normalFont.boldVariant
	}
	*normalFont {
		^Font("Helvetica", 12);
	}
	*codeFont {
		^Font("Monaco", 9);
	}

}


HelpXMLStream : Post { // change later to CollStream
	classvar <open;
	
	*openTag { |tag|
		this << "<" << tag << ">";
		open = open.add(tag);
	}
	
	*closeTag { |tag|
		var stackTag = open.pop;
		tag !? { 
			if(stackTag != tag) { Error("XML tags don't close properly.").throw }
		};
		this << "</" << stackTag << ">";
	}
}



HelpDocNode {

	// editing gui
	
	addToGUI {
		^this.subclassResponsibility(thisMethod)
	}
	
	addTextField { |parent, property, size = 10|
		var oldVal = this.perform(property);
		// determine optimal size (todo)
		^TextView(parent, Rect(0, 0, 500, 30))
			.string_(oldVal ? "add % here".format(property))
			.keyDownAction_({ |v|
				AppClock.sched(0.01, { this.perform(property.asSetter, v.string); nil })
			});
	}

	// rendering
	
	
	writeAsPlist { |path|
		this.asDict.writeAsPlist(path)
	}
	
	addToDoc { |doc|
		^this.subclassResponsibility(thisMethod)
	}
	
	asDict {
		var res = ();
		this.properties.do { |property| res.put(property, this.perform(property)) };
		^res
	}
	
	addPropertyToXML { |stream, property|
		var value = this.perform(property).asCompileString;
		stream.openTag(property);
		stream.openTag("string");
		stream << value;
		stream.closeTag("string");
		stream.closeTag(property);
		
	}
	
	asXML { |stream|
		this.properties.do { |property| 
			this.addPropertyToXML(stream, property);
			stream.nl;
		}
	}
	
	addString { |doc, string, font|
		font = font ? HelpDoc.normalFont;
		// doc.setFont(font, doc.selectionStart); // still platform specific
		doc.selectedString = string;
		string.postln;
		doc.rangeText(doc.string.size, 0);
	}
	
		
}

HelpDocHeader : HelpDocNode {
	var <>name, <>headline, <>description;
	
	*new { |name, headline, description|
		^super.newCopyArgs(name, headline, description)
	}
	
	properties {
		^#[\name, \headline, \description]
	}
	
	// editing gui
	
	addToGUI { |parent|
		this.properties.do { |property|
			this.addTextField(parent, property);
			parent.view.decorator.nextLine;
		};
	}
	
	// rendering
	
	addToDoc { |doc|
		var superclassstr;
		this.addString(doc, name ++ "\t\t", HelpDoc.bigFont);
		this.addString(doc, headline ++ "\n\n", HelpDoc.boldFont);
		
		if(name.asSymbol.isClassName) {
			superclassstr = name.asSymbol.asClass.superclasses.collect(_.name).join(" : ");
			this.addString(doc, "Inherits from: % \n\n".format(superclassstr), HelpDoc.boldFont);
		};
		
		description !? {
			this.addString(doc, description ++ "\n\n", HelpDoc.normalFont);
		}
	}
	

}

HelpDocExample : HelpDocNode {
	var <>description, <>text;
	
	properties {
		^#[\description, \text]
	}
	
	// editing gui
	
	addToGUI { |parent|
		[\description, \text].do { |property|
			this.addTextField(parent, property);
		};
	}
	
	// rendering
	
	addToDoc { |doc|
		this.addString(doc, description ++ "\n\n", HelpDoc.codeFont);
		this.addString(doc, text ++ "\n\n", HelpDoc.codeFont);
	}

}




