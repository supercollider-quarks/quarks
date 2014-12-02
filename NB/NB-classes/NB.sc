NB {

	classvar <methodNames;
	classvar <>verbose = false;			
	var <>name, <>width, <>height, <>publishing, <>path = "/tmp", <>fullpath;
	var fileType = "pdf";				// options: png, gif, tiff, jpeg, jpg, pdf, mov
	var <>frames = 30, <>fps = 30;
	var <>mode = "RGB";							
	var <>pythonString = false; 
	var <>displayLatency = 1;			// min. elapsed time (in secs.) between render and display
	var <>needsTab = false;
	var <>basicPointPrim = "oval";		// options: oval or rect
	
	*new { arg 
			name = "untitled", 
			width = 320, 
			height = 240, 
			publishing = true,
			fileType = "pdf",
			frames = 30,
			fps = 30;
		^super.newCopyArgs(name, width, height, publishing).init(fileType, frames, fps)
	}
	
	init { arg ft, fs, f_p_s;
		var comma = ", ";
		var document;
		var info;	
		var nameWithPy;
		var types;	
		
		types = ["png", "gif", "tiff", "jpeg", "jpg", "pdf", "mov"].collect({ arg each, i;
			each.asSymbol
		});
		if( types.includes(ft.asSymbol).not, { 
			^("ERROR: " ++ ft ++ " is not a supported filetype\n") ++
			("CHOOSE FROM: png, gif, tiff, jpeg, jpg, pdf, or mov")
		}, { fileType = "." ++ ft;
		});
		frames = fs;
		fps = f_p_s;
				
		methodNames = methodNames ?? {
			NB.methods.collect({ arg each, i; each.name }).add(\open);
		};
		
		if(name.contains(".py").not, { nameWithPy = name ++ ".py" });
		fullpath = path ++ "/" ++ nameWithPy;
		document = File.new(fullpath, "w");
		if(fileType != ".mov", {
			document.write( "size(" ++ width ++ comma ++ height ++ ")\n" )
		},{
			document.write(
				"from AppKit import NSApplication\n" ++
				"NSApplication.sharedApplication().activateIgnoringOtherApps_(0)\n" ++
				"\n" ++
 				"from nodebox.graphics import Context\n" ++
				"from nodebox.util import random, choice, grid, files\n" ++
				"from nodebox.util.QTSupport import Movie\n" ++
 				"\n" ++
				"movie = Movie(" ++ 
				(name ++ fileType).asCompileString ++ 
				", " ++ 
				"fps=" ++ 
				fps ++ 
				")\n" ++ 
				"for i in range(" ++ frames ++ "):\n" ++
				"\tctx = Context()\n" ++
				"\tctx.size(" ++ width ++ ", " ++ height ++ ")\n" 
			)
		});
		document.close;	
		// essential!! have to set the environment variable or console.py won't work!!
		"PYTHONPATH".setenv("/Applications/NodeBox/NodeBox.app/Contents/Resources/");
	}
	
	// this adds two strings required at the end of a file when writing movie code
	// this is always the last message BEFORE displaying movie code or rendering movies
	finishMovieCode {
		var document;
		document = File.new(fullpath, "a");
		document.write(
			"\tmovie.add(ctx)\n" ++
			"movie.save()"
		);
		document.close
	}
	
	*test0_NodeBox {
		var n;
		n = NB("test0_NodeBox", 400, 300);		
		n.background(1, 0, 0);			
		n.fill(0, 1, 0);				
		n.oval(100, 64, 100, 200);		
		n.fill(0, 0, 1);
		n.oval(200, 64, 100, 200);
		n.fill(1, 0.5, 1, 0.24);
		n.rect(100, 40, 200, 125);
		n.fill(1, 1, 1);
		n.text("sc3 ++ nodebox", 10, 26);
		n.fontsize(16);
		n.text("SinOsc.ar(440)", 282, 289);
		n.fill(1, 1, 1, 0.05);
		n.rect(0, 0, 50, 300);
		n.oval(0, 267, "HEIGHT", 200);				
		n.displayCode
	}

	*test1_imageDisplay {
		var n;
		n = NB("test1_imageDisplay", 400, 300);		
		n.background(1, 0, 0);			
		n.fill(0, 1, 0);				
		n.oval(100, 64, 100, 200);		
		n.fill(0, 0, 1);
		n.oval(200, 64, 100, 200);
		n.fill(1, 0.5, 1, 0.24);
		n.rect(100, 40, 200, 125);
		n.fill(1, 1, 1);
		n.text("sc3 ++ nodebox", 10, 26);
		n.fontsize(16);
		n.text("SinOsc.ar(440)", 282, 289);
		n.fill(1, 1, 1, 0.05);
		n.rect(0, 0, 50, 300);
		n.oval(0, 267, "HEIGHT", 200);				
		n.render;
		n.displayImage;
	}
	
	*test2_createMovieCode {	
	
		var n, r1, r2, r3;
		var numFrames = 120;
		var framesPerSecond = 30;
		
		// color
		r1 = "random(0.84, 1.0)";
		// rectangle y and text y position
		r2 = "random(20, 300)";
		// rectangle size
		r3 = "random(90, 110)";
		
		n = NB("test2_createMovieCode", 320, 240, true, "mov", numFrames, framesPerSecond);
		n.background(r1, r1, r1, r1);
		n.fill(0, 1, 0);
		n.rect(20, r2, r3, r3 ++ "/2");
		n.fontsize(12);
		n.fill(0, 0, 1);
		n.text("I am a Quicktime movie", 20, r2);
		n.finishMovieCode;
		n.displayCode
	}
	
	*test3_fileFormats {
		var n, r;
		r = "random()";
		["png", "gif", "tiff", "jpeg", "pdf"].collect({ arg item, i;
			n = NB(item ++ "_example", 400, 240, true, item);
			n.background(r, r, r, r);
			n.rect(20, 20, 100, 100);
			n.fontsize("random(10, 30)");
			n.text("I am a file in " ++ item ++ " format.", 5, 200);
			n.render;
			n.displayImage(3);
		});
	}

	// play background (furnishing) music
	// substitute a music rendering engine for the default in the func variable
	*satie { arg dur = 1, func;
	
		if(func.isNil, {
			func = 
				{
				SinOsc.ar([440, 441], 0, 0.2) 
				* 
				EnvGen.kr(Env.perc(0.1, dur - 0.1), 1.0, doneAction: 2)
				}
		});
		
		if( Server.local.serverRunning, 
			{	func.play
		}, { 
			if(verbose, { "A server doesn't appear to be running.".postln });
		})
	}
	
	// getter/setter
	fileType { arg v; 
		var f;
		var types;
		
		if(v.isNil, {
			// getter
			f = fileType.reverse;
			f.pop;
			^f.reverse
		},{
			// setter
			types = ["png", "gif", "tiff", "jpeg", "jpg", "pdf", "mov"].collect({ arg each, i;
				each.asSymbol
			});
			if( types.includes(v.asSymbol).not, { 
				^("ERROR: " ++ v ++ " is not a supported filetype\n") ++
				("CHOOSE FROM: png, gif, tiff, jpeg, jpg, pdf, or mov")
			}, { fileType = "." ++ v;
			});		
		});
	}
	
	// a helper to write lines of code to a file
	translateToPython { arg linesOfCode;
		linesOfCode.do({ arg eachLine, i;
			eachLine.py(this)
		});
	}
	
	// write code to a file 
	publishCode { arg ... linesOfCode;
		this.translateToPython(linesOfCode)
	}
	
	// write code to a file and display it in nodebox
	publishAndDisplayCode { arg ... linesOfCode;
		this.translateToPython(linesOfCode);
		this.displayCode
	}
	
	displayCode { this.display }
	display { ("open -a NodeBox.app " ++ fullpath).systemCmd; }

	render {
		var input;
		var output;
				
		input = fullpath;
				
		output = fullpath ++ 
				" " ++ 
				path ++ 
				"/" ++ 
				name ++ 
				fileType;
				
		// the mov part of the code probably doesn't work .... 
		if(this.fileType == "mov", {
			Pipe("export PYTHONPATH=/Applications/NodeBox/NodeBox.app/Contents/Resources/;
				python console.py " ++ 
				input ++
				" " ++
				output ++
				" " ++
				frames ++ 
				" " ++
				fps,
				"r"
			).close;
			
		}, {
			// this does (seem to) work
			(
			//"python /Applications/SuperCollider/console.py " ++
			"python " ++ NB.filenameSymbol.asString.dirname.dirname.escapeChar( $  )
			  +/+ "console.py " ++
			fullpath ++ 
			" " ++ 
			path ++ 
			"/" ++ 
			name ++ 
			fileType).unixCmd;
		});
		if( verbose, { "A successful render!".postln; });
		^this
	}
	
	renderInBackground {
		var input;
		var output;
				
		input = fullpath ++ 
				" " ++ 
				path ++ 
				"/" ++ 
				name ++ 
				"py";
				
		output = fullpath ++ 
				" " ++ 
				path ++ 
				"/" ++ 
				name ++ 
				fileType;
				
		if(this.fileType == "mov", {
			Pipe("export PYTHONPATH=/Applications/NodeBox/NodeBox.app/Contents/Resources/;
				python console.py " ++ 
				input ++
				" " ++
				output ++
				" " ++
				frames ++ 
				" " ++
				fps,
				"r"
			).close;
			
		}, {
			// this does (seem to) work
			(
			//"python /Applications/SuperCollider/console.py " ++
			"python " ++ NB.filenameSymbol.asString.dirname.dirname.escapeChar( $  )
			  +/+ "console.py " ++
			fullpath ++ 
			" " ++ 
			path ++ 
			"/" ++ 
			name ++ 
			fileType ++
			" &").unixCmd;
		});
		// if( verbose, { "A successful render!".postln; });
		this;
	}

	imageViewer {
		var window;
		var imageSize;
		var theName;
		var thePath;
		var imageView;
		
		theName = name ++ fileType;
		thePath = path ++ "/" ++ theName;
		
		window = 	SCWindow(
			theName, 
			Rect(120, 240, width + 50, height + 50)
		).front;
		
		imageSize = Rect(20, 20, width, height);
		imageView = SCMovieView(
			window, 
			imageSize
		);
		imageView.showControllerAndAdjustSize(false, false).path_(thePath);
	}
	
	displayImageWithSC { this.imageViewer }
	
	displayImage { arg dur;
		var pause;
		
		if(dur.notNil and: {dur.isKindOf(SimpleNumber)}, { 
			displayLatency = dur 
		});
		
		// pause so a race condition doesn't upset image display after image rendering
		pause = Routine({ 
			1.do({ arg i;
				displayLatency.wait;
				("open " ++ path ++ "/" ++ name ++ fileType).unixCmd; 
			}); 
		});
		pause.play(AppClock);
		// reset the instance variable to its previous value which means
		displayLatency = 1;
		^this
	}
	
	// a generic way to view an image or a movie
	openWith { arg imageViewerApp = "Preview", dur;
	
		var pause;
		
		if(dur.notNil and: {dur.isKindOf(SimpleNumber)}, { 
			displayLatency = dur 
		});
		
		// pause so a race condition doesn't upset image display after image rendering
		pause = Routine({ 
			1.do({ arg i;
				displayLatency.wait;
				(
					"open -a " ++ 
					imageViewerApp ++ 
					" " ++ 
					path ++ 
					"/" ++ 
					name ++ 
					fileType
				).unixCmd; 
			}); 
		});
		pause.play(AppClock);
		^this
	}
		
	renderAndDisplayImage { arg dur = 1;
		this.render;
		this.displayImage(1);
	}

	// infrastructure
	
	publish { arg cmdString;
		cmdString = this.addLineFeed(cmdString);
		cmdString = this.addTab(cmdString);
		if(this.fileType == "mov", {
			cmdString = "\t" ++ "ctx." ++ cmdString
		});
		if(publishing, { 
			this.write(cmdString)
		});
		^cmdString
	}
	
	// the next 3 methods are helpers for publish
	addLineFeed { arg cmdString;
		if((cmdString.last == $\n).not, {
			cmdString = cmdString ++ "\n";
		});
		^cmdString
	}
	
	addTab { arg cmdString;
		if(needsTab, { cmdString = "\t" ++ cmdString
		});
		^cmdString
	}
	
	write { arg code;
		var document;
		document = File(fullpath, "a");
		code.do({ arg cmd, i;
			document.write(cmd.value)
		});
		document.close;
	}
	
	// the oracle is a function
	matrix { arg neo, smith, oracle;
		smith.do({ arg thisThing, j;
			neo.do({ arg thatThing, i; 
				oracle.value(i, j) 
			});
		})
	}
	
	*matrixDemo { arg the_name;
		var n, a, g, b, r;
		var box;
		box = { arg x, y;
			n.stroke(1);
			n.beginpath(x, y);
			n.lineto(x + 100, 100 + y);
			n.endpath;
			n.beginpath(x, 100 + y);
			n.lineto(x + 100, 0 + y);
			n.endpath;
			n.fill(1, 0, 0);
			n.oval(45 + x, 45 + y, 10, 10);
			50.rrand(100).do({
				n.fill("random()", "random()", "random()");
				if(0.5.coin, {
					n.point(x + 25.rand2, y + 25.rand2)
				},{
					n.python(["rect", "oval"].choose.cmd(
						x + 30.rand2, 
						y + 30.rand2, 
						10.rand, 
						10.rand
					))
				});
			});
			n.skew("random(-20, 20)", "random(-20, 45)");
			n.fontsize("random(13, 24)");
			n.python(
				"text".cmd(
					["where", "is", "the", "much", "needed", "help", "file", "?"].choose, 
					x + 30, 
					y + 40
				)
			);
			n.reset;
		};
		#r, g, b, a = Array.fill(4, { "random(0, 0.5)" });
		n = NB(the_name, 400, 400, true);
		n.background(r, g, b, a);
		n.matrix(400, 400, { arg x, y;

		case
			{ x==0 and: { y==0 } } { box.value(x, y) } 
			{ x==100 and: { y==0 }} { box.value(x, y) } 
			{ x==200 and: { y==0 }} { box.value(x, y) } 
			{ x==300 and: { y==0 }} { box.value(x, y) } 
			{ x==0 and: { y==100 }}  { box.value(x, y) } 
			{ x==100 and: { y==100 }}  { box.value(x, y) } 
			{ x==200 and: { y==100 }}  { box.value(x, y) } 
			{ x==300 and: { y==100 }}  { box.value(x, y) } 
			{ x==0 and: { y==200 }}  { box.value(x, y) } 
			{ x==100 and: { y==200 }}  { box.value(x, y) } 
			{ x==200 and: { y==200 }}  { box.value(x, y) } 
			{ x==300 and: { y==200 }}  { box.value(x, y) } 
			{ x==0 and: { y==300 }}  { box.value(x, y) }
			{ x==100 and: { y==300 }}  { box.value(x, y) } 
			{ x==200 and: { y==300 }} { box.value(x, y) } 
			{ x==300 and: { y==300 }} { box.value(x, y) };
				
		});
		n.render;
	}

	// after a suggestion by Andrea Valle
	point { arg x, y, width = 1, height = 1, draw = "True";
		case 
			{ basicPointPrim == "rect" } { 
				^this.rect(x, y, width, height, draw) 
			}
			{ basicPointPrim == "oval" } { 
				^this.oval(x-(width/2), y+(height/2), width, height, draw) 
			}
	}

	// after joe the box?
	box { arg x, y, w, h, draw="True";
		var box;
		box = { 
			this.line(x, y, x+w, y) 
			++ 
			this.line(x, y+h, x + w, h+y) 
			++ 
			this.line(x, y, x, y+h) 
			++ 
			this.line(x+w, y, x+w, y+h)
		};
		if(publishing, {
			this.line(x, y, x+w, y);
			this.line(x, y+h, x + w, h+y);
			this.line(x, y, x, y+h);
			this.line(x+w, y, x+w, y+h);
			},{
			box.value
		});	
	}
	
	// assists with translation of NodeBox text commands
	asCompileString { arg txt;
		if(txt.class==Function, { txt = txt.value });
		if(txt.beginsWith($").not and: { txt.endsWith($").not },{ 
			txt = txt.asCompileString
		});
		^txt
	}
	
	// python translation

	assign { arg name, value;
		var theName;
		var python = "python";
		var wasPublishing;
		name = name.asString;
		wasPublishing = publishing;
		if(publishing, { publishing = false });
		if(value.isKindOf(Number), { 
			publishing = wasPublishing;
			^this.publish(name ++ " = " ++ value)
		});
		if(value.isKindOf(Function), { 
			value = value.value.asString;
		});
		theName = methodNames.detect({ arg aMethodName, i;
			value.contains(aMethodName.asString ++ "(")
		});
		if(theName.isNil and: { pythonString.not }, {
			// the value must be a string that needs to be quoted (as compileString)
			value = value.asCompileString;
			// if a nested assignment statement ...
			if(value.contains("\n"), {
				value = value.interpret });
		});
		pythonString = false;
		publishing = wasPublishing;
		^this.publish(name ++ " = " ++ value);
	}
	
	fromImport { arg lib ... args;
		var cmdString;
		cmdString = "from " ++ lib.asString ++ " import " ++ args.at(0);
		args = args.reverse;
		args.pop;
		args = args.reverse;
		^this.publish(cmdString.ccatList(args))
	}

	importAs { arg lib, name;
		var cmdString;
		cmdString = "import " ++ lib.asString ++ " as " ++ name.asString;
		^this.publish(cmdString)
	}
	
	print { arg statement;
		if(statement=="\n", { statement = ""; });
		if(statement.isKindOf(Symbol).not, { statement = statement.asCompileString });
		^this.publish("print " ++ statement.value)
	}
	
	printAll { arg ... statements;
		statements.do({ arg statement;
			this.print(statement)
		})
	}
			
	cmt { arg theComment; 
		^this.publish("# " ++ theComment);
	}
	
	cmts { arg ... comments;
		var result = "";
		comments.do({ arg aComment, i;
			result = result ++ "# " ++ aComment ++ "\n"
		});
		^this.publish("\n" ++ result ++ "\n");
	}
	
	python { arg oneLineOfCode; 
		pythonString = true;
		^this.publish(oneLineOfCode);
	}
	
	py { arg ... code; 
		var result = "";
		var wasPublishing = publishing ;
		publishing = false ;
		code.do({ arg eachLine, i; 
			result = result ++ this.addTab( this.python(eachLine) );
		});
		// this only happens in a for loop ....
		if(result.beginsWith("\t\t"), {
			result = this.popPop(result)
		});
		publishing = wasPublishing  ;
		^this.publish(result);
	}
	
	popPop { arg aString;
		aString = aString.reverse; 
		aString.pop; aString.pop; 
		^aString.reverse;
	}
	
	quote { arg aString; ^aString.asCompileString }
	
	// __ NodeBox API __

	// shape

	rect { arg x, y, width, height, roundness=0.0, draw="True";
		^this.publish(CmdString(\rect, x, y, width, height, roundness, draw))
	}
	
	oval { arg x, y, width, height, draw="True";
		^this.publish(CmdString(\oval, x, y, width, height, draw))
	}
	
	line { arg x1, y1, x2, y2, draw="True";
		^this.publish(CmdString(\line, x1, y1, x2, y2, draw))
	}
	
	arrow { arg x, y, width, type="NORMAL", draw="True";
		^this.publish(CmdString(\arrow, x, y, width, type, draw))
	}
	
	star { arg x, y, points=20, outer=100, inner=50, draw="True";
		^this.publish(CmdString(\star, x, y, points, outer, inner, draw))
	}
	
	// path
	
	beginpath { arg x="None", y="None";
		^this.publish(CmdString(\beginpath, x, y))

	}
	
	moveto { arg x, y;
		^this.publish(CmdString(\moveto, x, y))

	}
	
	lineto { arg x, y;
		^this.publish(CmdString(\lineto, x, y))
	}
	
	curveto { arg h1x, h1y, h2x, h2y, x, y;
		^this.publish(CmdString(\curveto, h1x, h1y, h2x, h2y, x, y))
	}
	
	endpath { arg draw="True";
		^this.publish(CmdString(\endpath, draw))

	}
	
	findpath { arg list, curvature = 1.0;
		^this.publish(CmdString(\findpath, list, curvature))
	}
	
	drawpath { arg path;
		^this.publish(CmdString(\drawpath, path))
	}
	
	beginclip { arg path;
		^this.publish(CmdString(\beginclip, path))
	}
	
	endclip { arg write = true;
		^this.publish(CmdString(\endclip))
	}
	
	// transform
	
	transform { arg mode="CENTER";
		^this.publish(CmdString(\transform, mode))
	}
	
	translate { arg x, y;
		^this.publish(CmdString(\translate, x, y))
	}
	
	rotate { arg degrees=0, radians=0; 
		^this.publish(CmdString(\rotate, degrees, radians))
	}
	
	scale { arg x, y="None"; 
		^this.publish(CmdString(\scale, x, y))
	}
	
	skew { arg x = 0, y=0;
		 ^this.publish(CmdString(\skew, x, y))
	}
	
	push { ^this.publish(CmdString(\push)) }
	
	pop { ^this.publish(CmdString(\pop)) }
	
	reset { ^this.publish(CmdString(\reset)) }
	
	// color
	
	colormode { arg mode = "RGB", range=1.0; 
		^this.publish(CmdString(\colormode, mode, range))
	}
	
	color { arg a, b, c, d, e;
		var red, green, blue;
		var hue, saturation, brightness;
		var cyan, magenta, yellow, black;
		var gray;
		var alpha;
		var cmdString;
		
		cmdString = case
			{ mode == "RGB" } { 
				red = a; green = b; blue = c; alpha = d;
				CmdString(\color, red, green, blue, alpha);
			}
			{ mode == "HSB" } { 
				hue = a; saturation = b; brightness = c; alpha = d;
				CmdString(\color, hue, saturation, brightness, alpha); 
			}
			{ mode == "CMYK" } { 
				cyan = a; magenta = b; yellow = c; black = d; alpha = e;
				CmdString(\color, cyan, magenta, yellow, black, alpha);
			}
			{ c.isNil } { 
				gray = a; alpha = b;
				CmdString(\color, gray, alpha); 
			};
		^cmdString
	}
	
	fill { arg a, b, c, d, e;
		var gray;
		var red, green, blue;
		var hue, saturation, brightness;
		var cyan, magenta, yellow, black;
		var alpha;
		var cmdString;

		cmdString = case
			{ mode == "RGB" } { 
				red = a; green = b; blue = c; alpha = d;
				CmdString(\fill, red, green, blue, alpha);
			}
			{ mode == "HSB" } { 
				hue = a; saturation = b; brightness = c; alpha = d;
				CmdString(\fill, hue, saturation, brightness, alpha);
			}
			{ mode == "CMYK" } { 
				cyan = a; magenta = b; yellow = c; black = d; alpha = e;
				CmdString(\fill, cyan, magenta, yellow, black, alpha); 
			}
			{ c.isNil } { 
				gray = a; alpha = b;
				CmdString(\fill, gray, alpha);
			}
			{ b.isNil } { 
				gray = a;
				if(a.isKindOf(Symbol), {
					"fill(" ++ a.asString ++ ")" },{
					CmdString(\fill, gray);
				})
			};
		^this.publish(cmdString)
	}
	
	nofill { ^this.publish(CmdString(\nofill)) }
	
	stroke { arg a, b, c, d, e;
		var gray;
		var red, green, blue;
		var hue, saturation, brightness;
		var cyan, magenta, yellow, black;
		var alpha;
		var cmdString;

		cmdString = case
			{ mode == "RGB" } { 
				red = a; green = b; blue = c; alpha = d;
				CmdString(\stroke, red, green, blue, alpha);
			}
			{ mode == "HSB" } { 
				hue = a; saturation = b; brightness = c; alpha = d;
				CmdString(\stroke, hue, saturation, brightness, alpha);
			}
			{ mode == "CMYK" } { 
				cyan = a; magenta = b; yellow = c; black = d; alpha = e;
				CmdString(\stroke, cyan, magenta, yellow, black, alpha);
			}
			{ c.isNil } { 
				gray = a; alpha = b;
				CmdString(\stroke, gray, alpha);
			}
			{ b.isNil } { 
				gray = a;
				if(a.isKindOf(Symbol), {
					"stroke(" ++ a.asString ++ ")" },{
					CmdString(\stroke, gray);
				})
			};
		^this.publish(cmdString)
	}
	
	nostroke { ^this.publish(CmdString(\nostroke)) }
	
	strokewidth { arg width; 
		^this.publish(CmdString(\strokewidth, width))
	}
	
	background { arg r, g, b, a = 1.0;
		^this.publish(CmdString(\background, r, g, b, a))
	}

	// type
	
	font { arg fontname, fontsize="None";
		if(fontname.isKindOf(String), {
			fontname = this.asCompileString(fontname); }, {
			fontname = fontname.asString
		});
		^this.publish(CmdString(\font, fontname, fontsize))
	}
	
	fontsize { arg fontsize; 
		^this.publish(CmdString(\fontsize, fontsize))
	}
	
	text { arg txt, x, y, width="None", height=1000000, outline="True"; 
		if(txt.isKindOf(String), {
			txt = this.asCompileString(txt); }, {
			txt = txt.asString
		});
		^this.publish(CmdString(\text, "u"++txt, x, y, width, height, outline))
	}
	
	textpath { arg txt, x, y, width="None", height=1000000; 
		txt = this.asCompileString(txt);
		^this.publish(CmdString(\textPath, txt, x, y, width, height))
	}
	
	textwidth { arg txt, width="None"; 
		txt = this.asCompileString(txt);
		^this.publish(CmdString(\textwidth, txt, width))
	}
	
	textheight { arg txt, width="None"; 
		txt = this.asCompileString(txt);
		^this.publish(CmdString(\textheight, txt, width))
	}
	
	textmetrics { arg txt, width="None"; 
		txt = this.asCompileString(txt);
		^this.publish(CmdString(\textmetrics, width))
	}
	
	lineheight { arg height="None";
		^this.publish(CmdString(\lineheight, height))
	}
	
	align { arg align="LEFT"; 
		^this.publish(CmdString(\align, align))
	}
		
	// image
	
	image { arg path, x, y, width, height; 
		^this.publish(CmdString(\image, path.asCompileString, x, y, width, height))
	}
	
	imagesize { arg path; 
		^this.publish(CmdString(\imagesize, path.asCompileString))
	}
	
	// utility
	
	size { arg w, h; 
		^this.publish(CmdString(\size, w, h))
	}
	
	// in NodeBox, this is 'var' ... but 'var' is a reserved word in SC
	variable { arg name, type, default, min, max; 
		^this.publish(CmdString(\var, name.asString.asCompileString, type, default, min, max))
	}
	
	choice { arg list; 
		^this.publish(CmdString(\choice, list))

	}
	
	grid { arg cols, rows, colsize=1, rowsize=1; 
		^this.publish(CmdString(\grid, cols, rows, colsize, rowsize))
	}
	
	files { arg path; 
		^this.publish(CmdString(\files, path.asCompileString))
	}
	
	// CmdString only __ the publish method isn't used
	
	openRead { arg path = "/this/has/to/be/a/valid/path"; 
		^CmdString(\openRead, path.asCompileString)
	}
	
	openReadlines { arg path = "/this/has/to/be/a/valid/path";
		^CmdString(\openReadlines, path.asCompileString)
	}
	
	autotext { arg path;
		^CmdString(\autotext, path.asCompileString)
	}
	
	random { arg v1="None", v2="None"; 
		^CmdString(\random, v1, v2);
	}
}