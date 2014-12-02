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