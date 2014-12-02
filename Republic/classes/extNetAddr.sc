+ NetAddr {

	servesPort { |inPort|
		^inPort == port	
	}
	
	checkServesLangPort {
		var langPort = NetAddr.langPort;
		if(this.servesPort(langPort).not) {
			"Your port number (%) falls outside of port range. Try restarting SuperCollider"
				.format(langPort).warn;
		}
	}

	*multiNew { |hostname, port|
		^[hostname, port].flop.collect(NetAddr(*_))
	}

	*multiNewRange {|hostname, port, size = 8|
		^this.multiNew(hostname, port + (0..size-1))
	}

	*multiBroadcast {|port = 57120, size = 8| // add mask later
		^this.multiNewRange("255.255.255.255", port, size)
	}
	
	*getBroadcastIPs { 
		^Platform.case(
			\osx, {
				unixCmdGetStdOut("ifconfig | grep broadcast | awk '{print $NF}'")
				.split($\n).reject(_.isEmpty) },
			\windows, { // untested?
				unixCmdGetStdOut("ifconfig | grep broadcast | awk '{print $NF}'")
				.split($\n).reject(_.isEmpty) },
			\linux, { // works at least on ubuntu and xandros...
				unixCmdGetStdOut("/sbin/ifconfig | grep Bcast | awk 'BEGIN {FS = \"[ :]+\"}{print $6}'")
				.split($\n).reject(_.isEmpty) }
		);
	}

}

+ NetAddrMP {
	
	servesPort { |inPort|
		^ports.includes(inPort)	
	}
	
}