LSys
{
	
	var <axiom, <currentAxiom, <rules, <>environ, 
		<parsedRules, <parsedCurAxiom, <ignores, <funcValsDict,
		<pcaStripped, <curAxiomLevels, <curRuleLevels;
	
	*new
	{|argAxiom, argRules, argIgnore, argEnvironment|
	
		^super.new.init(argAxiom, argRules, argIgnore, argEnvironment);
	}
	
	init
	{|argAxiom, argRules, argIgnore, argEnvironment|
	
		//no sanity check for now
		axiom = argAxiom;
		currentAxiom = argAxiom;
		rules = argRules;
		ignores = argIgnore ? "";
		environ = argEnvironment ? Environment.new;
		funcValsDict = Dictionary.new;
		curRuleLevels = List.new;
		
		
		this.parseRules;
		this.parseAxiom;
		this.bakeIgnores;
	}
	
	bakeIgnores
	{
		pcaStripped = parsedCurAxiom.copy;
		
		if(ignores.size > 0,
		{
			pcaStripped.do
			({|item, cnt|
				
				item.species.switch
				(
					Symbol,
					{
						if(ignores.includes(item.asString[0]), { pcaStripped[cnt] = '' });
					},
					Array,
					{
						if(ignores.includes(item[0].asString[0]), { pcaStripped[cnt] = '' });
					}
				);
			});
		});
	}
	
	parseRules
	{

		var tempOrder = List.new;
		
		//order: context sensitive rules come first
		
		rules.do
		({|rItem|
		
			if(rItem.key.includes(">"[0]) or: { rItem.key.includes("<"[0]) },
			{
				tempOrder.add(rItem);
			});
		});
		
		//check syntax for context sensitive rules, < should come first then >
		tempOrder.do
		({|rItem|
		
			if((rItem.key.count({|item| item == $>; }) > 1) or:
					{ (rItem.key.count({|item| item == $<; }) > 1) },
			{
				"Rule % has more than one < or >".format(rItem.asCompileString).error;
				^this.halt;
			});
				
			if(rItem.key.includes(">"[0]) and: { rItem.key.includes("<"[0]) },
			{				
				if(rItem.key.detectIndex({|item| item == $<; }) > 
					rItem.key.detectIndex({|item| item == $>; }),
				{
					"In rule %, > comes before <, and it's not allowed.".format(rItem).error;
					^this.halt;
				});
			});			
		});
		
		//add the context free rules
		rules.do
		({|rItem|
		
			if(rItem.key.includes(">"[0]).not and: { rItem.key.includes("<"[0]).not },
			{
				tempOrder.add(rItem);
			});
		});
		
		//tempOrder.postln;
		
		parsedRules = this.makeRules(tempOrder);
		//parsedRules = this.makeRules(rules); //unordered
		
		this.levelRules;
		
	}
	
	levelRules
	{
		var curLevel = 0;
		var tempLeveled = List.new;
		var tempRuleItem = List.new;
		//"in levelRules!".postln;
		parsedRules.do
		({|item|
			
			[0, 2].do
			({|lrIndex|
				if(item[lrIndex].notNil,
				{
					tempRuleItem = List.new;
					item[lrIndex].do
					({|rSegment|
						
						block
						({|break|
						
							if(rSegment == '[', //]
							{
								curLevel = curLevel + 1;
								tempRuleItem.add(curLevel);
								break.value;
							});
							
							if(rSegment == /*[*/']',
							{
								tempRuleItem.add(curLevel);
								curLevel = curLevel - 1;
								break.value;
							});
							
							tempRuleItem.add(curLevel);
						});
					});
					
					tempLeveled.add(tempRuleItem.asArray);
				},
				{
					tempLeveled.add(nil);
				});
			});
		});
		
		curRuleLevels = tempLeveled.asArray;
	}
	
	makeRules
	{|argRules|
		
		var madeRules = List.new;
		var parseRule;
		var inParens;
		var parensBuffer;
		//format:
		//[preceding, strict predecessor, succeeding, product]
		
		parseRule =
			{|inRule|
				
				var argRule = inRule;
				var parsedRule = List.new;
				var commaCount = 0;
				
				if(argRule.includes($().not, //)
				{
					argRule.as(Array).collect({|c| c.asString.asSymbol; });
				},
				{
					inParens = false;
					parensBuffer = "";
					
					argRule.do
					({|item, cnt|
					
						if(inParens.not,
						{
							if(item != $(, //)
							{
								parsedRule.add(item.asString.asSymbol);
							},
							{
								inParens = true;
							});
						},
						{
							if(item == $,,
							{
								commaCount = commaCount + 1;
							},
							{//(
								if(item == $),
								{
									inParens = false;
									parsedRule[parsedRule.size-1] = 
										parsedRule[parsedRule.size-1].bubble;
									
									parsedRule[parsedRule.size-1] = 
										parsedRule[parsedRule.size-1].add(commaCount + 1);
									commaCount = 0;
								});
							});
						});
					});
					
					parsedRule.asArray; //return
				});
				
				
			};
		
		argRules.do
		({|rule|
		
			block
			({|outerBreak|
			
				if(rule.key.includes(">"[0]).not and: { rule.key.includes("<"[0]).not },
				{//if totally context free
					madeRules.add([nil, parseRule.value(rule.key), nil, rule.value]);
					outerBreak.value;
				});
				
				if(rule.key.includes(">"[0]).not and: { rule.key.includes("<"[0]) },
				{
					rule.key = rule.key.split($<);
					madeRules.add(
						[
							parseRule.value(rule.key[0]), 
							parseRule.value(rule.key[1]), 
							nil,
							rule.value
						]);
					outerBreak.value;
				});
				
				if(rule.key.includes(">"[0]) and: { rule.key.includes("<"[0]).not },
				{
					rule.key = rule.key.split($>);
					madeRules.add(
						[
							nil,
							parseRule.value(rule.key[0]),
							parseRule.value(rule.key[1]),
							rule.value
						]);
					outerBreak.value;
				});
				
				if(rule.key.includes(">"[0]) and: { rule.key.includes("<"[0]) },
				{
					rule.key = rule.key.split($<);
					madeRules.add(
						[
							parseRule.value(rule.key[0]),
							parseRule.value(rule.key[1].split($>)[0]),
							parseRule.value(rule.key[1].split($>)[1]),
							rule.value
						]);
					outerBreak.value;
				});
			});
		});
		
		^madeRules.asArray;
	}
	
	parseAxiom
	{
		var tempParsed = List.new;
		var commaCount = 0;
		var inParens = false;
		var numBuffer = "";
		var numList = List.new;
		funcValsDict = Dictionary.new;
		
		currentAxiom.do
		({|aChar, cnt|
		
			if(inParens.not,
			{
				if(aChar != $(, //)
				{
					tempParsed.add(aChar.asString.asSymbol)
				},
				{
					inParens = true;
				});
			},
			{//(
				if(aChar != $),
				{
					if(aChar != $,,
					{
						numBuffer = numBuffer ++ aChar;
					},
					{
						numBuffer = numBuffer.asFloat;
						numList.add(numBuffer);
						commaCount = commaCount + 1;
						numBuffer = "";
					});
				},
				{
					numBuffer = numBuffer.asFloat;
					numList.add(numBuffer);
					numBuffer = "";
					
					tempParsed[tempParsed.size-1] = tempParsed[tempParsed.size-1].bubble;
					tempParsed[tempParsed.size-1] = 
						tempParsed[tempParsed.size-1].add(commaCount + 1);
					commaCount  = 0;
					funcValsDict.put(tempParsed.size-1, numList.asArray);
					numList = List.new;
					inParens = false;
				});
					
			});
		});
		
		parsedCurAxiom = tempParsed.asArray;
		this.levelAxiom;
	}
	
	levelAxiom
	{
		var tempLevels = List.new;
		var curLevel = 0;
		var branchID = 0;
		var openBranches = List[0];
		
		parsedCurAxiom.do
		({|item|
		
			block
			({|break|
			
				if(item == '[', //]
				{
					curLevel = curLevel + 1;
					branchID = branchID + 1;
					openBranches.add(branchID);
					tempLevels.add([curLevel, openBranches.last]);
					break.value;
				});
				
				if(item == /*[*/']',
				{
					tempLevels.add([curLevel, openBranches.last]);
					curLevel = curLevel - 1;
					openBranches.pop;
					break.value;
				});
				
				tempLevels.add([curLevel, openBranches.last]);
			});
		});
		
		curAxiomLevels = tempLevels.asArray;
	}
	
	applyRules
	{|argLevel = 1|
	
		var curMatch;
		var newStr;
		var curSegSize;
		var curSegment;
		var newString;
		var ruleApplied;
		var leftMatched, rightMatched;
		
		argLevel.do
		({
			//newStr = List.new;
			newString = List.new;
			ruleApplied = false;
			parsedCurAxiom.do
			({|aItem, aCnt|
			 
				block
				({|aBreak|
					parsedRules.do
					({|rItem, rCnt|
	
						ruleApplied = false;
						leftMatched = false;
						rightMatched = false;
						curSegSize = rItem[1].size;
						curSegment = parsedCurAxiom[aCnt..(aCnt+curSegSize-1)];
						if(curSegment == rItem[1],
						{
							rItem[0..2].do
							({|rSegment, rsCnt|
							
								if(rSegment.notNil,
								{
									rsCnt.switch
									(
										0,
										{
											if(this.checkLeft(aCnt, rSegment, curRuleLevels[0]),
											{
												leftMatched = true;
											});
										},
										1,
										{
											//already matched
										},
										2,
										{
											if(this.checkRight(aCnt+curSegSize-1, rSegment, curRuleLevels[1]),
											{
												rightMatched = true;
											});
										}
									);
								});
							}); //rItem.do
	
							if(rItem[0].isNil.or(leftMatched == true) and: { rItem[2].isNil.or(rightMatched == true) },
							{
								newString.add(this.returnMatched(aCnt, rItem, [leftMatched, rightMatched]));
								ruleApplied = true;
								aBreak.value;
							});					
						}); //if rule[1] matched
					}); //parsedRules.do
				}); //block abReak
				if(ruleApplied.not,
				{
					newString.add(this.returnUnmatched(aCnt));
				}); //0.01.wait;
			}); //parsedAxiom.do

			currentAxiom = newString.join;
			
			this.parseAxiom;
			this.bakeIgnores;
		}); //argLevel
		
		^currentAxiom;
	}
	
	checkLeft
	{|argAxIndex, argSegment, argSegLevel|
	
		var toCompare = List.new;
		var curIndex = argAxIndex - 1;
		var cmpItem;
		
		argSegLevel = argSegLevel.reject({|item, cnt| ignores.includes(argSegment[cnt].asString[0]).and(argSegment[cnt].species != Array); });
		argSegment = argSegment.reject({|item| ignores.includes(item.asString[0]).and(item.species != Array); });
		
		while({ (toCompare.size < argSegment.size).and(curIndex >= 0); },
		{
			cmpItem = pcaStripped[curIndex];
			
			if(cmpItem == '',
			{
				curIndex = curIndex - 1;
			},
			{
				if(curAxiomLevels[curIndex][1] == curAxiomLevels[argAxIndex][1],
				{
					toCompare.addFirst(cmpItem);
					curIndex = curIndex - 1;
				},
				{
					if((curAxiomLevels[curIndex][0] - argSegLevel[argSegment.size-1-toCompare.size]) < curAxiomLevels[argAxIndex][0],
					{
						toCompare.addFirst(cmpItem);
						curIndex = curIndex - 1;
						
					},
					{
						curIndex = curIndex - 1;
					});
				});
			});
		});
	
		if(argSegment == toCompare.asArray, { ^true; }, { ^false; });
	}
	
	checkRight
	{|argAxIndex, argSegment, argSegLevel|
	
		var toCompare = List.new;
		var curIndex = argAxIndex + 1;
		var cmpItem;
		
		argSegLevel = argSegLevel.reject({|item, cnt| ignores.includes(argSegment[cnt].asString[0]).and(argSegment[cnt].species != Array); });
		argSegment = argSegment.reject({|item| ignores.includes(item.asString[0]).and(item.species != Array); });
				
		while({ (toCompare.size < argSegment.size).and(curIndex < pcaStripped.size); },
		{
			cmpItem = pcaStripped[curIndex];
			
			if(cmpItem == '',
			{
				curIndex = curIndex + 1;
			},
			{
				if(curAxiomLevels[curIndex][1] == curAxiomLevels[argAxIndex][1],
				{
					toCompare.add(cmpItem);
					curIndex = curIndex + 1;
				},
				{
					if((curAxiomLevels[curIndex][0] - curAxiomLevels[argAxIndex][0] == argSegLevel[toCompare.size]) and:
						{ curAxiomLevels[curIndex][0] > curAxiomLevels[argAxIndex][0]; },
					{
						toCompare.add(cmpItem);
						curIndex = curIndex + 1;
					},
					{
					
						curIndex = curIndex + 1;
					});
				});
			});
		});
		
		if(argSegment == toCompare.asArray, { ^true; }, { ^false; });
	}
	
	returnUnmatched
	{|argIndex|
	
		var candid = parsedCurAxiom[argIndex];
		if(candid.species == Symbol,
		{//it's a char
			^candid.asString;
		},
		{//a its a function
			^(candid[0].asString ++ funcValsDict.at(argIndex).asString.replace(" ", "").replace("[","(").replace("]",")"));
		});
	}
	
	returnMatched
	{|argIndex, argRule, lrState|
	
		var old = parsedCurAxiom[argIndex];
		var funcArgs = List.new;
		
		if(old.species == Symbol,
		{
			^argRule[3];
		},
		{
			if(lrState[0], { funcArgs.add(funcValsDict.at(argIndex - 1)); }); //add left args
			funcArgs.add(funcValsDict.at(argIndex));
			if(lrState[1], { funcArgs.add(funcValsDict.at(argIndex + 1)); }); //add right args
			
			^(environ.use({argRule[3].value(*funcArgs.flat).asString}));
		});
	}
	
	giveParsedString
	{
		var tempResponse = List.new;
		
		parsedCurAxiom.do
		({|item, cnt|
		
			if(item.species == Symbol,
			{
				tempResponse.add(item);
			},
			{
				tempResponse.add([item[0], funcValsDict.at(cnt)]);
			});
		});
		
		^tempResponse;
	}
	
	axiom_
	{|argAxiom|
	
		axiom = argAxiom;
		currentAxiom = argAxiom;
		this.parseRules;
		this.parseAxiom;
		this.bakeIgnores;
	}
	
	rules_
	{|argRules|
	
		rules = argRules;
		this.parseRules;
		this.parseAxiom;
		this.bakeIgnores;		
	}
	
	ignores_
	{|argIgnores|
	
		ignores = argIgnores;
		this.parseRules;
		this.parseAxiom;
		this.bakeIgnores;
	}
}