CoverMe 
{
	classvar <>backupsFolder, <activeDocs, <boundFiles, <covering, kdFunc;
		
	//writes a backup file each time code is ran via cmd-c
	//the folder for written files is ~/Documents/SCBackups/MM-DD-YY 
	//and never cleaned by this class. The backup file names have a random
	//number appended to them. Files with same names are supported.
	//Implemented with reference from the clean implementation of the
	//AutoBackup class by Wouter Snoei.
	//Batuhan Bozkurt 2009
	
	*initClass
	{
		activeDocs = List.new;
		boundFiles = List.new;
		covering = false;
		backupsFolder = "~/Documents/SCBackups/" ++ Date.getDate.format("%m_%d_%Y") ++"/";
	}
	
	*start
	{
		if(PathName(backupsFolder).isFolder.not,
		{//"folder not present, creating...".postln;
			("mkdir"+ backupsFolder).unixCmd;
		});
		
		if(covering == false, {
		kdFunc = {
			arg doc, char, mod, unicode, keycode;
			var fileIndex, file, randomName;
			
			if((char.ascii == 3) and: ((mod == 262401) or: (mod == 2097408)), //if cmd-c or enter
			{//"writing...".postln;
				if(activeDocs.includes(doc).not,
				{
					randomName; //"adding file to bucket...".postln;
					activeDocs.add(doc);
					randomName = (doc.title.basename + rrand(10000, 999999)).replace(" ", "_");
					//randomName.postln;
					while(
					{ 
						boundFiles.includes(randomName) or: 
						PathName(backupsFolder ++ randomName ++ ".sc").isFile; 
					}, 
					{ randomName = 
						(doc.title.basename + rrand(10000, 999999)).replace(" ", "_"); });
					boundFiles.add(randomName);
				});
				
				fileIndex = activeDocs.indexOf(doc);
				file = File((backupsFolder ++ boundFiles[fileIndex] ++ ".sc").standardizePath, "w");
				file.write(doc.string);
				file.close;						
			});
		};
		
		Document.globalKeyDownAction = Document.globalKeyDownAction.addFunc(kdFunc); 
		});
		
		covering = true;
		"Automatic backup system activated...".postln;
		
	}
	
	*stop
	{
		if(covering, 
		{ 
			Document.globalKeyDownAction = Document.globalKeyDownAction.removeFunc(kdFunc); 
		});
		covering = false;
		"Automatic backup system deactivated...".postln;
	}
	
	*showFiles
	{
		("open" + backupsFolder.standardizePath).unixCmd;
	}
}