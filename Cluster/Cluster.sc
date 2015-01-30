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

// class to apply methods to all elements of the items array
// all items should be of type oclass

//To Do merge Cluster and ClusterBasic into just one, and subclass as appropriate.

Cluster{

	var <items, <oclass;

	prCollect{ |selector,args|

		var return = items.collect(_.perform(*([selector]++args)));

		if(return[0].class == this.oclass){
			^this
		}{
			^Cluster(return)
		}

	}

	prCollectSimple{ |selector|

		var return = items.collect(_.perform(selector));

		"prcollectsimple";

		if(return[0].class == this.oclass){
			^this
		}{
			^Cluster(return)
		}
	}

	prCollectArray{ |selector,argArray|

		var return = items.collect{ |item,i| item.perform(*([selector]++argArray[i]))};
		"!!!prCollectArray";

		//if((return[0].class == this.oclass) || (return[0] == nil)){
		//	^this
		//}{
			^Cluster(return)
		//}

	}

	//expand a set of arguments into an array of size items.size
	prExpand{ |args|
		("prExpand: "++args);
		^Cluster.expandArray(args,this);
	}

	prExpandCollect{ |selector,args|

		if(args.isNil){
			"args nil";
			^this.prCollectSimple(selector)
		}{
			"args not nil";
			^this.prCollectArray(selector,this.prExpand(args))
		}

	}

	prCheckSize{ |clusterobject|
		if(clusterobject.items.size != items.size){
			Error("Cluster sizes mismatch. ClusterObject with size "++clusterobject.items.size++
			" vs ClusterObject with size "++items.size++" \n "++[clusterobject.items,items]).throw
		}
	}

	//default implementation of collected classes.
	// will work, but it's not possible to pass arguments by name. e.g. busnum: 3
	doesNotUnderstand{ arg selector...args;
		//("does not understand- base class "++this.class++" selector "++selector);
		if(this.respondsTo(selector)){
			^this.prExpandCollect(selector,args)
		}{
			^super.doesNotUnderstand(*([selector]++args));
		}

	}

	respondsTo{ |selector|
		^items[0].respondsTo(selector)
	}

	dopost{ "items:".postln; items.do(_.postln) }

	printOn { arg stream;
		stream << "Cluster" << items;
	}

	clApply{ |func|
		^Cluster(items.collect{ |item| func.(item) })
	}

	clApplyF{ |func|
		^Cluster(items.collect{ |item| { func.(item) } })
	}

	*applyN{ arg func ...clusterObjects;
		var n = clusterObjects.collect{ |obj|
			if(obj.class == Cluster){obj.items.size}{1}
		}.maxItem;
		//if object is not cluster it is duplicated to the maximum number of items and made into a cluster.
		clusterObjects = clusterObjects.collect{ |obj|
			if(obj.class != Cluster){Cluster(Array.fill(n,{obj}))}{obj}
		};

		^Cluster(n.collect{ |i|
			func.(*clusterObjects.collect{ |clusterObj|
				if(clusterObj.items.size == n){
					clusterObj.items[i]
				}{
					clusterObj.items[0]
				}
			})
		})
	}

	// Class methods wrapping
	*fromArray{ |array,oclass|
		if(array.collect{ |x| x.class}.as(Set).size>1){
			Error("Cluster: "++array++" - Items should be all of the same class").throw
		};
	 	^super.newCopyArgs(array,array[0].class);
	 }

	 *new{ |array,oclass|
	 	^this.fromArray(array,array[0].class);
	 }

	*expandArray{ |array,clusterObject|

		var recursF1,recursF2,finalF;

		recursF1 = { |array,clusterobject|
			array.collect{ |item|
				if(item.isArray && item.isString.not){
					recursF1.(item,clusterobject)
				}{
					if(item.class == Cluster){
						clusterObject.prCheckSize(item);
						item
					}{
						Cluster(clusterobject.items.collect{ item })
					}
				}
			}
		};

		recursF2 = { |array,i|
			array.collect{ |item|
				if(item.isArray && item.isString.not){
					"is array";
					recursF2.(item,i)
				}{
					"is Cluster";
					item.items[i]
				}
			}
		};

		finalF = { |array,clusterobject|
			clusterobject.items.collect{ |adas,i|
				("line "++i);
				recursF2.(array,i)
			}};

		^finalF.(recursF1.(array,clusterObject),	clusterObject)
	}

	*searchArrayForCluster{ |array|
        array.postln;
		array.do{ |item|
            item.postln;
			if(item.class.superclasses.includes(ClusterBasic)){
				^item
			}{
				if(item.isArray && item.isString.not){
					^this.searchArrayForCluster(item)
				}

			}
		};
		Error("arguments must have at least one Cluster class instance").throw
	}

}
























