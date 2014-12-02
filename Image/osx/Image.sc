/*
Image class - Wrapper around the NSImage class using the SCNSObject Bridge
require : Cocoa Bridge additions
*/

Size {
	var <>width, <>height;
}

ImageType {
	*tiff {^0;}
	*bmp {^1;}
	*gif {^2;}
	*jpeg {^3;}
	*png {^4;}
	*jpeg2000 {^5;} // carefull: supported only in 10.4
}

Compositing {
	*clear {^0;}
	*copy {^1;}
	*sourceIn {^2;}
	*sourceOver {^3;}
	*sourceOut {^4;}
	*sourceATop {^5;}
	*destinationOver {^6;}
	*destinationIn {^7;}
	*destinationOut {^8;}
	*destinationATop {^9;}
	*xor {^10;}
	*darker {^11;}
	*highlight {^12;}
	*lightPlus {^13;}
}

SCNSProxy {
	var scns_obj;
	
	object_ {|obj|
		if(obj.isKindOf(SCNSObject), {scns_obj=obj});
	}
	
	object {
		^scns_obj;
	}
	
	doesNotUnderstand {arg sel...args;
		var result;
		if(args.notNil and:{args.size > 0}, {
			result = scns_obj.invoke(sel.asString.replace("_", ":") ++ ":", args);
		}, {
			result = scns_obj.invoke(sel.asString.replace("_", ":"), nil);
		});
		
		if(result.isKindOf(SCNSObject), {
			if(result === scns_obj, {^this}, {^SCNSProxy.new.object_(result)});
		});
		^result;
	}
	
	/*
	release {
		scns_obj.release;
	}
	*/
}

ImageRep : SCNSProxy {
	
	*newFrom {|aSCNSObject|
		^super.new.initImageRep(aSCNSObject);
	}
	
	initImageRep {|aSCNSObject|
		this.object_(aSCNSObject);
	}
	
	width {
		^scns_obj.invoke("pixelsWide");
	}
	
	height {
		^scns_obj.invoke("pixelsHigh");
	}
	
	hasAlpha {
		^scns_obj.invoke("hasAlpha"); // only for Bitmap Images so
	}
	
	setAlpha {
		|aBol|
		^scns_obj.invoke("setAlpha", [aBol]); // only for Bitmap Images so
	}
	
	isOpaque {
		^scns_obj.invoke("isOpaque"); // only for Bitmap Images so
	}
	
	bitsPerSample {
		^scns_obj.invoke("bitsPerSample");
	}
	
	bounds {
		^Rect(0,0,this.width, this.height);
	}
	
	drawAtPoint {
		|aPoint|
		Pen.use {
			Pen.translate(aPoint.x, this.height+aPoint.y);
			Pen.scale(1.0, -1.0);
			scns_obj.invoke("draw");
		}
	}
	
	drawInRect {
		|argRect|
		Pen.use {
			Pen.translate(0, argRect.height);
			Pen.scale(1.0, -1.0);
			scns_obj.invoke("drawInRect:", [argRect]);
		}
	}
	
	/*
	type {
		
	}
	*/
	
	prRelease {scns_obj.release;}
}

