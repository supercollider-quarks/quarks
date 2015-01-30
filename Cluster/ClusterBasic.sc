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


ClusterBasic {
	var <items;

	//no arguments
	prCollectNoArgs{ |selector|
		var return = items.collect(_.tryPerform(selector));
		"prcollectsimple";
		^this.getReturnClusterObject(return);
	}

	//with arguments
	prCollectWithArgs{ |selector,argArray|
		var return = items.collect{ |item,i| item.tryPerform(*([selector]++argArray[i]))};
		"!!!prCollectWithArgs";
		^this.getReturnClusterObject(return);
	}

	getReturnClusterObject{ |returnArray|
		var clusterClasses;
		if((returnArray[0].class == this.oclass) ){
			^this
		}{
			if((returnArray[0].isNil) ){
				^nil
			} { //this needs to be fixed, it should return the class corresponding to the oclass.
				clusterClasses = ClusterBasic.allSubclasses;
				//clusterClasses.remove(this.class); // ?
				clusterClasses = clusterClasses.collect(_.oclass);
				if(clusterClasses.includes(returnArray[0].class)){
					^("Cluster"++returnArray[0].class.asCompileString).compile.value.fromArray(returnArray)
				}{
					^ClusterArg(returnArray)
				}
			}
		}
	}

	//expand a set of arguments into an array of size items.size
	prExpandArgs{ |args|
		("prExpand: "++args);
		^ClusterBasic.expandArray(args,this);
	}

	prExpandCollect{ |selector,args|
		if(args.size == 0){
			"args nil";
			^this.prCollectNoArgs(selector)
		}{
			"args not nil";
			^this.prCollectWithArgs(selector,this.prExpandArgs(args))
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
		//("ClusterBasic:doesNotUnderstand | base class "++this.class++" | selector "++selector).postln;
		if(this.respondsTo(selector)){
			^this.prExpandCollect(selector,args)
		}{
			//if method not implemented than call Object's doesNotUnderstand
			//"items: %".format(items).postln;
			//"class % doesn't implement selector %".format(items[0].class, selector).postln;
			^super.doesNotUnderstand(*([selector]++args));
		}
	}

	respondsTo{ |selector|
		^items[0].respondsTo(selector)
	}

	oclass{ ^this.class.oclass }

	dopost{ "items:".postln; items.do(_.postln) }

	printOn { arg stream;
		stream << this.class.asString << items ;
	}

	// Class methods wrapping
	*fromArray{ |array|
		if(array.collect{ |x| x.class }.as(Set).size>1){
			Error("ClusterBasic: "++array++" - Items should be all of the same class").throw
		};
	 	^super.newCopyArgs(array);
	 }

	 *new{ |array|
	 	^this.fromArray(array);
	 }

	*expandArray{ |array,clusterObject|
		var recursF1,recursF2,finalF;
		recursF1 = { |array,clusterobject|
			array.collect{ |item|
				if(item.isArray && item.isString.not){
					recursF1.(item,clusterobject)
				}{
					if(item.class.superclasses.includes(ClusterBasic)){
						clusterObject.prCheckSize(item);
						switch(item.class)
							{ClusterBus}{ item.index }
							{ClusterBuffer}{ item.bufnum }
							{item}
					}{
						ClusterArg(clusterobject.items.collect{ item })
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
					"is clusterarg";
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

	*searchArrayForClusterRec{ |array|
        //return the first element that is a ClusterArg
		^array.inject(None(), { |state,item|
            if(state.isDefined) {
                state
            } {
                if(item.class.superclasses.includes(ClusterBasic)){
                    Some(item)
                }{
                    if(item.isArray && item.isString.not){
                        this.searchArrayForClusterRec(item)
                    } {
                        state
                    }

                }
            }
        })
	}

    *searchArrayForCluster{ |array|
        var x = this.searchArrayForClusterRec(array);
        if (x.isEmpty ) {
            Error("arguments must have at least one Cluster class instance").throw
        } {
            ^x.get
        }
    }

	// is there a case beyond getters and setters for classvars that a class method is called with no arguments and no defaults ?
	*prCollectSimple{ |selector,referenceCluster|
		("prCollectSimple - this class: "++this.class);
		^ClusterArg([this.oclass.perform(selector)]);
	}

	*prCollectWithArgs{ |selector,argArray,referenceCluster|
		("prCollectWithArgs - this class: "++this.class);
		^referenceCluster.items.collect{ |item,i| this.oclass.perform(*([selector]++argArray[i]))};
	}

	*prExpandCollect{ |selector,args, referenceCluster|
		var f;
		("prExpandCollect - this class: "++this.class);
		if(args.size == 0){
			"*prExpandCollect - no arguments - not doing anything at the moment - probably you want a class var, just get it directly".postln;
			//^this.prCollectSimple(selector)
		}{
			f = { |array|
				array.collect{ |item|
					if(item.isArray && item.isString.not && (item.class != M) ){
						f.(item)
					}{
						if( item.class == M ){
							item.asClusterArg
						}{
							item
						}
					}
				}
			};
			args = f.(args);
			referenceCluster = referenceCluster ?? { this.searchArrayForCluster(args) };
			^this.prCollectWithArgs(selector, this.expandArray(args,referenceCluster), referenceCluster)
		}
	}

	*doesNotUnderstand{ arg selector...args;
		var cluster;
		//("doesNotUnderstand - "++selector++" this class: "++this.class++" args is "++args).postln;
		if(this.oclass.class.findRespondingMethodFor(selector).notNil){
			^super.newCopyArgs(this.prExpandCollect(selector,args))
		}
	}

	*newExpandCollect { |selector, args,referenceCluster|
		^super.newCopyArgs(this.prExpandCollect(selector,args, referenceCluster))
	}

	*oclass{ 	^Object }

	clusterfy{ ^this }

	deCluster{ ^items[0] }

	clApplyF{ |func|
		^ClusterArg(items.collect{ |item| { func.(item) } })
	}

	//explicit overloading of methods from Object

	changed { arg what ... moreArgs;
		^this.doesNotUnderstand(*([\changed,what]++moreArgs));
	}

	addDependant { arg dependant;
		^this.doesNotUnderstand(\addDependant,dependant);
	}

	removeDependant { arg dependant;
		^this.doesNotUnderstand(\removeDependant,dependant);
	}
	release {
		^this.doesNotUnderstand(\release)
	}
	releaseDependants {
		^this.doesNotUnderstand(\releaseDependants)
	}
}



