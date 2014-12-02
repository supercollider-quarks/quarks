
	Library.put(\menuItems,'introspection','ClassBrowser',{
		Object.gui
	});

	Library.put(\menuItems,\test,'listen to audio busses',{
		BussesTool.new(Server.default)
	});

	Library.put(\menuItems,\introspection,\classfinder,{ 
		var close=true;
		Sheet.getString("Search classes...","",{ arg string;
			Sheet({ |layout|
				var matches,first;
				Object.allSubclasses.do({ |class|
					if(class.isMetaClass.not and: {class.name.asString.find(string,true).notNil},{
						matches = matches.add(class);
					});
				});
				if(matches.isNil,{
					("No classes matching " + string + "found").gui(layout);
				},{
					if(matches.size == 1,{
						matches.first.openCodeFile;
						if(close ? true,{layout.window.close; });
					},{
						matches.sortMap({|c| c.name }).do { arg class;
							var b;
							layout.startRow;
						    b = ActionButton(layout,class.name.asString,{ 
							    class.openCodeFile; 
							    if(close ? true,{layout.window.close; });
							},300);
							first = first ? b;
						}
					});
					if(first.notNil,{
					    first.focus
					});
				});
			},"Class search",Rect(500,800,500,500))
		},Rect(500,800,100,100))
	});

	Library.put(\menuItems,\introspection,\methodfinder,{
		Sheet.getString("Search methods...","",{ arg string;
			Sheet({ |layout|
				var matches;
				Object.allSubclasses.do({ |class|
					class.methods.do({ |meth|
						if(meth.name.asString.find(string,true).notNil,{
							matches = matches.add(meth,true);
							layout.startRow;
							MethodLabel(meth,layout,300);
						});
					})
				});
				if(matches.isNil,{
					("No methods matching " + string + "found").gui(layout);
				})
			},"Method search")
		})
	});

	Library.put(\menuItems,\introspection,\findRespondingMethodFor,{
		Sheet.getString("Search for: Class.methodname","",{ arg string;
			Sheet({ |layout|
				var class,method,methodName;
				# class, methodName = string.split($.);
				class = class.asSymbol.asClass;
				methodName = methodName.asSymbol;
				if(class.notNil,{
					while({ class != Meta_Object },{
						method = class.findMethod(methodName);
						if(method.notNil,{
							method.gui;
							class = Meta_Object
						 },{
						 	class = class.superclass;
						 });
					});
				},{
					("Class not found" + string).gui
				})
			},"Method search")
		})
	});

	Library.put(\menuItems,\introspection,\findReferencesToClass,{
		Sheet.getString("Class name:","",{ arg string;
			Class.findAllReferences(string.asSymbol).do({ arg ref;
				ref.postln;
			});
		});
	});

	Library.put(\menuItems,\introspection,'find class-not-found',{

			Class.allClasses.do({ arg class;
				(class.methods).do({ arg method;
					var selectors;
					selectors = method. selectors;
					selectors.do({ arg lit;
						if(lit.isKindOf(Symbol) and: { lit.isClassName },{
							if(lit.asClass.isNil,{
								lit.post;
								(30 - lit.asString.size).do({ " ".post; });
								(class.name.asString ++ ":" ++
									method.name.asString).postln;
							})
						})
					})
				})
			});
	});

	Library.put(\menuItems,\introspection,'find method-not-found',{
		/*
			search for potential method not found errors

			look through all methods for method selectors that
				are not defined for any class

			this finds many false cases,
			but i found a good 7 or 8 typos in mine and others code

			// could reject any spec names
			// any instr names
			// sound file types
		*/

		var allMethodNames;
		allMethodNames = IdentityDictionary.new;

		Class.allClasses.do({ arg class;
			(class.methods).do({ arg method;
				allMethodNames.put(method.name,true);
			})
		});

		Class.allClasses.do({ arg class;
			(class.methods).do({ arg method;
				var selectors;
				selectors = method.selectors;
				if(selectors.notNil,{
					selectors.do({ arg lit;
						if(lit.isKindOf(Symbol) and: { lit.isClassName.not },{
							if(allMethodNames.at(lit).isNil,{
								lit.post;
								(30 - lit.asString.size).do({ " ".post; });
								(class.name.asString ++ ":" ++ method.name.asString).postln;
							})
						})
					})
				});
			})
		});

	});

	//needs a tree browser
	Library.put(\menuItems,\introspection,'non-nil class variables',{
		Sheet({ arg f;
			Object.allSubclasses.do({ arg c,i;
				if(c.classVarNames.size > 0,{
					ClassNameLabel(c,f.hr);
					c.classVars.do({ arg cv,cvi;
						var iv;
						if(cv.notNil,{
							VariableNameLabel(c.classVarNames.at(cvi),f.startRow);
							//ClassNameLabel(cv.class,f);
							InspButton(cv,f);
						})
					});
				})
			})
		},"Classvars")
	});

	Library.put(\menuItems,\introspection,'find duplicate variable name errors',{
		var f;
		f = { arg class,found;
			class.subclasses.do({ arg sub;
				var full,myFound;
				myFound = if(found.isNil,{ List.new },{ found.copy });

				if(sub.instVarNames.as(IdentitySet).size != sub.instVarNames.size,{
					full = sub.instVarNames.reject(IsIn(myFound)).as(Array);
					full.removeAll( sub.instVarNames.as(IdentitySet) );
					myFound.addAll(full);
					if(full.notEmpty,{
						Post << sub << " has defined a variable with the same name as a superclass." << Char.nl;

						Post << full << Char.nl << Char.nl;
					});
				});
				f.value(sub,myFound);
			});
		};

		f.value(Object);
	});
		