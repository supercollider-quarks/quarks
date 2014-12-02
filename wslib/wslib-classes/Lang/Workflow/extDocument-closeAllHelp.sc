+ Document {
	*closeAllHelp { 
		Document.openDocuments.select({ |doc|
			(doc.path.asString.dirname.find("help", true) != nil)
			or: (doc.path.asString.basename.find(".help", true) != nil) })
				.do( _.close );
		}
	
	*closeNoPath {
		Document.openDocuments.select({ |doc|
			( doc.path == nil ) && { 
				( (doc.title != ":: scratchpad ::") &&
				(doc.isListener.not))
				&& doc.isEdited.not } })
			.do( _.close );
		}
		
	*closeObsolete { this.closeAllHelp; this.closeNoPath; }
	
	}