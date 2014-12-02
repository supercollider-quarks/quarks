GServer : Server {

	var <>cmdArgs = "";

	start{
		("scgraph"+cmdArgs).unixCmd;
		
	}

}