+ControlSpec {

	excludingZeroCrossing {
		if(minval != 0 and:{ maxval != 0 }) {
			^this.hasZeroCrossing
		}
		^false
	}

}

+Array {

	cvCenterBuildCVConnections { | server, nodeID, node, cvcKeys, setActive |
		var parameters, cvLinks, k, wdgtKey, cvValues;
		var label, cv, expr, whatToSet;

		parameters = this.copy.clump(2);
		cvLinks = Array(parameters.size);
		parameters = parameters.collect { | p |
			#label, cv = p;
			if(cv.class !== Array, {
				if(cv.isKindOf(Function), { cv = cv.value });
				#cv, expr = cv.asArray;
			}, {
				cv.do({ |c, i|
					c.isKindOf(Function).if({ cv[i] = cv[i].value });
				})
			});
			expr = expr ? cv;
			if(expr.isNumber.not, {
				cv.asArray.do({ |c, i|
					wdgtKey = CVCenter.all.findKeyForValue(c);
					wdgtKey !? {
						if(cv.size > 1, {
							cvValues = nil!(cv.size);
							cv.do({ |cvau, i|
								if(cvau === CVCenter.at(wdgtKey), {
									cvValues[i] = "cv.value";
								}, {
									cvValues[i] = "CVCenter.at('"++CVCenter.all.findKeyForValue(cvau)++"').value";
								})
							})
						});
						if(node.notNil, {
							if(cv.size > 1, {
								cvLinks.add(CVCenter.addActionAt(
									wdgtKey, "default_"++node.asString.replace(34.asAscii, ""),
									"{ |cv| "++node+"!? {"+node++".setn('"++label++"', ["++cvValues.join(", ")++"]) }}",
									// slot:
									active: setActive
								));
								cvValues = nil;
							}, {
								switch(CVCenter.cvWidgets[wdgtKey].class,
									CVWidget2D, {
										if(label.isArray, {
											(label++[\lo, \hi]).clump(2).flop.do({ |pair|
												cvLinks.add(CVCenter.addActionAt(wdgtKey, "default_"++node.asString.replace(34.asAscii, ""),
													"{ |cv| "++node+"!? {"+node++".set('"++pair[0]++"', cv.value) }}", pair[1]
												))
											})
										}, {
											cvLinks.add(CVCenter.addActionAt(wdgtKey, "default_"++node.asString.replace(34.asAscii, ""),
												"{ |cv| "++node+"!? {"+node++".setn('"++label++"', [cv.value, CVCenter.at('"++wdgtKey++"').hi.value]) }}", \lo
											));
											cvLinks.add(CVCenter.addActionAt(wdgtKey, "default_"++node.asString.replace(34.asAscii, ""),
												"{ |cv| "++node+"!? {"+node++".setn('"++label++"', [CVCenter.at('"++wdgtKey++"').lo.value, cv.value]) }}", \hi
											));
										})
									},
									// CVWidgetMS, {
									//
									// },
									{
										cvLinks.add(CVCenter.addActionAt(
											wdgtKey, "default_"++node.asString.replace(34.asAscii, ""),
											"{ |cv| "++node+"!? {"+node++".set('"++label++"', cv.value) }}",
											active: setActive
										))
									}
								)
							})
						}, {
							if(cv.size > 1, {
								CVCenter.cvWidgets[wdgtKey].class.postln;
								cvLinks.add(CVCenter.addActionAt(
									wdgtKey, \default,
									"{ |cv| Server('"++server++"').sendBundle("++server.latency++", ['/n_setn', "++nodeID++", '"++label++"', "++cv.size++", "++cvValues.join(", ")++"]) }"

								))
							}, {
								switch(CVCenter.cvWidgets[wdgtKey].class,
									CVWidget2D, {
										if(label.isArray, {
											(label++[\lo, \hi]).clump(2).flop.do({ |pair|
												cvLinks.add(CVCenter.addActionAt(wdgtKey, "default_"++node.asString.replace(34.asAscii, ""),
													"{ |cv| Server('"++server++"').sendBundle("++server.latency++", ['/n_setn', "++nodeID++", '"++pair[0]++"', 1, cv.value]) }", pair[1]
												))
											})
										}, {
											cvLinks.add(CVCenter.addActionAt(wdgtKey, "default_"++node.asString.replace(34.asAscii, ""),
												"{ |cv| Server('"++server++"').sendBundle("++server.latency++", ['/n_setn', "++nodeID++", '"++label++"', 2, cv.value, CVCenter.at('"++wdgtKey++"').hi.value]) }", \lo
											));
											cvLinks.add(CVCenter.addActionAt(wdgtKey, "default_"++node.asString.replace(34.asAscii, ""),
												"{ |cv| Server('"++server++"').sendBundle("++server.latency++", ['/n_setn', "++nodeID++", '"++label++"', 2, CVCenter.at('"++wdgtKey++"').lo.value, cv.value]) }", \hi
											));
										})
									},
									// CVWidgetMS, {
									//
									// },
									{
										cvLinks.add(CVCenter.addActionAt(wdgtKey, \default,
											"{ |cv| Server('"++server++"').sendBundle("++server.latency++", ['/n_setn', "++nodeID++", '"++label++"', 1, cv.value]) }"
										))
									}
								)
							})
						})
					}
				})
			})
		};

		^parameters.flatten(1);
	}

}