/* (c) Stefan Nussbaumer */
/*
 This program is free software; you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation; either version 3 of the License, or
 (at your option) any later version.

 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with this program; if not, write to the Free Software
 Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301  USA
*/

/* automatic GUI-creation from SynthDefs, Ndefs ... */
/* read input */

+Object {

	// execute .changed for the given array of symbols
	changedKeys { |keys ... moreArgs|
		keys.do({ |key|
			this.changed(key.asSymbol, *moreArgs);
		})
	}

	getObjectVarNames { |environment|
		var interpreterVars, varNames = [], envs = [];
		var pSpaces = [], proxySpace;

		interpreterVars = #[a,b,c,d,e,f,g,h,i,j,k,l,m,n,p,q,r,s,t,u,v,w,x,y,z];

		varNames = varNames ++ interpreterVars.select({ |n|
			thisProcess.interpreter.perform(n) === this;
		});
		if(currentEnvironment.class !== ProxySpace, {
			currentEnvironment.pairsDo({ |k, v|
				if(v === this, { varNames = varNames.add("~"++(k.asString)) });
			})
		});

		case
			{ this.isKindOf(Node) } {
				environment !? {
					envs = interpreterVars.select({ |n|
						thisProcess.interpreter.perform(n) === environment;
					});
					currentEnvironment.pairsDo({ |k, v|
						if(v === environment, { envs = envs.add("~"++(k.asString)) });
					});
					environment.pairsDo({ |k, v|
						if(v === this, {
							envs = envs.collect({ |ev| ev = ev++"['"++k++"']" });
						})
					})
				};
				varNames = varNames++envs;
			}
			{ this.isKindOf(NodeProxy) and:{ this.class != Ndef }} {
				// the NodeProxy passed in could be part of a ProxySpace
				if(varNames.size < 1, {
					pSpaces = pSpaces ++ interpreterVars.select({ |n|
						thisProcess.interpreter.perform(n).class === ProxySpace;
					});
					if(currentEnvironment.class !== ProxySpace, {
						currentEnvironment.pairsDo({ |k, v|
							if(v.class === ProxySpace, { pSpaces = pSpaces.add("~"++k) });
						})
					});
					pSpaces.do({ |p|
						if(p.class === Symbol, {
							proxySpace = thisProcess.interpreter.perform(p);
						});
						if(p.class === String, {
							proxySpace = p.interpret;
						});
						if(proxySpace.respondsTo(\envir), {
							proxySpace.envir.pairsDo({ |k, v|
								if(v === this, {
									varNames = varNames.add(p.asString++"['"++k++"']");
								})
							})
						})
					})
				})

			}
			{ this.class == Ndef } { varNames = varNames.add(this.asString) }
		;
		^varNames;
	}

}

+Synth {

	cvcGui { |displayDialog=true, prefix, pairs2D, environment|
		var sDef, def, cDict = (), metadata;
		var thisType, thisControls, thisSpec, thisSlots, thisName, done=[];
		sDef = SynthDescLib.global[this.defName.asSymbol];
		sDef.metadata !? { sDef.metadata.specs !? { metadata = sDef.metadata.specs }};
		sDef.controlDict.pairsDo({ |n, c| cDict.put(n, c.defaultValue) });
		if(displayDialog, {
			CVWidgetSpecsEditor(displayDialog, this, this.defName.asSymbol, cDict, prefix, pairs2D, metadata, environment);
		}, {
			cDict.pairsDo({ |cName, vals|
				block { |break|
					pairs2D.pairsDo({ |wdgtName, cNames|
						if(cNames.includes(cName), {
							break.value(
								thisName = wdgtName;
								thisType = \w2dc;
								thisControls = cNames;
								if(metadata.notNil, {
									metadata[cNames[0]] !? { thisSpec = metadata[cNames[0]].asSpec };
								}, {
									thisSpec = cName.asSpec;
								});
								done = done.add(cNames).flat;
							)
						})
					})
				};
				if(cDict[cName].size == 0 and:{ done.includes(cName).not }, { thisType = nil; thisName = cName });
				if(cDict[cName].size == 2, { thisType = \w2d; thisName = cName });
				if(cDict[cName].size > 2, { thisType = \wms; thisName = cName });

				switch(thisType,
					\w2dc, {
						thisSlots = [cDict[thisControls[0]], cDict[thisControls[1]]];
					},
					\w2d, {
						thisSlots = cDict[cName];
						if(metadata.notNil, {
							metadata[cName] !? { thisSpec = metadata[cName].asSpec };
						}, {
							thisSpec = cName.asSpec;
						})
					},
					\wms, {
						thisSlots = cDict[cName];
						if(metadata.notNil, {
							metadata[cName] !? { thisSpec = metadata[cName].asSpec };
						}, {
							thisSpec = cName.asSpec;
						})
					},
					thisSlots = [cDict[cName]];
					if(metadata.notNil, {
						metadata[cName] !? { thisSpec = metadata[cName].asSpec };
					}, {
						thisSpec = cName.asSpec;
					})
				);

				prefix !? { thisName = prefix.asString++(thisName.asString[0]).toUpper ++ thisName.asString[1..] };

				CVCenter.finishGui(this, cName, nil, (
					cName: thisName,
					type: thisType,
					enterTab: this.defName.asSymbol,
					controls: thisControls,
					slots: thisSlots,
					specSelect: thisSpec
				))
			})
		})
	}

}

+NodeProxy {

	cvcGui { |displayDialog=true, prefix, pairs2D|
		var cDict = (), name;
		var thisType, thisControls, thisSpec, thisSlots, thisName, done=[];
		this.getKeysValues.do({ |pair| cDict.put(pair[0], pair[1]) });
		if(this.class === Ndef, {
			name = this.key;
		}, {
			name = nil;
		});
		if(displayDialog, {
			CVWidgetSpecsEditor(displayDialog, this, name, cDict, prefix, pairs2D);
		}, {
			cDict.pairsDo({ |cName, vals|
				block { |break|
					pairs2D.pairsDo({ |wdgtName, cNames|
						if(cNames.includes(cName), {
							break.value(
								thisName = wdgtName;
								thisType = \w2dc;
								thisControls = cNames;
								thisSpec = cName.asSpec;
								done = done.add(cNames).flat;
							)
						})
					})
				};
				if(cDict[cName].size == 0 and:{ done.includes(cName).not }, { thisType = nil; thisName = cName });
				if(cDict[cName].size == 2, { thisType = \w2d; thisName = cName });
				if(cDict[cName].size > 2, { thisType = \wms; thisName = cName });

				switch(thisType,
					\w2dc, {
						thisSlots = [cDict[thisControls[0]], cDict[thisControls[1]]];
					},
					\w2d, {
						thisSlots = cDict[cName];
						thisSpec = cName.asSpec;
					},
					\wms, {
						thisSlots = cDict[cName];
						thisSpec = cName.asSpec;
					},
					thisSlots = [cDict[cName]];
					thisSpec = cName.asSpec;
				);

				prefix !? { thisName = prefix.asString++(thisName.asString[0]).toUpper ++ thisName.asString[1..] };

				CVCenter.finishGui(this, cName, nil, (
					cName: thisName,
					type: thisType,
					enterTab: name,
					controls: thisControls,
					slots: thisSlots,
					specSelect: thisSpec
				))
			})
		})
	}

}