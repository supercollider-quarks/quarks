+ SCImage
{
	
	*fromSound
	{|argWidth, argHeight, pathToSound|
		
		var sFile = SoundFile.openRead(pathToSound.standardizePath);
		var fArray, tempImage;
		var aSize;
		
		if((argWidth == -1)  or: { argHeight == -1; },
		{
			argWidth = sFile.numFrames.sqrt.asInteger;
			argHeight = argWidth;
		});
		
		aSize = argWidth * argHeight * sFile.numChannels;
		fArray = FloatArray.fill(aSize, { 0; });
		
		sFile.readData(fArray);
		
		fArray = fArray.clump(sFile.numChannels).flop[0];
		
		fArray = fArray ++ FloatArray.fill(((aSize / sFile.numChannels) - fArray.size).round.asInteger, { 0; });
		//[(aSize / sFile.numChannels), fArray.size].postln;
		
		
		
		fArray = fArray.collect({|item| Color.gray((item + 1) * 0.5).asInteger; }).as(Int32Array);
		tempImage = SCImage.color(argWidth, argHeight, Color.red);
		
		tempImage.pixels = fArray;
		fArray = nil;
		^tempImage;		
	}
}