Image : SCNSProxy {
	classvar <all;
	var <path=nil, reps, <isCached=false, <>autoTransform=true;
	
	*initClass {
		all = IdentityDictionary.new;
	}
	
	*releaseAll {
		all.asArray.do {|object|
			this.remove(object);
		}
	}
	
	*numberOfImages {
		^all.size;
	}
	
	*add {|image|
		all.put(image.path.asSymbol, image);
	}
	
	*remove {|image|
		var entry = all.at(image.path.asSymbol);
		if(entry.notNil, {
			entry.prCleanup;
			all.removeAt(image.path.asSymbol);
		});
	}
	
	*get {|path|
		^all.at(path.asSymbol);
	}
	
	*newClear {
		|size|
		if(size.isKindOf(Rect), {
			size = Size(size.width, size.height);
		});
		if(size.notNil, {
			"new cleared image".postln;
			^super.new.initFromSize(size);
		},{
			^nil;
		});
	}
	
	*new {
		|path|
		if(path.size <= 0 or: {path.isNil},{
			"bad arguments for image".postln;
			^nil;
		}, {
			^super.new.initFromFile(path);
			//^super.new.testPath(argPath);
		});
	}
	
	saveAs {
		|argPath, type|
		var path, ns_path, ns_data, ns_image, result=nil;
		path = argPath.asString;
		ns_path = SCNSObject("NSString", "initWithCString:length:", [path, path.size]);
		
		this.lockFocus;
		ns_image = SCNSObject("NSBitmapImageRep", "initWithFocusedViewRect:", [Rect(0,0,this.width, this.height)]);
		this.unlockFocus;
		
		ns_data = ns_image.invoke("representationUsingType:properties:", [type, nil]);
		result = ns_data.invoke("writeToFile:atomically:", [ns_path, true]);
		ns_data.release;
		^(result.ascii == 1);
	}
	
	flipped {
		var newImage = Image.newClear(this.width@this.height), t_ = autoTransform;
		newImage.lockFocus;
		
		Pen.use {
			autoTransform = true;
			this.drawAtPoint(0@0, operation:Compositing.sourceIn);
			autoTransform = t_;
		};
		newImage.unlockFocus;
		^newImage;
	}
	
	initFromSize {
		|argSize|
		var image, cache;
		
		image = SCNSObject("NSImage", "initWithSize:", [argSize]);
		cache = SCNSObject("NSCachedImageRep", "initWithSize:depth:separate:alpha:", [argSize, 0, true, true]);
		
		image.invoke("addRepresentation:", [cache]);
		cache.release;
		
		reps = List.new;
		scns_obj = image;
		isCached = true;
		this.recache;
		
		this.prinit;
	}
	
	initFromFile {
		|argPath|
		
		var ns_string, ns_image, ns_bitdata, ns_bitmap, ns_url;
		
		reps = List.new;
		path = argPath.asString;
		
		if(
			path.beginsWith("http://") ||
			path.beginsWith("file:///") ||
			path.beginsWith("ftp://")
		,{
			("loading image at url: " ++ path).postln;
			ns_string = SCNSObject("NSString", "initWithCString:length:", [path, path.size]);
			ns_url = SCNSObject("NSURL", "initWithString:", [ns_string]);
			ns_image = SCNSObject("NSImage", "initWithContentsOfURL:", [ns_url]);
			ns_url.release;
		}, 
		 {
		 	if(path.beginsWith("/").not, {path = Document.current.path.dirname ++ "/" ++ path});
		 	if(File.exists(path).not, {
		 		("File does not exists at path: " ++ path).warn;
		 		^nil;
		 	});
		 	
		 	("loading image at systempath: " ++ path).postln;
		 	ns_string = SCNSObject("NSString", "initWithCString:length:", [path, path.size]);
		 	ns_image = SCNSObject("NSImage", "initWithContentsOfFile:", [ns_string]);
		});
		ns_string.release;
		ns_bitdata = ns_image.invoke("TIFFRepresentation"); // ensure bitmap data
		ns_bitmap = SCNSObject("NSBitmapImageRep", "initWithData:", [ns_bitdata]);
		
		ns_image.release;
		ns_image = SCNSObject("NSImage", "init");
		ns_image.invoke("addRepresentation:", [ns_bitmap]);
		
		ns_bitdata.release;
		ns_bitmap.release;
		
		scns_obj = ns_image;		
		this.recache;
		this.prinit;
	}
	
	prinit {
		var ns_rep, max;
		
		ns_rep		= scns_obj.invoke("representations");
		max 			= ns_rep.invoke("count");
		
		// getting all representations
		if(max.notNil and:{max > 0}, {
			max.do {|idx|
				reps.add(ImageRep.newFrom(ns_rep.invoke("objectAtIndex:", [idx])));
			}
		});
		
		ns_rep.release;  
		this.prAdd;
	}
	
	/*
	initFromFile {
		arg argPath;
		var ns_string, ns_rep, max;
		
		reps			= List.new;
		path 		= argPath;
		ns_string 	= SCNSObject("NSString", "initWithCString:length:", [path, path.size]);
		scns_obj 		= SCNSObject("NSImage", "initWithContentsOfFile:", [ns_string]);
		ns_rep		= scns_obj.invoke("representations");
		max 			= ns_rep.invoke("count");
		
		// getting all representations
		if(max.notNil and:{max > 0}, {
			max.do {|idx|
				reps.add(ImageRep.newFrom(ns_rep.invoke("objectAtIndex:", [idx])));
			}
		});
		
		//clean
		ns_string.release;
		ns_rep.release;
		
		this.prAdd(this);
	}
	*/
	
	
	release {	
		this.prRemove(this);
	}
	
	bounds {
		^reps.at(0).bounds;
	}
	
	numRepresentations {
		^reps.size;
	}
	
	representationAt {
		|idx|
		^reps.at(idx.max(0).min(reps.size-1));
	}
	
	width {
		^reps.at(0).width;
	}
	
	height {
		^reps.at(0).height;
	}
	
	hasAlpha {
		^reps.at(0).alpha;
	}
	
	setAlpha {
		reps.at(0).setAlpha(true);
	}
	
	isOpaque {
		^reps.at(0).isOpaque;
	}
	
	bitsPerSample {
		^reps.at(0).bitsPerSample;
	}
	
	bitsPerPixel {
		^reps.at(0).bitsPerPixel;
	}
	
	drawAtPoint {
		arg aPoint, fraction=1.0, operation=Compositing.copy;
		Pen.use {
			if(autoTransform, {
				Pen.translate(aPoint.x, aPoint.y + this.height);
				Pen.scale(1, -1.0);
			});
			scns_obj.invoke("drawAtPoint:fromRect:operation:fraction:", [Point(0, 0), Rect(0, 0, 0, 0), operation, fraction.asFloat]);
		}
	}
	
	drawInRect {
		arg aRect, fraction=1.0, operation=Compositing.copy;
		Pen.use {
			if(autoTransform, {
				Pen.translate(0, aRect.height);
				Pen.scale(1, -1.0);
			});
			scns_obj.invoke("drawInRect:fromRect:operation:fraction:", 
				[aRect, Rect(0, 0, 0, 0), operation, fraction.asFloat]
			);
		}
	}
	
	drawRegionAtPoint {
		arg aPoint, aRegion=nil, fraction=1.0, operation=Compositing.copy;
		Pen.use {
			if(aRegion.isNil, 
			{
				aRegion = Rect(0,0,this.width,this.height)
			});
			if(autoTransform, {
				Pen.translate(aPoint.x, aPoint.y + aRegion.height);
				Pen.scale(1.0, -1.0);
				aRegion.top = this.height - aRegion.top - aRegion.height;
			});
			scns_obj.invoke("drawInRect:fromRect:operation:fraction:", 
				[Rect(0, 0, aRegion.width, aRegion.height), aRegion, operation, fraction.asFloat]
			);
		}
	}
	
	/* not done
	drawRegionInRect {
		arg aRect, aRegion=nil, fraction=1.0, operation=Compositing.copy;
		Pen.use {
			if(aRegion.isNil, 
			{
				aRegion = Rect(0,0,this.width,this.height)
			});
			
			if(aRect.isNil,
			{
				aRect = aRegion.copy;
			});
			
			if(autoTransform, {
				Pen.translate(aRect.left, aRect.top + aRegion.height);
				Pen.scale(1.0, -1.0);
				aRegion.top = this.height - aRegion.top - aRegion.height;
				scns_obj.invoke("drawInRect:fromRect:operation:fraction:", 
					[Rect(0,0,aRect.width, aRect.height), aRegion, operation, fraction.asFloat]
				);
			},{
				scns_obj.invoke("drawInRect:fromRect:operation:fraction:", 
					[aRect, aRegion, operation, fraction.asFloat]
				);
			});
		}
	}
	*/
	
	drawStringAtPoint {
		arg string, point, font, color;
		var r;
		if(string.isNil, {^this});
		r = string.bounds;
		Pen.use {
			Pen.translate(0, this.height);
			Pen.scale(1,-1);
			point.y = this.height - point.y;
			string.drawAtPoint(point, font, color);
		}
	}
	
	/* 
	//probably not a good idea 
	cmdPeriod {
		this.class.releaseAll;
	}
	*/
	
	/// private
	prCleanup {
		"clean up image".postln;
		reps.do {|r|r.release;};
		scns_obj.release;
	}
	
	prAdd {
		this.class.add(this);
	}
	
	prRemove {
		this.class.remove(this);
	}

}