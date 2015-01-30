/*
    Cluster Library
    Copyright 2009-2012 Miguel Negrão.

    Cluster Library: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

   Cluster Library is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with Cluster Library.  If not, see <http://www.gnu.org/licenses/>.
*/

+ Server{
	asClusterServer{
		^ClusterServer.new([this])
	}
}

+ Group{
	asClusterGroup{
		^ClusterGroup.fromArray([this])
	}
	
}

+ OSCBundle{
	asClusterBundle{
		^ClusterOSCBundle.fromArray([this])
	}
		
}

+ SynthDef{

	sendCluster{ |clusterServer|
		clusterServer.items.do{ |server|  this.send(server) };
	}
	
	laadCluster{ |clusterServer|
		clusterServer.items.do{ |server|  this.load(server) };
	}
	
	//unsyncronized
	playCluster{ |clustertarget,args,addAction=\addToTail|
		var synths = clustertarget.items.collect{ |target|
			this.play(target,args,addAction)
		};
		^ClusterSynth.fromArray(synths);
		
		
	}


}

+ Array{
	//for a single array with n values corresponding to n objects
	asClusterArg{
		^ClusterArg(this)
	}
	
	asCluster{
		^Cluster(this)
	}
	
}
