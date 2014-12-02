	// Markov chain/generative grammar approach with self-adjusting context depth. 
	// Nierhaus, de Campo 2008 
	
ContextSnake : Pattern {

	var <>corpus, <>starter, <>minDepth, <>acceptSingleCond, <>starterLength, <>verbose=false;
	
	*new { |corpus, starter, minDepth=4, acceptSingleCond=false, starterLength=4| 
		^super.newCopyArgs(corpus, starter, minDepth, acceptSingleCond, starterLength).init;
	} 
	init { 
		starter ?? { this.randStarter }
	}
	randStarter { starter =  corpus.choose.keep(starterLength) }
	
	embedInStream { arg inval; 
		var predecessor = starter;
		var candidateIndexPairs, numPairs, chosenSpot, corpusLine, nextVal;
		
		var findNext = { 
				// find  successor candidates
			candidateIndexPairs = this.find(predecessor);
			if (verbose) { [\candidateIndexPairs, candidateIndexPairs].postln }; 
			
			numPairs = candidateIndexPairs.size; 
			
			numPairs.switch(
				0, { 
					if (verbose, { "no successors".postln });
					nextVal = nil 
				},
				1, { 
						// if below minimum, or other reasons, accept single choice
					if ( (predecessor.size <= minDepth) or: 
						acceptSingleCond
					) { 
						if (verbose, { "accepting single".postln });
						chosenSpot = candidateIndexPairs.first; 
						corpusLine = corpus[chosenSpot[0]];
						
						nextVal = corpusLine[chosenSpot[1] + predecessor.size]; 
						if (predecessor.isKindOf(String)) { 
							predecessor = predecessor ++ nextVal;
						} {
							predecessor = predecessor.add(nextVal); 
						};
						
						if (verbose) { "predec: ".post; predecessor.postcs; };
					} { 
						// else shorten context and try again recursively
						if (verbose, { \recursion.postcs });
						predecessor = predecessor.drop(1);
						nextVal = findNext.value;
					}
				},
				{ 	// more than one option:
					chosenSpot = candidateIndexPairs.choose; 
					if (verbose, { [\multi, \chosenSpot, chosenSpot, 
						\candidateIndexPairs, candidateIndexPairs].postcs 
					});
					
					corpusLine = corpus[chosenSpot[0]];
					nextVal = corpusLine[chosenSpot[1]+predecessor.size];
						// ok, lengthen context
						if (predecessor.isKindOf(String)) { 
							predecessor = predecessor ++ nextVal;
						} {
							predecessor = predecessor.add(nextVal); 
						};
					nextVal;
				});
			
			nextVal;
		}; 
					// yield starter values first
		predecessor.do { |item| 
			if (verbose.not) { 
				inval = item.embedInStream(inval); 
			} { 
				post("// returning starter: ");
				inval = item.postln.embedInStream(inval);
			}
		};
					// then if successors are found, yield those.
		while { nextVal = findNext.value; nextVal.notNil } { 
			if (verbose, { [\context, predecessor, \nextVal, nextVal].postcs });
			inval = nextVal.embedInStream(inval);
		};
		^inval;
	}

	find { |sublist| 
		var indexPairs = [];
			corpus.do({ |line, i|Ê 
				var found = line.findAll(sublist);  
				if (found.notNil) { 
					indexPairs = indexPairs ++ found.collect({ |where| [i, where] }) 
				};
			});
		^indexPairs;
	}
	
	// analysis 
	vocabulary {
		var voc = Set[]; 
		corpus.do (voc.addAll(_));
		^voc;
	}
	
	longestSnippets {  |sample| 

		var snippet, pairs, isEmpty=true, wasEmpty=true, prevSnip, prevPairs; 
		var allSnips = [], start = 0, end = 0; 
		
		while { 
			end = end.max(start);
			end < sample.size 
		} { 
			wasEmpty = isEmpty;
			prevPairs = pairs;
			prevSnip = snippet;

			snippet = sample.copyRange(start, end);
			pairs = this.find(snippet);
			isEmpty = pairs.isEmpty; 

			if (isEmpty and: wasEmpty.not) { 
				// post last valid combination
				allSnips = allSnips.add([start, end - 1, prevSnip, prevPairs]);
			};
			if (isEmpty) { start = start + 1 } { end = (end + 1) };
		};
			// add last if valid
		if (isEmpty.not) {
			//	"	//	adding last snippet: ".post;
			allSnips = allSnips.add([start, end, snippet, pairs]);
		};
		^allSnips;
	}
	
	isValidOutput { |sample, snippets|

		var allSnips = snippets ?? { this.longestSnippets(sample) };
		var numOverlaps = allSnips.size - 1;
		var shortOverlaps = Array.new(numOverlaps); 

		numOverlaps.do({ |snip, i| 
			shortOverlaps.add(allSnips[i][1] - allSnips[i+1][0] + 1)
		});

		shortOverlaps.postln;
		^shortOverlaps.every(_ > 0);
	}
	
	isNew { |sample|
		var foundAt = corpus
			.collect({ |txt, i| [i, txt.find(sample)] })
			.reject(_.includes(nil));
			
		if (foundAt.notEmpty) { 
			":::	Sample exists in corpus at:".format(sample).postln;
			foundAt.do { |pair|
				"\t[item, index]: % ".format(pair).postln;
			}
		};
		^foundAt.isEmpty
	}
}