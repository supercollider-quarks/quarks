HadronStateSave
{
	var parentApp;
	
	*new
	{|argParentApp|
		
		^super.new.init(argParentApp);
	}
	
	init
	{|argParentApp|
	
		parentApp = argParentApp;
	}
	
	showSaveDialog
	{		
		File.saveDialog("Save project", {}, {|pathFile| this.saveState(pathFile); });
	}
	
	saveState
	{|argFile|
		var outFile;
		
		parentApp.isDirty = false;
		
		outFile = File(argFile, "w");
		outFile.write("?Hadron 1\n");
		outFile.write("?StartPlugs\n");
		
		parentApp.alivePlugs.do
		({|item|
			outFile.write
			(
				item.class.asString
				++ 31.asAscii 
				++ item.ident 
				++ 31.asAscii 
				++ item.uniqueID.asString 
				++ 31.asAscii
				++ item.extraArgs.asCompileString
				++ 31.asAscii
				++ (item.boundCanvasItem.objView.bounds.left@item.boundCanvasItem.objView.bounds.top).asString
				++ 31.asAscii
				++ item.outerWindow.bounds.asString
				++ 31.asAscii
				++ item.oldWinBounds.asString
				++ 31.asAscii
				++ item.isHidden.asString
				++"\n"
			);
			
			
		});
		
		
		outFile.write("?EndPlugs\n");
		outFile.write("?StartConnections\n");
		
		parentApp.alivePlugs.do
		({|item|
		
			var tempIn, tempOut;
			tempIn = item.inConnections.deepCopy;
			tempOut = item.outConnections.deepCopy;
			
			tempIn.do
			({|inItem, count|
				
				if(inItem[0] != nil,
				{
					tempIn[count][0] = tempIn[count][0].uniqueID;
				});
			});
			
			tempOut.do
			({|outItem, count|
				
				if(outItem[0] != nil,
				{
					tempOut[count][0] = tempOut[count][0].uniqueID;
				});
			});
			
			outFile.write
			(
				item.uniqueID.asString
				++ 31.asAscii
				++ tempIn.asCompileString
				++ 31.asAscii
				++ tempOut.asCompileString
				++ "\n"
			);			
		});
		
		outFile.write("?EndConnections\n");
		
		outFile.write("?StartPlugParams\n");
		
		parentApp.alivePlugs.do
		({|item|
			
			outFile.write(item.uniqueID.asString);
			
			item.giveSaveValues.do
			({|sValue|
				
				outFile.write(31.asAscii);
				outFile.write(sValue.asCompileString.replace("\n", " ")); //.asCompileString injects linefeeds to some stuff don't really know why...
			});
			outFile.write("\n");
		});
		
		outFile.write("?EndPlugParams\n");
		outFile.write("?EndSave\n");
		
		outFile.close;
		
		parentApp.displayStatus("State saved!", 1);
	}
	
}