+ String {
	ask { |function|
		function = function ? { this.postln; };
		^SCAlert( this, [\cancel, \ok], [ nil, function ] );
		}
	
	request { |function, question = "Please enter string:"|
		function = function ? { |string| string.postln; };
		question =  question ? "Please enter string:";
		^SCRequestString( this, question, function );
		}
		
	write { |fileName, overwrite = false, ask = true|
		if( fileName.isNil )
			{ SCRequestString("~/scwork/string.txt", "Please specify a file name:",
				{ |string| File.checkDo( string, this, overwrite, ask)  } );
			} {
			File.checkDo( fileName, this, overwrite, ask)
			};
		}		
	}
	
+ Function {

	ask { |string|
		string = string ? "evaluate function?";
		^SCAlert( string, [\cancel, \ok], [nil, this] )
		}
		
	request { |default = "", question|
		question =  question ? "Please enter string:";
		^SCRequestString( default, question, this );
		}
	
	}
