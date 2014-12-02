// updated curl method stack based on wslib String:curl

// if you have wslib installed: check whether this functionality is already implemented there, if not, replace the file extString-curl.sc with this one.
// if you don't have wslib installed: rename this file to extString-curl.sc and recompile

+ String {
	curlMsg { |path, options|
		var expr;

		expr = "curl % %".format( options ? "", this.quote );
		path.notNil.if{
			expr = expr + "-o %".format(path.quote);
		};

		^expr;
	}
	
	curl { |path, options, action, postOutput = true|
		var msg = this.curlMsg(path, options);
		
		path.notNil.if({
			^msg.unixCmd(action, postOutput);
		}, {
			^msg.unixCmdGetStdOut;
		})
	}
}