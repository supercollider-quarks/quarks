Grapher {

	// A class for calling GraphViz
	// You can add Graph object and it generates
	// a .dot file including them all
	// if you want different img file
	// you simply instantiate as many Graphers
	// and seth the paths  

	var <>graph, <>runner, <>dotFile, <>dotPath, <>outPath, <>binPath, <>flags ;
	var <>colFact ; // a multiplier for color scaling

	// if you want you can set all the stuff
	*new { arg graph, runner, ext = "png", outPath = "/graphs/tmp",
						dotPath = "/graphs/tmp.dot",
						binPath = "/usr/local/graphviz-2.12/bin/dot",
						flags = "-Gcharset=latin1",
						colFact = 0.1 ; // --> after 10 times white is reached ;
		var flag =  "-T"++ext + flags ;
		var theOutPath = outPath++"."++ext ;
		^super.new.initGrapher( graph, runner, theOutPath, dotPath, binPath, flag, colFact ) 
	}

	initGrapher { arg  aGraph, aRunner, aOutPath, aDotPath, aBinPath, theFlags, aColFact ;
		graph = aGraph ;
		runner = aRunner ;
		outPath = aOutPath ;
		dotPath = aDotPath ;
		binPath = aBinPath ;
		flags = theFlags ;
		colFact = aColFact ;
		graph.addDependant(this) ;
		runner.addDependant(this) ;	
		this.update ;
	}

	


	// pretty empyrical --> used for snapshot from runner
	calculateVColor { arg vCount, alpha = 0.8 ;
		var col, colString ;
		vCount = (vCount*colFact).clip2(1) ; // scaled
		col = Color.new(vCount, vCount*0.5, vCount*0.25, alpha).asHSV ;
		colString = col[0].asString+col[1].asString+col[2].asString+alpha.asString ;
		colString = colString.replace("nan", "0") ; 
		^colString
	}



//	removeGraph { }

	translate { arg mul = 0.02 ;
		// mul takes care of an inch to pixel conversion
		var pos, label, col, num ;
		dotFile = File(dotPath, "w") ;
		dotFile.write("digraph G {
graph [bgcolor=White];
node [color=\"#800040a3\",fontname=Monaco, shape=rectangle, fontsize=8, fontcolor=white, style=filled];
edge [color=\"#00408099\",fontname=Arial, fontsize=8, fontcolor=black];
		
\n");
		graph.graphDict.keys.do({ arg key ;
			num = if( runner.statsDict[key] == nil, {0},{runner.statsDict[key]});
			col = this.calculateVColor(num) ;
			col = ", fillcolor = \""++col++"\"" ;
			label = "label =\""+key.asString++": " +graph.graphDict[key][3].asString+"\"";
			pos = ", pos =\""++(graph.graphDict[key][0]*mul).asString++","++(graph.graphDict[key][1]*mul).asString++"!\"" ;
			dotFile.write(key.asString
					+"["+label+pos+col+ "];\n") ;
			graph.graphDict[key][5..].do({arg e ;
				dotFile.write(
					key.asString + "->" + e[0].asString + "[label =\""+e[2].asString++":" +e[1].asString + "\"] ;\n" ) 
				}) ;
			}) ;
		dotFile.write("\n}");
		dotFile.close ;
	}

	// call graphviz
	exec {
			var p, l ;
			var cmd = binPath + flags + dotPath + "-o" + outPath ;
			cmd.postln ;
			unixCmd(cmd) ;
			"done".postln ;
				}


	update { arg theChanged, theChanger;
		this.translate ;
		//this.exec ; // call graphviz
	}

	snapshot { arg render = false ;
		this.translate ;
		if ( render, { this.exec }) ;
	}

}



// Setup a GUI with a routine reloading the img file specified in the path 
// with a trick by Sciss
// NOTE: troubles if image filesize is changing (--> always)
// no exploration --> better use ZGRViewer

GraphView {

	var <>path, <>task, <>time ;

	*new {Ê arg aPath, aTime = 3 ;
		^super.new.initViewer(aPath, aTime)Ê
	}

	initViewer { arg aPath, aTime ;
		var w, m, gui, counter = 0 ;
		path = aPath ;
		time = aTime ;
		GUI.schemes.do(_.put(\imageView,ImageView));
		gui = GUI.current ;
		w = gui.window.new("GraphViewer", Rect(100, 100, 1200, 800)) ;
		w.view.background = Color.white ;
		m = gui.imageView.new( w, Rect( 0, 0, 1200, 800 )) ;
		m.path_(path) ;
		w.front;
		task = Task({ loop({
					time.wait;
					// counter = counter + time ;
					// ("refreshing after"+counter+"secs").postln;
//					m.path_(path) ;
					m.reload;
				})
		}, AppClock).start
	}


}
