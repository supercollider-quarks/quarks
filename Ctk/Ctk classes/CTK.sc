CtkObj {
	classvar <>latency = 0.1, <cond, <addActions;
	var <objargs;
	var <uniqueMethods;

	at {^this}

	addTo {arg aCtkScore;
		aCtkScore.add(this);
		^this;
		}

	addGetter {arg key, defaultVal;
		objargs.put(key.asSymbol, defaultVal);
		this.addUniqueMethod(key.asSymbol, {arg object; object.objargs[key]});
		}

	addSetter {arg key, func;
		this.addUniqueMethod((key.asString++"_").asSymbol,
//			{arg object, newval; object.objargs[key] = newval.value; object;
			{arg object, newval; object.objargs[key] = newval; func.value(object, newval); object;
			});
		}

	addMethod {arg key, func;
		objargs.put(key.asSymbol, func);
		this.addUniqueMethod(key.asSymbol, {arg object ... args;
			objargs[key].value(object, args);
			});
		}

	addParameter {arg key, defaultVal;
		defaultVal.isKindOf(Function).if({
			this.addMethod(key, defaultVal);
			}, {
			this.addGetter(key, defaultVal);
			this.addSetter(key);
			})
		^this;
		}

	addUniqueMethod { arg selector, function;
		var methodDict;
		if (uniqueMethods.isNil, { uniqueMethods = IdentityDictionary.new });
		uniqueMethods.put(selector, function);
	}

	doesNotUnderstand {arg selector ... args;
		(uniqueMethods[selector].notNil).if({
			^uniqueMethods[selector].value(this, *args);
			}, {
			^DoesNotUnderstandError(this, selector, args).throw;
			})
		}

	*initClass {
		cond = Condition.new;
		addActions = IdentityDictionary[
			\head -> 0,
			\tail -> 1,
			\before -> 2,
			\after -> 3,
			\replace -> 4,
			0 -> 0,
			1 -> 1,
			2 -> 2,
			3 -> 3,
			4 -> 4
			];
		}
	}

// a wrapper for Score... takes CtkEvents and calcs a pad time, sorts, etc.
CtkScore : CtkObj {

	var <endtime = 0, score, <buffers, <ctkevents, <ctkscores, <controls, notes, <others,
		<buffermsg, <buffersScored = false, <groups, oscready = false, <messages, <sds;
	var <masterScore, <allScores, <masterNotes, <masterControls, <masterBuffers,
		<masterGroups, <masterMessages, cmdPeriod;

	*new {arg ... events;
		^super.new.init(events);
		}

	init {arg events;
		masterScore = [];
		// I think this is where the mem leak comes in... all CtkObjs create this
		// Dictionary - and this is bad.
		objargs = Dictionary.new;
		score = Score.new;
		ctkscores = Array.new;
		buffers = Array.new;
		messages = Array.new;
		groups = Array.new;
		notes = Array.new;
		ctkevents = Array.new;
		controls = Array.new;
		others = Array.new;
		sds = Array.new;
		events.notNil.if({
			this.add(events);
			});
		}

	add {arg ... events;
		events.flat.do({arg event;
			case { // if the event is a note ...
				event.isKindOf(CtkNote)
				} {
				notes = notes.add(event);
				this.checkEndTime(event);
				} {
				event.isKindOf(CtkGroup);
				} {
				groups = groups.add(event);
				this.checkEndTime(event);
				} { // if the event is a buffer
				event.isKindOf(CtkBuffer);
				} {
				buffersScored.if({buffersScored = false});
				buffers = buffers.add(event);
				} {
				event.isKindOf(CtkEvent);
				} {
				ctkevents = ctkevents.add(event);
				this.checkEndTime(event);
				} {
				event.isKindOf(CtkControl);
				} {
				event.isScored.not.if({
					controls = controls.add(event);
					event.isScored = true;
					event.ctkNote.notNil.if({
						this.add(event.ctkNote);
						});
					this.checkEndTime(event);
					})
				} {
				event.isKindOf(CtkAudio);
				} {
				// do nothing, but don't complain either!
				} {
				event.isKindOf(CtkScore);
				} {
				ctkscores = ctkscores.add(event);
				buffers = buffers.addAll(event.buffers);
				this.checkEndTime(event);
				} {
				event.isKindOf(CtkMsg);
				} {
				messages = messages.add(event);
				this.checkEndTime(event);
				} {
				(event.isKindOf(CtkSynthDef) or: {event.isKindOf(CtkNoteObject)})
				} {
				sds = sds.add(event);
				} {
				event.isKindOf(CtkProtoNotes)
				} {
				event.synthdefs.do({arg thisSD;
					sds = sds.add(thisSD);
				})
				} {
				event.respondsTo(\messages);
				} {
				others = others.add(event);
				event.respondsTo(\endtime).if({
					this.checkEndTime(event)
					})
				} {
				true
				} {
				"It appears that you are trying to add a non-Ctk object to this score".warn;
				}
			});
			oscready.if({this.clearOSC});
		}

	clearOSC {
		oscready = false; score = Score.new;
		}

	checkEndTime {arg event;
		(event.endtime).notNil.if({
			(endtime < event.endtime).if({
				endtime = event.endtime
				})
			}, {
			// if an event only has a starttime, use that
			(endtime < event.starttime).if({
				endtime = event.starttime;
				})
			});
	}

	notes {arg sort = true;
		oscready.if({this.clearOSC});
		^notes.sort({arg a, b; a.starttime <= b.starttime});
		}

	notesAt {arg time, thresh = 0.0001;
		var notelist;
		notelist = this.notes;
		^notelist.select({arg me; me.starttime.fuzzyEqual(time, thresh) > 0})
		}

	score {
		this.saveToFile;
		^score.score;
		}

	saveToFile {arg path;
		score = Score.new;

		this.prepareObjects(false);
		this.groupTogether;
		this.objectsToOSC;
		sds.do({arg thisSD;
			thisSD.isKindOf(SynthDef).if({
				score.add([0, [\d_recv, thisSD.asBytes]]);
			}, {
				score.add([0, [\d_recv, thisSD.synthdef.asBytes]]);
			})
		});

		score.sort;
		score.add([endtime + 0.2, 0]);
		path.notNil.if({score.saveToFile(path)});
		}

	addBuffers {
		var data, chunk;
		buffersScored.not.if({
			buffersScored = true;
//			endtime = endtime + 0.1;
			buffers.do({arg me;
				this.add(CtkMsg(me.server, 0.0, me.bundle).bufflag_(true));
				this.add(CtkMsg(me.server, endtime + 0.1, me.freeBundle));
				me.collection.notNil.if({
					data = me.collection.collectAs({|item| item}, FloatArray);
					(data.size / 1024).floor.do({arg i;
						this.add(CtkMsg(me.server, 0.0, [\b_setn, me.bufnum, i * 1024, 1024] ++
							data[(i*1024).asInt..((i*1024)+1023).asInt]));
					});
					chunk = (data.size / 1024).floor * 1024;
					(data.size > chunk).if({
						this.add(CtkMsg(me.server, 0.0, [\b_setn, me.bufnum, chunk, data.size-chunk-1] ++
							data[chunk.asInt..(data.size-chunk-1).asInt]));
					});
				});
				(me.closeBundle.notNil).if({
					this.add(CtkMsg(me.server, endtime + 0.1, me.closeBundle));
					});
				});
			})
		}

	freeGroups {
		masterGroups.do({arg me;
			me.freeAll(endtime)
			})
		}
	// builds everything except the buffers since they act
	// different in NRT and RT

	prepareObjects {arg rt = false;
		var eventArray, allReleases, theseReleases;
		var time, argname, argval;
		masterNotes = Array.new;
		masterControls = Array.new;
		masterBuffers = Array.new;
		masterGroups = Array.new;
		masterMessages = Array.new;
		allReleases = Array.new;
		allScores = [];
		rt.not.if({
			this.addBuffers;
			});
		this.concatScores(this);
		ctkevents.do({arg thisctkev;
			allScores = allScores.add(thisctkev.score)
			});
		allScores.do({arg thisscore;
			this.grabEvents(thisscore.groups, thisscore.notes,
				thisscore.controls, thisscore.buffers, thisscore.messages);
			});
//		rt.not.if({
//			this.addBuffers;
//			});
//		masterMessages = masterMessages ++ messages;
/*		this.freeGroups;
		masterGroups.do({arg thisgroup;
			(thisgroup.messages.size > 0).if({
				thisgroup.messages.do({arg me;
					masterMessages = masterMessages.add(me);
					})
				});
			});*/
		masterControls.do({arg thiscontrol;
			(thiscontrol.messages.size > 0).if({
				thiscontrol.messages.do({arg me;
					masterMessages = masterMessages.add(me);
					})
				});
			});
		masterNotes.do({arg thisnote;
			var bundle, endmsg, oldval, refsSort;
			endmsg = thisnote.getFreeMsg;
			endmsg.notNil.if({
				this.checkEndTime(endmsg);
				masterMessages = masterMessages.add(endmsg);
				});
			thisnote.refsDict.do({arg key, val;
				var tmpdur;
				refsSort = key.value.sort({arg a, b; a[0] < b[0]});
				refsSort.do({arg me;
					#time, argname, argval = me;
					case
						{argval.isKindOf(SimpleNumber)}
						{
							thisnote.set(time, argname, argval)
						}
						{argval.isKindOf(CtkControl) and:
							{argval.isScored.not or: {
								argval.isARelease}}}
						{
							argval.setStarttime(time + thisnote.starttime);
							tmpdur = thisnote.endtime - time - thisnote.starttime;
							(argval.duration.isNil or: {argval.duration > tmpdur}).if({
								argval.duration_(tmpdur);
								});
							thisnote.releases = thisnote.releases.add(argval);
							argval.isARelease = true;
							thisnote.noMaps.indexOf(argname).notNil.if({
								thisnote.set(time, argname, argval)
								}, {
								thisnote.map(time, argname, argval)								})
							}
						{argval.isKindOf(CtkBus)}
						{
							thisnote.noMaps.indexOf(argname).notNil.if({
								thisnote.set(time, argname, argval.asUGenInput)
								}, {
								argval.isKindOf(CtkControl).if({
									thisnote.map(time, argname, argval.asUGenInput)
									}, {
									thisnote.mapa(time, argname, argval.asUGenInput)
									})
								})
						}
						{true}
						{thisnote.set(time, argname, argval)}
					});
				});
			(thisnote.messages.size > 0).if({
				thisnote.messages.reverseDo({arg me;
					this.checkEndTime(me);
					masterMessages = masterMessages.add(me);
					})
				});
			});
		theseReleases = this.collectReleases(masterNotes);
		while({
			allReleases = allReleases ++ theseReleases;
			theseReleases = this.collectReleases(theseReleases);
			theseReleases.size > 0;
			});
		allReleases.do({arg thisnote;
			var endmsg;
			endmsg = thisnote.getFreeMsg;
			endmsg.notNil.if({
				masterMessages = masterMessages.add(endmsg);
				});
			(thisnote.messages.size > 0).if({
				thisnote.messages.do({arg me;
					masterMessages = masterMessages.add(me);
					})
				});
			});
		masterNotes = masterNotes ++ allReleases;
		this.freeGroups;
		masterGroups.do({arg thisgroup;
			(thisgroup.messages.size > 0).if({
				thisgroup.messages.do({arg me;
					masterMessages = masterMessages.add(me);
					})
				});
			});
//		rt.if({
//			"Not rt!".postln;
//			masterBuffers.do({arg thisbuffer;
//				(thisbuffer.messages.size > 0).if({
//					thisbuffer.messages.do({arg me;
//						masterMessages = masterMessages.add(me);
//						})
//					})
//				});
//		});
		}

	collectReleases {arg noteCollect;
		var raw, rels, ctkns;
		raw = noteCollect.collect({arg me;
			me.releases;
			});
		rels = raw.select({arg me;
			me.size > 0;
			});
		ctkns = rels.flat.collect({arg me;
			me.ctkNote});
		^ctkns = ctkns.select({arg me; me.notNil});
		}

	grabEvents {arg thesegroups, thesenotes, thesecontrols, thesebuffers, thesemessages;
		masterGroups = masterGroups ++ thesegroups.collect({arg me; me});
		masterNotes = masterNotes ++ thesenotes.collect({arg me; me});
		masterBuffers = masterBuffers ++ thesebuffers.collect({arg me; me});
		masterControls = masterControls ++ thesecontrols.collect({arg me; me});
		masterMessages = masterMessages ++ thesemessages.collect({arg me; me});
		}

	groupTogether {
		masterScore = masterGroups ++ masterNotes ++ masterMessages;
		masterScore.sort({arg a, b;
			a.starttime < b.starttime;
			});
		masterScore = masterScore.separate({arg a, b;
			a.starttime.fuzzyEqual(b.starttime, 1.0e-07) == 0;
			});
		masterScore.do({arg thisTimesEvents;
			(thisTimesEvents.size > 1).if({
				thisTimesEvents.sort({arg a, b;
					((b.target == a) or: {a.isKindOf(CtkMsg) and: {a.bufflag}});
					})
				})
			});
		}

	concatScores {arg aScore;
		(aScore.ctkscores.size > 0).if({
			aScore.ctkscores.do({arg me;
				this.concatScores(me);
				});
			});
		this.checkEndTime(aScore);
		allScores = allScores.add(aScore);
		}

	getNotes {arg aCtkControl;
		aCtkControl.ctkNotes.notNil.if({
			this.add(aCtkControl.ctkNotes);
			})
		}

	// create the OSCscore, load buffers, play score
	play {arg server, clock, quant = 0.0, startPoint = 0.0, endPoint = -1.0;
		server = server ?? {Server.default};
		endtime = endtime + 0.2;
		server.boot;
		server.waitForBoot({
			score = Score.new;
			Routine.run({
				this.loadBuffers(server, clock, quant);
				this.prepareObjects(true);
				this.groupTogether;
				this.objectsToOSC;
				latency.wait;
				server.sync(cond);
				(startPoint > 0.0).if({
					(endPoint > startPoint).if({
						score = score.section(startPoint, endPoint);
						endtime = endPoint - startPoint;
						}, {
						score = score.section(startPoint);
							endtime = endtime - startPoint + 0.2;
					})

				});
				score.play;
				cmdPeriod = {
					var items;
					items = [masterBuffers, masterScore, masterControls].flat;
					items.do({arg me;
						me.free(addMsg: false);
						me.isKindOf(CtkNote).if({
							me.releases.do({arg thisRel; thisRel.free;})
							})
						});
					CmdPeriod.remove(cmdPeriod);
					cmdPeriod = nil;
					};
				CmdPeriod.add(cmdPeriod);
				SystemClock.sched(endtime, cmdPeriod);
				})
			})
		}

	objectsToOSC {
		masterScore.do({arg thisTimeEvent;
			var offset, block, thisBundle, thisTime = thisTimeEvent[0].starttime;
			var tmpBundle, tmp;
			block = 0;
			offset = 1.0e-07;
			thisBundle = [];
			thisTimeEvent.do({arg me;
				thisBundle = thisBundle ++ me.msgBundle;
				});
			(thisBundle.bundleSize < 4096).if({
				thisBundle = thisBundle.addFirst(thisTime);
				score.add(thisBundle);
				}, {
				tmpBundle = [];
				while({
					thisBundle.size > 0
					}, {
					// remove a message
					tmp = thisBundle.removeAt(0);
					// check if tmpBundle is above our desired size first
					((tmpBundle ++ tmp).bundleSize > 4096).if({
						tmpBundle = tmpBundle.addFirst(thisTime + (block * offset));
						score.add(tmpBundle);
						tmpBundle = [];
						tmpBundle = tmpBundle.add(tmp);
						block = block + 1;
						}, {
						tmpBundle = tmpBundle.add(tmp);
						});
					});
				tmpBundle = tmpBundle.addFirst(thisTime + (block * offset));
				score.add(tmpBundle);
				});
			});
		oscready = true;
		}

	// make collections work here
	loadBuffers {arg server, clock, quant;
		var data, chunk;
		(buffers.size > 0).if({
			buffers.do({arg thisBuf;
				thisBuf.messages.do({arg thisMsg;
					this.add(thisMsg);
					});
				this.add(CtkMsg(server, endtime, thisBuf.freeBundle));
				(thisBuf.closeBundle.notNil).if({
					this.add(CtkMsg(server, endtime, thisBuf.closeBundle));
					});
				thisBuf.load(sync: true);
				});
			});
		latency.wait;
		server.sync(cond);
		}

	// SC2 it! create OSCscore, add buffers to the score, write it
	write {arg path, duration, sampleRate = 44100, headerFormat = "AIFF",
		sampleFormat = "int16", options, action, inputFilePath, oscFilePath;
		var tmpfile, stamp;
		oscFilePath.isNil.if({
			stamp = Date.seed;
			tmpfile = (thisProcess.platform.name == \windows).if({
				"/windows/temp/trashme" ++ stamp;
			}, {
				"/tmp/trashme" ++ stamp;
			});
		},{
			tmpfile = oscFilePath;
		});
		this.saveToFile;
		score.recordNRT(tmpfile, path, inputFilePath, sampleRate: sampleRate,
			headerFormat: headerFormat,
		 	sampleFormat: sampleFormat, options: options, duration: duration,
		 	action: action);
		}
	// add a time to all times in a CtkScore
	/* will probably have to add events and controls here soon */
	/* returns a NEW score with the events offset */

	offset {arg duration;
		var items;
		// all but buffers
		items = notes ++ groups ++ messages ++ controls ++ ctkevents ++ others;		items.do({arg me;
			me.setStarttime(me.starttime + duration);
			});
		endtime = endtime + duration;
		}

	// copying can be problematic - dependencies can be lost
	copy {
		var newScore, newNote;
		newScore = CtkScore.new;
		this.items.do({arg me;
			me.isKindOf(CtkNote).if({
				newScore.add(me.copy(me.starttime));
				}, {
				newNote = me.deepCopy;
				newNote.server = me.server; // deepCopy changes the server! This can be bad.
				newScore.add(newNote);
				})
			});
		^newScore;
		}

	items {^notes ++ groups ++ messages ++ controls ++ ctkevents ++ buffers ++ others }

	merge {arg newScore, newScoreOffset = 0;
		var addScore;
		addScore = newScore.offset(newScoreOffset);
		this.add(addScore.items ++ addScore.ctkscores);
		}
}
// creates a dictionary of Synthdefs, and CtkNoteObjects
CtkProtoNotes {
	var <synthdefs, <dict;
	*new {arg ... synthdefs;
		^super.newCopyArgs(synthdefs).init;
		}

	init {
		dict = Dictionary.new;
		this.addToDict(synthdefs);
		}

	// load and add to the dictionary
	addToDict {arg sds;
		sds.do({arg me;
			case
				{me.isKindOf(SynthDef)}
				{dict.add(me.name.asSymbol -> CtkNoteObject.new(me))}
				{me.isKindOf(SynthDescLib)}
				{me.read;
				me.synthDescs.do({arg thissd;
					dict.add(thissd.name.asSymbol -> CtkNoteObject.new(thissd.name.asSymbol))
					});
				}
			})
		}

	load {arg ... servers;
		servers.do({arg aServer;
			synthdefs.do({arg me; me.load(aServer)});
		})
	}

	send {arg ... servers;
		servers.do({arg aServer;
			synthdefs.do({arg me; me.send(aServer)});
		})
	}

//	add {
//		synthdefs.do({arg me; me.add;})
//	}

	at {arg id;
		^dict[id.asSymbol]
		}

	add {arg ... newsynthdefs;
		synthdefs = synthdefs ++ newsynthdefs;
		this.addToDict(newsynthdefs);
		}
}


CtkNoteObject {
	var <synthdef, <server, <synthdefname, args, <noMaps, isPlaying, score;

	*new {arg synthdef, server;
		^super.newCopyArgs(synthdef, server).init;
		}

	init {
		var sargs, sargsdefs, sd, count, tmpar, namesAndPos, sdcontrols, tmpsize, kouts;
		case
			{
			synthdef.isKindOf(SynthDef)
			}{
			this.buildControls;
			}{
			// if a string or symbol is passed in, check to see if SynthDescLib.global
			// has the SynthDef
			(synthdef.isKindOf(String) || synthdef.isKindOf(Symbol) ||
				synthdef.isKindOf(SynthDesc))
			}{
			synthdef.isKindOf(SynthDesc).if({
				sd = synthdef;
				}, {
				sd = SynthDescLib.global.at(synthdef);
				});
			sd.notNil.if({
				// check if this is a SynthDef being read from disk... if it is, it
				// has to be handled differently
				sd.def.allControlNames.notNil.if({
					synthdef = sd.def;
					this.buildControls;
					}, {
					synthdef = sd.def;
					args = IdentityDictionary.new;
					synthdefname = synthdef.name;
					count = 0;
					namesAndPos = [];
					sd.controls.do({arg me, i;
						(me.name != '?').if({
							namesAndPos = namesAndPos.add([me.name, i]);
							});
						});
					sdcontrols = namesAndPos.collect({arg me, i;
						(i < (namesAndPos.size - 1)).if({
							tmpsize = namesAndPos[i + 1][1] - me[1];
							[me[0].asSymbol, (tmpsize > 1).if({
								(me[1]..(namesAndPos[i+1][1] - 1)).collect({arg j;
									sd.controls[j].defaultValue;
									})
								}, {
								sd.controls[me[1]].defaultValue;
								})]
							}, {
							tmpsize = sd.controls.size - 1 - me[1];
							[me[0].asSymbol, (tmpsize > 1).if({
								(me[1] .. (sd.controls.size) - 1).collect({arg j;
									sd.controls[j].defaultValue;
									}, {
									sd.controls[me[1]].defaultValue;
									})
								})]
							})
						});
					sdcontrols.do({arg me;
						var name, def;
						#name, def = me;
						args.add(name -> def);
						})
					});
					// no maps keeps Out.kr output vars from being mapped to
					kouts = sd.outputs.collect({arg me; me.startingChannel.source});
					kouts.removeAllSuchThat({arg item; item.isKindOf(String).not});
					noMaps = kouts.collect({arg item; item.asSymbol});
				},{
				"The SynthDef id you requested doesn't appear to be in your global SynthDescLib. Please .memStore your SynthDef, OR run SynthDescLib.global.read to read the SynthDesscs into memory".warn
				})
			}
		}

	// for loading directly to a specific server
	load {arg ... servers;
		server.do({arg aServer;
			synthdef.load(aServer)
		})
		}

	send {arg ... servers;
		server.do({arg aServer;
			synthdef.send(aServer)
		})
		}

	add {
		synthdef.add;
	}

	buildControls {
		var kouts;
		synthdef.load(server ?? {Server.default});
		args = IdentityDictionary.new;
		synthdefname = synthdef.name;
		synthdef.allControlNames.do({arg ctl, i;
			var def, name = ctl.name;
			def = ctl.defaultValue ?? {
				(i == (synthdef.allControlNames.size - 1)).if({
					synthdef.controls[ctl.index..synthdef.controls.size-1];
					}, {
					synthdef.controls[ctl.index..synthdef.allControlNames[i+1].index-1];
					})
				};
			args.add(name -> def);
			});
		noMaps = synthdef.children.collect({arg me;
			(((me.rate == \control) or: {me.rate == \audio})
				and: {me.isKindOf(AbstractIn) or: {me.isKindOf(AbstractOut)}}).if({
					((me.class != LocalIn) and: {(me.class != LocalOut)}).if({
						me.inputs[0].isKindOf(OutputProxy).if({me.inputs[0].name}, {nil});
						}, {
						nil
						})
				})});
		noMaps.removeAllSuchThat({arg item; item.isNil});
		}

	// create an CtkNote instance
	new {arg starttime = 0.0, duration, addAction = 0, target = 1, server;
		this.deprecated(thisMethod, this.class.findRespondingMethodFor(\note));
		^this.note(starttime, duration, addAction, target, server, synthdefname, noMaps)
//			.args_(args.deepCopy);
		}

	note {arg starttime = 0.0, duration, addAction = 0, target = 1, server;
		^CtkNote.new(starttime, duration, addAction, target, server, synthdefname, noMaps)
			.args_(args.deepCopy);
		}

	args {arg post = true;
		post.if({
			("Arguments and defaults for SynthDef "++synthdefname.asString++":").postln;
			args.keysValuesDo({arg key, val;
				("\t"++key++" defaults to "++val).postln;
				});
			});
		^args;
		}

	tester {arg server;
		var paramDict, durBox, playButton, clock;
		var w, x, y, z, nChans;
		server = server ?? {Server.default};

		w = Window.new("Controls for: "++synthdefname, Rect(100, 600, 1000, 400));
		w.view.decorator_(x = FlowLayout(w.bounds, 10@10, 20@5));
		w.front;
		paramDict = Dictionary.new;

		playButton = Button(w, 50@20)
			.states_([
				["Play", Color.green, Color.black],
				["Stop", Color.red, Color.black]
				])
			.action_({arg me;
				this.playFunc(server, me.value, false, nil, playButton, durBox, paramDict, nChans)
				});

		StaticText(w, 90@20)
			.string_("Duration:");

		durBox = NumberBox(w, 50@20)
			.value_(1);

		x.nextLine;

		Button(w, 50@20)
			.states_([
				["Render", Color.green, Color.black]
				])
			.action_({arg me;
				CocoaDialog.savePanel({arg path;
					this.playFunc(server, 1, true, path, playButton, durBox, paramDict, nChans);
					}, {
					"No file saved".warn
					})
				});

		StaticText(w, 90@20)
			.string_("Num Channels:");

		nChans = NumberBox(w, 50@20)
			.value_(2);

		x.shift(60, 0);

		Button(w, 100@20)
			.states_([
				["Draw Buffer", Color.green, Color.black]
				])
			.action_({
				this.createBuffer
				});

		x.nextLine;

		x.shift(310, 0);

		StaticText(w, 200@20)
			.string_("Env or path to a buffer");
		StaticText(w, 40@20)
			.string_("Scale");
		StaticText(w, 40@20)
			.string_("Bias");
		StaticText(w, 50@20)
			.string_("TScale");
		StaticText(w, 40@20)
			.string_("Freq");
		StaticText(w, 40@20)
			.string_("Low");
		StaticText(w, 40@20)
			.string_("High");
		StaticText(w, 40@20)
			.string_("Phase");

		x.shift(0, 30);

		z = ScrollView(w, Rect.new(0, 0, w.bounds.width, w.bounds.height-60)) ;
		z.decorator_(y = FlowLayout(z.bounds, 10@10, 20@5));
		args.keysValuesDo({arg key, val;
			paramDict.add(key -> Dictionary.new);
			StaticText(z, 60@20)
				.string_(key.asString);
			paramDict[key].add("default" ->
				NumberBox(z, 40@20)
					.value_(val)
					);
			StaticText(z, 40@20)
				.string_("Control");
			paramDict[key].add("control" ->
				PopUpMenu(z, 70@20)
					.items_(["none", "env", "k-buffer", "playbuf", "diskin",
						"LFNoise0", "LFNoise1", "LFNoise2",
						"SinOsc", "Impulse", "LFSaw", "LFPar", "LFTri", "LFCub"])
					.action_({arg me;
						me.value;
						});
				);
			paramDict[key].add("env" ->
				TextView(z, 200@20)
				);
			paramDict[key].add("scale" ->
				NumberBox(z, 40@20)
					.value_(1)
				);
			paramDict[key].add("bias" ->
				NumberBox(z, 40@20)
					.value_(0)
				);
			paramDict[key].add("tscale" ->
				NumberBox(z, 40@20)
					.value_(1)
				);
			paramDict[key].add("freq" ->
				NumberBox(z, 40@20)
					.value_(1)
				);
			paramDict[key].add("low" ->
				NumberBox(z, 40@20)
					.value_(-1)
				);
			paramDict[key].add("high" ->
				NumberBox(z, 40@20)
					.value_(1)
				);
			paramDict[key].add("phase" ->
				NumberBox(z, 40@20)
					.value_(0)
				);
			y.nextLine;
			});
		}

	playFunc {arg server, val, nrt = false, path, playButton, durBox, paramDict, nChans;
		var func, str, buf, note, clock;
		func = case
			{val == 0}
			{isPlaying.if({isPlaying = false; server.freeAll; clock.clear; clock.stop;
				score.buffers.do({arg me; me.free})})}
			{val == 1}
			{
				score = CtkScore.new;
				note = this.new;
				note.setDuration(durBox.value);
				note.args.keys.do({arg key;
					case
						{(paramDict[key]["control"].value == 0)}
						{note.perform((key++"_").asSymbol, paramDict[key]["default"].value)}
						{(paramDict[key]["control"].value == 1)}
						{
							str = paramDict[key]["env"].string.interpret;
							str.isKindOf(Env).if({
								note.perform((key++"_").asSymbol,
									CtkControl.env(str, levelScale: paramDict[key]["scale"].value,
										levelBias: paramDict[key]["bias"].value, timeScale:
										paramDict[key]["tscale"].value))
									}, {
									"Parameter for "++key++" does not appear to be an Env".warn;
									})

						}
						{(paramDict[key]["control"].value == 2)}
						{
							str = paramDict[key]["env"].string.interpret;
							PathName(str).isFile.if({
								score.add(buf = CtkBuffer.playbuf(str));
								note.perform((key++"_").asSymbol,
									CtkControl.kbuf(buf, levelScale: paramDict[key]["scale"].value,
										levelBias: paramDict[key]["bias"].value,
										timeScale: paramDict[key]["tscale"].value))

								});
						}
						{(paramDict[key]["control"].value == 3)}
						{
							str = paramDict[key]["env"].string.interpret;
							PathName(str).isFile.if({
								score.add(buf = CtkBuffer.playbuf(str));
								note.perform((key++"_").asSymbol, buf);
								});
						}
						{(paramDict[key]["control"].value == 4)}
						{
							str = paramDict[key]["env"].string.interpret;
							PathName(str).isFile.if({
								score.add(buf = CtkBuffer.diskin(str));
								note.perform((key++"_").asSymbol, buf);
								});
						}
						{(paramDict[key]["control"].value > 4)}
						{	note.perform((key++"_").asSymbol,
								CtkControl.lfo(paramDict[key]["control"]
										.items[paramDict[key]["control"].value].interpret,
									paramDict[key]["freq"].value,
									paramDict[key]["low"].value,
									paramDict[key]["high"].value,								paramDict[key]["phase"].value))
						}
						;
				});
				score.add(note);
				nrt.if({
					score.write(path, options: ServerOptions.new.numOutputBusChannels_(nChans.value))
					}, {
					clock = TempoClock.new;
					score.play;
					isPlaying = true;
					clock.sched(durBox.value + 0.2, {
						{(playButton.value != 0).if({playButton.valueAction_(0)})}.defer});
					})
			};
		func.value;
		}

	createBuffer {
		var bufEditWindow, bufEditDec, plot;
		bufEditWindow = Window.new("A Plot Buffer", Rect(400, 500, 400, 300));
		bufEditWindow.front;
		plot = Plot.newClear(100, parent: bufEditWindow);
		bufEditWindow.bounds_(Rect(400, 500, 460, 340));
		Button(bufEditWindow, Rect(20, 300, 70, 20))
			.states_([
				["Lowpass", Color.green, Color.black]
				])
			.action_({plot.data_(plot.data.lowpass)});
		Button(bufEditWindow, Rect(100, 300, 70, 20))
			.states_([
				["Save", Color.green, Color.black]
				])
			.action_({
				CocoaDialog.savePanel({arg path;
					plot.saveToSF(path)
					}, {
					"No Plot was saved".warn
					})
				})
		}
}

CtkSynthDef : CtkNoteObject {
	*new {arg name, ugenGraphFunc, rates, prependArgs, variants;
		var synthdef;
		synthdef = SynthDef(name, ugenGraphFunc, rates, prependArgs, variants);
		^super.new(synthdef);
		}
	}

CtkNode : CtkObj {
	classvar /*<addActions, */<nodes, <servers, <resps, <cmd, <groups;

	var <addAction, <target, <>server;
	var >node, <>messages, <starttime, <>willFree = false;//, <group;
	var <isPaused = false, <>releases, <>releaseFunc, <>onReleaseFunc;

	*new {
		^super.new.initCtkNode;
	}

	initCtkNode {
		server = server ?? {Server.default};
		servers[server].isNil.if({
			servers.add(server -> server);
			nodes.add(server -> []);
			groups.add(server -> []);
			resps.add(server -> OSCresponderNode(server.addr, '/n_end', {arg time, resp, msg, addr;
				var tag, nodeID, prevNodeID, nextNodeID, group, headNode, tailNode;
				#tag, nodeID, prevNodeID, nextNodeID, group, headNode, tailNode = msg;
				group = (group == 1);
				nodes[server].remove(msg[1]);
				groups[server].do({arg me;
					me.notNil.if({
						me.children.remove(msg[1]);
						me.noteDict.removeAt(msg[1]);
						})
					});
				}).add);
		});
		cmd.if({cmd = false; CmdPeriod.doOnce({this.cmdPeriod})});
	}

	node {
		^node ?? {node = server.nextNodeID};
		}

	// server is the server assigned already to an object or. If
	// an object is inited without a server, Server.default is used
	watch {arg group;
		var thisidx;
		nodes[server] = nodes[server].add(node);
		group.isKindOf(CtkGroup).if({
			this.addGroup(group);
			group.noteDict.add(node -> this);
			group.children = group.children.add(node);
			});
		}

	setStarttime {arg newStarttime;
		starttime = newStarttime;
		}

	addGroup {arg group;
		var idx;
		//groups.postln;
		groups[server].indexOf(group).isNil.if({
			groups[server] = groups[server].add(group)
			});
		}

	isPlaying {
		var idx;
//		this.addServer;
		(servers.size > 0).if({
//			idx = servers.indexOf(server);
			(nodes[server].notNil && nodes[server].includes(node)).if({^true}, {^false});
			}, {
			^false
			})
		}

	before {arg time, target;
		var bund;
		bund = [\n_before, this.node.asUGenInput, target.node.asUGenInput];
		this.handleMsg(time, bund);
	}

	after {arg time, target;
		var bund;
		bund = [\n_after, this.node.asUGenInput, target.node.asUGenInput];
		this.handleMsg(time, bund);
	}

	cmdPeriod {
		resps.do({arg me; me.remove});
		resps = Dictionary.new;
		servers = Dictionary.new;
		nodes = Dictionary.new;
		this.isKindOf(CtkGroup).if({
			this.isGroupPlaying = false;
		});
		cmd = true;
		}

	set {arg time, key, value;
		var bund;
		bund = [\n_set, this.node, key, value.asUGenInput];
		this.handleMsg(time, bund);
		}

	setn {arg time, key ... values;
		var bund;
		values = values.flat.perform(\asUGenInput);
		bund = [\n_setn, this.node, key, values.size] ++ values;
		this.handleMsg(time, bund);
		}

	map {arg time, key, value;
		var bund;
		bund = [\n_map, this.node, key, value.asUGenInput];
		this.handleMsg(time, bund);
		}

	mapn {arg time, key ... values;
		var bund;
		values = values.flat;
		bund = [\n_mapn, this.node, key, values.size] ++ values.collect({arg me; me.bus});
		this.handleMsg(time, bund);
		}

	mapa {arg time, key, value;
		var bund;
		bund = [\n_mapa, this.node, key, value.asUGenInput];
		this.handleMsg(time, bund);
		}

	mapan {arg time, key ... values;
		var bund;
		values = values.flat;
		bund = [\n_mapan, this.node, key, values.size] ++ values.collect({arg me; me.bus});
		this.handleMsg(time, bund);
		}

	handleMsg {arg time, bund;
		this.isPlaying.if({ // if playing... send the set message now!
			time.notNil.if({
				SystemClock.sched(time, {
					server.sendBundle(latency, bund);
					});
				}, {
				server.sendBundle(latency, bund);
				})
			}, {
			starttime = starttime ?? {0.0};
			time = time ?? {0.0};
			messages = messages.add(CtkMsg(server, starttime + time, bund));
			})
	}

	release {arg time, key = \gate;
		this.set(time, key, 0);
		willFree = true;
		(((releases.size > 0) or: {onReleaseFunc.notNil or: {releaseFunc.notNil}}) and: {this.isPlaying}).if({
			Routine.run({
				(time ?? 0).wait;
				onReleaseFunc.value;
				while({
					0.1.wait;
					this.isPlaying.not.if({
						releaseFunc.value(this);
						(releases.size > 0).if({
							releases.do({arg me; me.free})
							});
						});
					this.isPlaying;
					})
				});
			});
		^this;
		}

	// immeditaely kill the node
	free {arg time = 0.0, addMsg = true;
		var bund;
		bund = [\n_free, this.node];
		willFree = true;
		this.isPlaying.if({
			onReleaseFunc.value;
			SystemClock.sched(time, {
				releaseFunc.value;
				server.sendBundle(latency, bund);
				(releases.size > 0).if({
					releases.do({arg me;
						me.free;
						})
					});
				});
			}, {
			addMsg.if({
				messages = messages.add(CtkMsg(server, time+starttime, bund));
				})
			})
		}

	pause {
		this.isPlaying.if({
			isPaused.not.if({
				server.sendMsg(\n_run, node, 0);
				isPaused = true;
				})
			})
		}

	run {
		this.isPlaying.if({
			isPaused.if({
				server.sendMsg(\n_run, node, 1);
				isPaused = false;
				})
			})
		}

	asUGenInput {^node ?? {this.node}}
	asControlInput {^node ?? {this.node}}

	*initClass {
		nodes = Dictionary.new; // [];
		servers = Dictionary.new; // []
		resps = Dictionary.new; // [];
		cmd = true;
		groups = Dictionary.new; // [];
		}
	}

// these objects are similar to the Node, Synth and Buffer objects, except they are used to
// create Scores and don't directly send messages to the Server

CtkNote : CtkNode {

	var <duration, <synthdefname,
		<endtime, <args, <setnDict, <mapDict, <noMaps, automations, <refsDict;

	*new {arg starttime = 0.0, duration, addAction = 0, target = 1, server, synthdefname, noMaps;
//		server = server ?? {Server.default};
		^super.newCopyArgs(Dictionary.new, nil, addAction, target, server)
			.initCN(starttime, duration, synthdefname, noMaps)
			.initCtkNode
		}

	copy {arg newStarttime;
		var newNote;
		newStarttime = newStarttime ?? {starttime};
		newNote = this.deepCopy;
		newNote.server_(server);
		newNote.setStarttime(newStarttime);
		newNote.messages = Array.new;
		newNote.node_(nil);
		newNote.args_(args.deepCopy);
		^newNote;
		}

	initCN {arg argstarttime, argduration, argsynthdefname, argnoMaps;
		server = server ?? {Server.default};
		target.isKindOf(CtkNode).if({
			(target.server != server).if({
				"Mismatch between target Server and this CtkNode's server, setting to target's server".warn;
				server = target.server;
			})
		});
		starttime = argstarttime;
		duration = argduration;
		synthdefname = argsynthdefname;
		node = nil;
		noMaps = argnoMaps;
		messages = Array.new;
		(duration.notNil && (duration != inf)).if({
			endtime = starttime + duration;
			});
		setnDict = Dictionary.new;
		mapDict = Dictionary.new;
		refsDict = Dictionary.new;
		releases = [];
		automations = [];
		}

	setStarttime {arg newstart;
		starttime = newstart;
		releases.do({arg me; me.setStarttime(newstart)});
		(duration.notNil && (duration != inf)).if({
			endtime = starttime + duration;
			})
		}

	setDuration {arg newdur;
		duration = newdur;
		releases.do({arg me;
			(me.duration > newdur).if({
				me.duration_(newdur)
				})
			});
		starttime.notNil.if({
			endtime = starttime + duration;
			})
		}

	getValueAtTime {arg argname, time = 0.0;
		var times, argkey, values, pos, myRef;
		refsDict[argname].isNil.if({
			^args.at(argname)
			}, {
			myRef = refsDict[argname].value ++ [[0.0, argname, args.at(argname)]];
			#times, argkey, values = myRef.value.sort({arg a, b;
				a[0] < b[0]
				}).flop;
			pos = times.indexOfGreaterThan(time);
			pos = pos.isNil.if({
				times.size - 1;
				}, {
				pos - 1;
				});
			^values[pos];
			})
		}

	args_ {arg argdict;
		args = argdict;
		argdict.keysValuesDo({arg argname, val;
			this.addUniqueMethod(argname.asSymbol, {arg note, time;
				time.isNil.if({
					args.at(argname)
					}, {
					this.getValueAtTime(argname, time)
					});
				});
			this.addUniqueMethod((argname.asString++"_").asSymbol, {
				arg note, newValue, timeOffset, curval;
				var oldval, thisarg;
				(this.isPlaying).if({
						oldval = args[argname];
						args.put(argname.asSymbol, newValue);
						this.handleRealTimeUpdate(argname, newValue, oldval, timeOffset);
						}, {
						timeOffset.isNil.if({
							args.put(argname, newValue);
							this.checkIfRelease(newValue);
							}, {
							curval = this.perform(argname, timeOffset);
							refsDict[argname].isNil.if({
								refsDict.put(argname,
									Ref([[timeOffset, argname, newValue]]))
								}, {
								refsDict[argname] = refsDict[argname].value.add(
									[timeOffset, argname, newValue])
								});
							(curval.isKindOf(CtkControl) and: {curval.isARelease}).if({
								curval.duration_(timeOffset - curval.starttime)
								})
							});
						});

				note;
				});
			});
		}

	performArgs {arg argsPairs;
		argsPairs = argsPairs.clump(2);
		argsPairs.do({arg thisPair;
			var key, val;
			#key, val = thisPair;
			this.perform((key ++ "_").asSymbol, val)
		});
	}

	checkIfRelease {arg aValue;
		// if it is a CtkControl     AND
		(aValue.isKindOf(CtkControl) and: {
			// it is is NOT playing or already a release
			(aValue.isARelease or: {aValue.isPlaying or: {aValue.isScored}}).not}).if({
				// then make it a release;
				releases = releases.add(aValue);
				aValue.isARelease = true;
				aValue.setStarttime(starttime);
				(aValue.duration.notNil and:
					{duration.notNil and: {aValue.duration > duration}}).if({
						aValue.duration_(duration);
					});
				(aValue.duration.isNil and: {duration.notNil}).if({
					aValue.duration_(duration)
					})
				})
		}

	checkNewValue {arg argname, newValue, oldval;
		case {
			(newValue.isArray || newValue.isKindOf(Env) || newValue.isKindOf(InterplEnv))
			}{
			newValue = newValue.asArray;
			this.setn(nil, argname, newValue.asUGenInput);
			}{
			newValue.isKindOf(CtkBus)
			}{
			newValue.isPlaying.not.if({
				this.checkIfRelease(newValue);
				newValue.play(node, argname);
				});
			noMaps.indexOf(argname).notNil.if({
				this.set(latency, argname, newValue);
				}, {
				newValue.isKindOf(CtkControl).if({
					this.map(latency, argname, newValue);
					}, {
					this.mapa(latency, argname, newValue);
					})
				})
			}{
			true
			}{
			this.set(nil, argname, newValue.asUGenInput);
			};
		// real-time support for CtkControls
		(oldval.isKindOf(CtkControl)).if({
			(releases.indexOf(oldval)).notNil.if({
				oldval.free;
				releases.remove(oldval);
				})
			});
	}

	handleRealTimeUpdate {arg argname, newValue, oldval, timeOffset;
		timeOffset.notNil.if({
			SystemClock.sched(timeOffset, {
				this.isPlaying.if({
					this.checkNewValue(argname, newValue, oldval)
					})
				})
			}, {
			this.checkNewValue(argname, newValue, oldval);
			});
		}

	// every one of these has a tag and body... leaves room for addAction and
	// target in CtkEvent

	newBundle {
		var bundlearray, initbundle;
		bundlearray =	this.buildBundle;
		initbundle = [starttime, bundlearray];
		setnDict.keysValuesDo({arg key, val;
			val = val.perform(\asUGenInput);
			initbundle = initbundle.add([\n_setn, node, key, val.size] ++ val);
			});
		mapDict.keysValuesDo({arg key, val;
			val.isKindOf(CtkControl).if({
				initbundle = initbundle.add([\n_map, node, key, val.asUGenInput])
				}, {
				initbundle = initbundle.add([\n_mapa, node, key, val.asUGenInput])
				})
			});
		^initbundle;
		}

	// no time stamp;
	msgBundle {
		var bundlearray, initbundle;
		bundlearray =	this.buildBundle;
		mapDict.keysValuesDo({arg key, val;
			bundlearray = bundlearray.add(key);
			bundlearray = bundlearray.add(val.asMapInput);
			});
		initbundle = [bundlearray];
		setnDict.keysValuesDo({arg key, val;
			val = val.perform(\asUGenInput);
			initbundle = initbundle.add([\n_setn, node, key, val.size] ++ val);
			});
		^initbundle;
		}

	buildBundle {
		var bundlearray, tmp;
//		(target.isKindOf(CtkNote) || target.isKindOf(CtkGroup)).if({
//			target = target.node});
		bundlearray =	[\s_new, synthdefname, this.node, addActions[addAction], target.asUGenInput];
		args.keysValuesDo({arg key, val;
			var refsize;
			// check if val is a Ref - if so, we just need the initial value
			// store and automate its data
			refsDict[key].notNil.if({
				refsDict[key].value.do({arg me;
					automations = automations.add(me)
					});
				});
			tmp = this.parseKeys(key, val);
			tmp.notNil.if({
				bundlearray = bundlearray ++ tmp;
				})
			});
		^bundlearray;
		}

	parseKeys {arg key, val;
		case {
			(val.isArray || val.isKindOf(Env) || val.isKindOf(InterplEnv))
			}{
			setnDict.add(key -> val.asArray); ^nil;
			}{
//			val.isKindOf(CtkControl)
			val.isKindOf(CtkBus)
			}{
			// if this key is a noMap (so, probably the bus arg of Out.kr),
			// send in the CtkControl's bus number
			noMaps.indexOf(key).notNil.if({
				^[key, val.asUGenInput];
				}, {
				// oherwise, map the arg to the argument
				mapDict.add(key -> val);
				^nil
				});
			}{
			true
			}{
			^[key, val.asUGenInput];
			}
	}


	bundle {
		^this.newBundle;
		}

	getFreeMsg {
		(duration.notNil && willFree.not).if({
			^CtkMsg(server, (starttime + duration).asFloat, [\n_free, this.node]);
			}, {
			^nil
			});
		}

	// support playing and releasing notes ... not for use with scores
	play {arg group;
		var bund, start;
		this.isPlaying.not.if({
			SystemClock.sched(starttime ?? {0.0}, {
				bund = OSCBundle.new;
				bund.add(this.buildBundle);
				setnDict.keysValuesDo({arg key, val;
					val = val.perform(\asUGenInput);
					bund.add([\n_setn, node, key, val.size] ++ val);
					});
				mapDict.keysValuesDo({arg key, val;
					(val.isPlaying.not).if({
						this.checkIfRelease(val);
						val.play;
						});
					val.isKindOf(CtkControl).if({
						bund.add([\n_map, node, key, val.asUGenInput]);
						}, {
						bund.add([\n_mapa, node, key, val.asUGenInput]);
						})
					});
				bund.send(server, latency);
				// for CtkControl mapping... make sure things are running!
				this.watch(group ?? {target}); // don't think this is correct - how to figure out which group things are runningin?
				// if a duration is given... kill it
				duration.notNil.if({
					SystemClock.sched(duration, {this.free(0.1, false)})
					});
				(automations.size > 0).if({
					this.playAutomations;
					})
				});
			^this;
			}, {
			"This instance of CtkNote is already playing".warn;
			})
		}

	playAutomations {
		var events, curtime = 0.0, firstev, idx = 0;
		// first, save the automations to a local var, and clear them out.
		events = automations;
		automations = [];
		events.sort({arg a, b; a[0] < b[0]});
		firstev = events[0][0];
		SystemClock.sched(firstev, {
			(this.isPlaying).if({
				this.perform((events[idx][1]++"_").asSymbol, events[idx][2]);
				curtime = events[idx][0];
				idx = idx + 1;
				(idx < events.size).if({
					events[idx][0] - curtime
					}, {
					nil
					});
				})
			})
		}

	prBundle {
		^this.bundle;
		}
	}

/* methods common to CtkGroup and CtkNote need to be put into their own class (CtkNode???) */
CtkGroup : CtkNode {
	var <>endtime = nil, <duration, <>isGroupPlaying = false, <>children, <>noteDict;

	*new {arg starttime = 0.0, duration, node, addAction = 0, target = 1, server;
		^super.newCopyArgs(Dictionary.new, nil, addAction, target, server, node)
			.init(starttime, duration)
			.initCtkNode(server)
		}

	*play {arg starttime = 0.0, duration, node, addAction = 0, target = 1, server;
		^this.new(starttime, duration, node, addAction, target, server).play;
		}

	init {arg argstarttime, argduration;
		target.isKindOf(CtkNode).if({
			(target.server != server).if({
				"Mismatch between target Server and this CtkNode's server, setting to target's server".warn;
				server = target.server;
			})
		}, {
			server = server ?? {Server.default};
		});
		starttime = argstarttime;
		duration = argduration;
		duration.notNil.if({
			endtime = starttime + duration
			});
//		server = server ?? {Server.default};
		messages = Array.new;
		children = Array.new;
		noteDict = Dictionary.new;
		}

	newBundle {
		var start, bundlearray;
		bundlearray =	this.buildBundle;
		start = starttime ?? {0.0}
		^[starttime, bundlearray];
		}

	buildBundle {
		var bundlearray;
		bundlearray =	[\g_new, this.node, addActions[addAction], target.asUGenInput];
		^bundlearray;
		}

	msgBundle {
		^[this.buildBundle];
		}

	prBundle {
		^this.bundle;
		}

	setStarttime {arg newstart;
		starttime = newstart;
		releases.do({arg me; me.setStarttime(newstart)});
		(duration.notNil && (duration != inf)).if({
			endtime = starttime + duration;
			})
		}

	setDuration {arg newdur;
		duration = newdur;
		releases.do({arg me;
			(me.duration > newdur).if({
				me.duration_(newdur)
				})
			});
		starttime.notNil.if({
			endtime = starttime + duration;
			})
		}

	bundle {
		var thesemsgs;
		thesemsgs = [];
		thesemsgs = thesemsgs.add(this.newBundle);
		(duration.notNil && willFree.not).if({
			thesemsgs = thesemsgs.add([(starttime + duration).asFloat, [\n_free, node]]);
			});
		^thesemsgs;
		}

	// create the group for RT uses
	play {arg neg = 0.01; // neg helps insure that CtkGroups will be created first
		var bundle = this.buildBundle;
		starttime.notNil.if({
			SystemClock.sched(starttime, {server.sendBundle(latency - neg, bundle)});
			}, {
			server.sendBundle(latency - neg, bundle);
			});
		duration.notNil.if({
			SystemClock.sched(duration, {this.freeAll})
			});
		this.watch;
		isGroupPlaying = true;
		^this;
		}

	freeAll {arg time = 0.0;
		var bund1, bund2;
		bund1 = [\g_freeAll, this.node];
		bund2 = [\n_free, this.node];
		isGroupPlaying.if({
			SystemClock.sched(time, {server.sendBundle(latency, bund1, bund2)});
			isGroupPlaying = false;
			}, {
			messages = messages.add(CtkMsg(server, time, bund1, bund2));
			})
		}

	deepFree {arg time = 0.0;
		this.freeAll(time);
		}

	}

CtkPGroup : CtkGroup {
	buildBundle {
		var bundlearray = [\p_new, this.node, addActions[addAction], target.asUGenInput];
		^bundlearray;
	}
}


// if a CtkBuffer is loaded to a server, its 'isPlaying' instance var will be set to true, and
// the CtkBuffer will be considered live.

CtkBuffer : CtkObj {
	var <bufnum, <path, <size, <startFrame, <numFrames, <numChannels, <server, channels, bundle,
		<freeBundle, <closeBundle, <messages, <isPlaying = false, <isOpen = false;
	var duration, <sampleRate, <starttime = 0.0, completion, <>endTime;
	var <collection, collPath, send, <label;

	*new {arg path, size, startFrame = 0, numFrames, numChannels, bufnum, server, channels;
		^this.newCopyArgs(Dictionary.new, nil, bufnum, path, size, startFrame, numFrames,
			numChannels, server, channels).init;
		}

	*diskin {arg path, size = 32768, startFrame = 0, server, channels;
		^this.new(path, size, startFrame, server: server, channels: channels)
		}

	*playbuf {arg path, startFrame = 0, numFrames, server, channels;
		^this.new(path, startFrame: startFrame, numFrames: numFrames, server: server,
			channels: channels)
		}

	*buffer {arg size, numChannels, server;
		^this.new(size: size, numChannels: numChannels, server: server)
		}

	*env {arg size, env, wavetable = 0, server;
		^this.new(size: size, numChannels: 1, server: server).fillWithEnv(0.0, env, wavetable);
		}

	*collection {arg collection, numChannels = 1, server;
		var data, obj;
		collection.isKindOf(RawArray).not.if({
			data = collection.collectAs({arg val; val}, FloatArray);
		}, {
			data = collection;
		});
		^this.new(size: data.size,
			numChannels: numChannels, server: server).collection_(data);
	}

	init {
		var sf, nFrames, test;
		server = server ?? {Server.default};
		bufnum = bufnum ?? {server.bufferAllocator.alloc(1)};
		channels.notNil.if({channels = channels.asArray});
		messages = [];
		test = true;
//		complettion = [];
		path.notNil.if({
			sf = SoundFile.new(path);
			test = sf.openRead;
			test.if({
				numChannels = sf.numChannels;
				channels.notNil.if({
					(channels.size < numChannels).if({
						numChannels = channels.size
						})
					});
				duration = sf.duration;
				sampleRate = sf.sampleRate;
				numFrames.isNil.if({
					numFrames = sf.numFrames;
					});
				sf.close;
				}, {
				("No soundfile found at: "++path).warn;
				})
			});
		test.if({
			case { // path, not size - load file with b_allocRead
				path.notNil && size.isNil
				} {
				// check if channels array is specified
				nFrames = numFrames ?? {0};
				channels.notNil.if({
					bundle = [\b_allocReadChannel, bufnum, path, startFrame, nFrames] ++ channels;
					}, {
					bundle = [\b_allocRead, bufnum, path, startFrame, nFrames];
					});
				} {// path, size ( for DiskIn )
				path.notNil && size.notNil
				} {
				nFrames = size; //numFrames ?? {size};
				channels.notNil.if({
					bundle = [\b_alloc, bufnum, size, numChannels,
						[\b_readChannel, bufnum, path, startFrame, nFrames, 0, 1] ++ channels];
					}, {
					bundle = [\b_alloc, bufnum, size, numChannels,
						[\b_read, bufnum, path, startFrame, nFrames, 0, 1]];
					});
				closeBundle = [\b_close, bufnum];
				} { /// just allocate memory (for delays, FFTs etc.)
				path.isNil && size.notNil
				} {
				numChannels = numChannels ?? {1};
				numFrames = size / numChannels;
				bundle = [\b_alloc, bufnum, size, numChannels];
				};
			freeBundle = [\b_free, bufnum];
			}, {
			"CtkBuffer set-up failed".warn;
			})
		}

	collection_ {arg aCollection;
		// should probably do some checking here... for now, this is fine
		collection = aCollection;
		isPlaying.if({
			(collection.size < 1026).if({
				this.set(0.0, 0, collection);
			}, {
				this.loadCollection(0.0, 0);
			})
		})
	}

	load {arg time = 0.0, sync = true, onComplete;
		SystemClock.sched(time, {
			Routine.run({
				var msg;
				isPlaying = true;
				completion.notNil.if({
					bundle = bundle.add(completion)
					});
//				cond = cond ?? {Condition.new};
				server.sendBundle(latency, bundle);
				sync.if({server.sync(cond);});
				// are there already messages to send? If yes... SYNC!, then send NOW
				((messages.size > 0) or: {collection.notNil}).if({
					server.sync(cond);
					messages.do({arg me;
						msg = me.messages;
						msg.do({arg thismsg;
							server.sendBundle(latency, thismsg);
							});
						server.sync(cond);
						});
					server.sync(cond);
					collection.notNil.if({
						(collection.size < 1025).if({
							this.set(0.0, 0, collection);
						}, {
							this.loadCollection(0.0, 0);
						})
					})
				}, {
					isPlaying = true;
				});
				sync.if({
					server.sync(cond);
					("CtkBuffer with bufnum id "++bufnum++" loaded").postln;
					onComplete.value;
					});
				})
			});
		}

	bundle {
		completion.notNil.if({
			bundle = bundle.add(completion);
			});
		^bundle;
		}

	free {arg time = 0.0, addMsg = true;
		closeBundle.notNil.if({
			SystemClock.sched(time, {
				server.sendBundle(latency, closeBundle, freeBundle);
				server.bufferAllocator.free(bufnum);
				});
			}, {
			SystemClock.sched(time, {
				server.sendBundle(latency, freeBundle);
				server.bufferAllocator.free(bufnum);
				});
			});
		addMsg.if({
			[closeBundle, freeBundle].do({arg bund;
				messages = messages.add(CtkMsg(server, time+starttime, bund));
			})
		});
		isPlaying = false;
		}

	set {arg time = 0.0, startPos, values;
		var bund;
		values = values.asArray;
		// check for some common problems
		((values.size + startPos) > size).if({
			"Number of values and startPos exceeds CtkBuffer size. No values were set".warn;
			^this;
			}, {
			bund = [\b_setn, bufnum, startPos, values.size] ++ values;
			([0.0, bund].bundleSize >= 8192).if({
				"Bundle size exceeds UDP limit. Use .loadCollection. No values were set".warn;
				^this;
				}, {
				this.bufferFunc(time, bund);
				^this;
				})
			})
		}

	zero {arg time = 0;
		var bund;
		bund = [\b_zero, bufnum];
		this.bufferFunc(time, bund);
		}

	fill {arg time = 0.0, newValue, start = 0, numSamples = 1;
		var bund;
		bund = [\b_fill, bufnum, start, numSamples, newValue];
		this.bufferFunc(time, bund);
		}

	read {arg time = 0.0, path, fileFrameStart = 0, numFrames, bufStartFrame = 0,
			leaveOpen = false, completionMessage, action;
		var bund;
		bund = [\b_read, bufnum, path, fileFrameStart, (numFrames ? -1).asInt,
			bufStartFrame, leaveOpen.binaryValue, completionMessage.value(this)];
		this.bufferFunc(time, bund, action);
	}

	loadCollection { arg time = 0.0, startFrame = 0, action;
		var msg, cond, path, file, array, sndFile, data;
		(server.isLocal and: {collection.notNil}).if({
			{
				collection.isKindOf(RawArray).not.if({
					data = collection.collectAs({|item| item}, FloatArray)
				}, {
					data = collection;
				});
				( collection.size > ((size - startFrame) * numChannels)).if({
					"Collection larger than available number of Frames".warn
				});
				sndFile = SoundFile.new;
				sndFile.sampleRate = server.sampleRate;
				sndFile.numChannels = numChannels;
				path = PathName.tmp ++ sndFile.hash.asString;
				sndFile.openWrite(path).if({
				 	sndFile.writeData(data);
				 	sndFile.close;
				 	this.read(time, path, bufStartFrame: startFrame,
				 		action: {arg ctkBuffer;
					 		File.delete(path).if({
						 		path = nil;
					 		}, {
						 		("Could not delete data file:" + path).warn;
					 		});
							action.value(array, this);
				 		})
					});
			}.forkIfNeeded;
		}, {
			"cannot do loadCollection with a non-local Server".warn;
		});
	}

	// write a buffer out to a file. For DiskOut usage in real-time, use openWrite and closeWrite
	write {arg time = 0.0, path, headerFormat = 'aiff', sampleFormat='int16',
			numberOfFrames = -1, startingFrame = 0;
		var bund;
		bund = [\b_write, bufnum, path, headerFormat, sampleFormat, numberOfFrames,
			startingFrame, 0];
		this.bufferFunc(time, bund);
		}

	// prepare a buffer for use with DiskOut
	openWrite {arg time = 0.0, path, headerFormat = 'aiff', sampleFormat='int16',
			numberOfFrames = -1, startingFrame = 0;
		var bund;
		isOpen = true;
		bund = [\b_write, bufnum, path, headerFormat, sampleFormat, numberOfFrames,
			startingFrame, 1];
		this.bufferFunc(time, bund);
		}

	closeWrite {arg time = 0.0;
		var bund;
		isOpen = false;
		bund = [\b_close, bufnum];
		this.bufferFunc(time, bund);
		}

	gen {arg time = 0.0, cmd, normalize = 0, wavetable = 0, clear = 1 ... args;
		var bund, flag;
		flag = (normalize * 1) + (wavetable * 2) + (clear * 4);
		bund = ([\b_gen, bufnum, cmd, flag] ++ args).flat;
		this.bufferFunc(time, bund);
		}

	sine1 {arg time, normalize = 0, wavetable = 0, clear = 1 ... args;
		this.gen(time, \sine1, normalize, wavetable, clear, args);
		}

	sine2 {arg time, normalize = 0, wavetable = 0, clear = 1 ... args;
		this.gen(time, \sine2, normalize, wavetable, clear, args);
		}

	sine3 {arg time, normalize = 0, wavetable = 0, clear = 1 ... args;
		this.gen(time, \sine3, normalize, wavetable, clear, args);
		}

	cheby {arg time, normalize = 0, wavetable = 0, clear = 1 ... args;
		this.gen(time, \cheby, normalize, wavetable, clear, args);
		}

	fillWithEnv {arg time = 0.0, env, wavetable = 0.0;
		env = (wavetable > 0.0).if({
			env.asSignal(size * 0.5).asWavetable;
			}, {
			env.asSignal(size)
			});
		this.set(time, 0, env);
		}

	// some methods from Stelios Manousakis for PartConv:

	// slightly hackish, yes, but tricks a Buffer into writing its contents into a Ctk buffer
	copyFromBuffer { arg buf, dstStartAt = 0, srcStartAt = 0, numSamples = -1;
		server.listSendMsg(
			buf.copyMsg(this, dstStartAt, srcStartAt, numSamples)
		)
	}
	copyMsg { arg buf, dstStartAt = 0, srcStartAt = 0, numSamples = -1;
		^[\b_gen, buf.bufnum, "copy", dstStartAt, bufnum, srcStartAt, numSamples]
	}

	preparePartConv { arg time = 0.001, buf, fftsize;
		var bund;
		//server.listSendMsg(["/b_gen", bufnum, "PreparePartConv", buf.bufnum, fftsize]);
		bund = [\b_gen, bufnum, "PreparePartConv", buf.bufnum, fftsize];
		this.bufferFunc(time, bund)
	}

	// checks if this is a live, active buffer for real-time use, or being used to build a CtkScore
	bufferFunc {arg time, bund, action;
		var cond;
		isPlaying.if({
			SystemClock.sched(time, {
				Routine.run({
					cond = Condition.new;
					server.sendBundle(latency, bund);
					server.sync(cond);
					latency.wait;
					action.value(this);
				})
			})
		}, {
		(time == 0.0).if({
			completion = bund;
			}, {
			messages = messages.add(CtkMsg(server, time ?? {0.0}, bund))
			})
		});
	}

	duration {
		duration.isNil.if({
			^size / (server.sampleRate ?? {"The Server doesn't appear to be booted, therefore a duration can not be calculated. The SIZE of the buffer in frames will be returned instead".warn; 1});
			}, {
			^duration
			})
		}

	server_ {arg aServer;
		isPlaying.not.if({
			server = aServer;
			}, {
			"A CtkBuffer's server can not be changed while it is being used in real-time mode".warn;})
		}
	asUGenInput {^bufnum}

	label_ {arg aLabel;
		label = aLabel.asSymbol;
	}

	loadToFloatArray { arg index = 0, count = -1, action;
		var msg, cond, path, file, array, condition;
		{
			Routine.run({
				condition = Condition.new;
				path = PathName.tmp ++ this.hash.asString;

				msg = this.write(0.0, path, "aiff", "float", count, index);
				latency.wait;
				server.sync(condition);
				file = SoundFile.new;
				protect {
					file.openRead(path);
					array = FloatArray.newClear(file.numFrames * file.numChannels);
					file.readData(array);
				} {
					file.close;
					if(File.delete(path).not) { ("Could not delete data file:" + path).warn };
				};
				action.value(array, this);
			})
		}.forkIfNeeded;
	}

	plot { arg name = "Plot", bounds = Rect(10, 10, 500, 400), minval = -1.0, maxval = 1.0, parent, labels=true;
		var gui;
		gui = GUI.current;
		this.loadToFloatArray(action: { |array, buf|
			{
				GUI.use( gui, {
					array.plot(name, bounds, numChannels: buf.numChannels, minval: minval, maxval: maxval, parent: parent, labels: labels);
				});
			}.defer;
		});
	}

}

CtkBus : CtkObj {
	var <server, <bus, <numChans;
}

CtkControl : CtkBus {
	var <initValue, <starttime, <messages, <isPlaying = false,
	<endtime = 0.0, <duration; //may want to get rid of setter later
	var <env, <ugen, <freq, <phase, <high, <low, <ctkNote, free, <>isScored = false, buffer,
	<isLFO = false, <isEnv = false, <isKBuf = false;
	var <timeScale, <levelBias, <levelScale, <doneAction, <>isARelease = false, <label;

	classvar <ctkEnv, <sddict;

	*new {arg numChans = 1, initVal = 0.0, starttime = 0.0, bus, server;
		^this.newCopyArgs(Dictionary.new, nil, server, bus, numChans, initVal.asArray, starttime).initThisClass;
		}

	/* calling .play on an object tells the object it is being used in real-time
	and therefore will send messages to server */
	*play {arg numChans = 1, initVal = 0.0, bus, server;
		^this.new(numChans, initVal, 0.0, bus, server).play;
		}

	*reserve {arg numChans, initVal = 0.0, starttime = 0.0, bus, server;
		^this.new(numChans, initVal, starttime, bus, server).reserve;
	}

	reserve {
		server.controlBusAllocator.reserve(bus, numChans, true);
	}

	initThisClass {
		var bund, numSlots;
		server = server ?? {Server.default};
		bus = bus ?? {server.controlBusAllocator.alloc(numChans)};
		messages = []; // an array to store sceduled bundles for this object
		numSlots = (numChans > initValue.size).if({
			initValue = initValue ++ Array.fill(numChans - initValue.size, {0.0});
			numChans;
			}, {
			numChans
			});
		bund = [\c_setn, bus, initValue.size] ++ initValue;
		messages = messages.add(CtkMsg(server, starttime.asFloat, bund));
		ctkNote = nil;
		}

	setStarttime {arg newStarttime;
		starttime = newStarttime;
		starttime.notNil.if({
			ctkNote.notNil.if({
				ctkNote.setStarttime(newStarttime);
				});
			[freq, phase, high, low].do({arg me;
				me.isKindOf(CtkControl).if({
					me.setStarttime(newStarttime);
				});
			});
		})
		}

	duration_ {arg newDuration;
		duration = newDuration;
		duration.notNil.if({
			ctkNote.notNil.if({
				ctkNote.setDuration(duration)
				});
			[freq, phase, high, low].do({arg me;
				(me.isKindOf(CtkControl) and: {me.isEnv.not}).if({
					me.duration_(duration)
					})
				})
			})
		}
	*env {arg env, starttime = 0.0, addAction = 0, target = 1, bus, server,
			levelScale = 1, levelBias = 0, timeScale = 1, doneAction = 0;
		^this.new(1, env[0], starttime, bus, server).initEnv(env, levelScale, levelBias, timeScale,
			addAction, target, doneAction);
		}

	initEnv {arg argenv, argLevelScale, argLevelBias, argTimeScale, argAddAction, argTarget,
			argDoneAction;
		env = argenv;
		env.isKindOf(InterplEnv).if({
			env = env.asEnv
			});
		timeScale = argTimeScale;
		levelScale = argLevelScale;
		levelBias = argLevelBias;
		doneAction = argDoneAction;
		isEnv = true;
		duration = env.releaseNode.notNil.if({
			free = false;
			nil
			}, {
			free = true;
			env.times.sum * timeScale;
			});
		// the ctk note object for generating the env
		ctkNote = sddict.dict[\ctkenv].note(starttime, duration, argAddAction, argTarget,
			server).myenv_(env).outbus_(bus).levelScale_(levelScale).levelBias_(levelBias)
			.timeScale_(timeScale).doneAction_(doneAction);
		}

	levelScale_ {arg newLS = 1;
		(isEnv or: {isKBuf}).if({
			ctkNote.levelScale_(newLS);
			levelScale = newLS;
			})
		}

	levelBias_ {arg newLB = 0;
		(isEnv or: {isKBuf}).if({
			ctkNote.levelBias_(newLB);
			levelBias = newLB;
			})
		}

	*kbuf {arg buffer, starttime = 0.0, addAction = 0, target = 1, bus, server,
			levelScale = 1, levelBias = 0, timeScale = 1;
		^this.new(1, 0, starttime, bus, server).initKbuf(buffer, levelScale, levelBias, timeScale,
			addAction, target);
		}

	initKbuf {arg argbuffer, argLevelScale, argLevelBias, argTimeScale, argAddAction, argTarget;
		buffer = argbuffer;
		timeScale = argTimeScale;
		levelScale = argLevelScale;
		levelBias = argLevelBias;
		isKBuf = true;
		duration = timeScale;
		// the ctk note object for generating the env
		ctkNote = sddict.dict[\ctkkbuffer].note(starttime, timeScale, argAddAction, argTarget,
			server).buffer_(buffer).outbus_(bus).levelScale_(levelScale).levelBias_(levelBias)
			.timeScale_(timeScale);
		}

	*lfo {arg ugen, freq = 1, low = -1, high = 1, phase = 0, starttime = 0.0, duration,
			addAction = 0, target = 1, bus, server;
		^this.new(1, 0.0, starttime, bus, server).initLfo(ugen, freq, phase, low, high, addAction,
			target, duration);
		}

	initLfo {arg argugen, argfreq, argphase, arglow, arghigh, argAddAction, argTarget, argDuration;
		var thisctkno;
		ugen = argugen;
		freq = argfreq;
		phase = argphase;
		low = arglow;
		high = arghigh;
		duration = argDuration;
		free = false;
		messages = [];
		isLFO = true;
		thisctkno = sddict.dict[("CTK"++ugen.class).asSymbol];
		case
			{
			[LFNoise0, LFNoise1, LFNoise2].indexOf(ugen).notNil;
			} {
			ctkNote = thisctkno.note(starttime, duration, argAddAction,
				argTarget, server).freq_(freq).low_(low).high_(high).bus_(bus);
			} {
			[SinOsc, Impulse, LFSaw, LFPar, LFTri, LFCub].indexOf(ugen).notNil;
			} {
			ctkNote = thisctkno.note(starttime, duration, argAddAction,
				argTarget, server).freq_(freq).low_(low).high_(high)
				.phase_(phase).bus_(bus);
			}
		}

	freq_ {arg newfreq;
		isLFO.if({
			ctkNote.freq_(newfreq);
			})
		}

	low_ {arg newlow;
		isLFO.if({
			ctkNote.low_(newlow);
			})
		}

	high_ {arg newhigh;
		isLFO.if({
			ctkNote.high_(newhigh);
			})
		}

	// free the id for further use
	free {
		isPlaying = false;
		ctkNote.notNil.if({
			ctkNote.free;
			});
		server.controlBusAllocator.free(bus);
		}

	release {
		ctkNote.notNil.if({
			ctkNote.release;
			})
		}

	play {arg node, argname;
		var time, bund, bundle;
		isPlaying = true;
		ctkNote.notNil.if({
			ctkNote.play;
			});
		messages.do({arg me;
			me.msgBundle.do({arg thisMsg;
				bund = bund.add(thisMsg);
				});
			bundle = OSCBundle.new;
			bund.do({arg me;
				bundle.add(me)
				});
			time = me.starttime;
			(time > 0).if({
				SystemClock.sched(time, {
					bundle.send(server, latency);
					})
				}, {
					bundle.send(server, latency);
				});
			});
		}

	server_ {arg aServer;
		this.isPlaying.not.if({
			server = aServer;
			})
		}

	set {arg val, time = 0.0;
		var bund;
		val = val.asArray;
		bund = [\c_setn, bus, val.size] ++ val;
		isPlaying.if({
			SystemClock.sched(time, {server.sendBundle(latency, bund)});
			}, {
			time = time ?? {0.0};
			messages = messages.add(CtkMsg(server, starttime + time, bund));
			});
		initValue = val;
		^this;
		}

	get {arg action;
		var o;
		OSCresponderNode(nil, '/c_set', {arg time, resp, msg;
			(msg[1] == bus).if({
				action.value(msg[1], msg[2]);
				resp.remove;
			})
		}).add;
		server.sendMsg(\c_get, bus);
	}

	+ { arg index;
		^this.at(index)
	}

	at {arg index;
		if (index >= numChans) { Error("CtkControl-at: index out of range").throw };
		^CtkControl.new(1, initValue, starttime, bus + index, server);
	}

	asUGenInput {^bus}
	asMapInput {^(\c++bus).asSymbol}

	*initClass {
		var thisctkno;
		StartUp.add({
			"Ctk init class runs".postln;
			sddict = CtkProtoNotes.new;
			sddict.add(
				SynthDef(\ctkenv, {arg gate = 1, outbus, levelScale = 1, levelBias = 0,
					timeScale = 1, doneAction = 0;
					Out.kr(outbus,
						EnvGen.kr(
							Control.names([\myenv]).kr(Env.newClear(64)),
							gate, timeScale: timeScale, doneAction: doneAction) *
						levelScale + levelBias)
				})
			);
			// control rate buffer playback - single shot over duration
			sddict.add(
				SynthDef(\ctkkbuffer, {arg outbus, buffer, levelScale = 1, levelBias = 0, timeScale = 1;
					var point;
					point = Line.kr(0, 1, timeScale) * BufFrames.kr(buffer);
					Out.kr(outbus, BufRd.kr(1, buffer, point, 0) * levelScale + levelBias);
				});
			);
			[LFNoise0, LFNoise1, LFNoise2].do({arg ugen;
				thisctkno = SynthDef(("CTK" ++ ugen.class).asSymbol, {arg freq, low, high, bus;
					Out.kr(bus, ugen.kr(freq).range(low, high));
				});
				sddict.add(thisctkno);
			});
			[SinOsc, Impulse, LFSaw, LFPar, LFTri, LFCub].do({arg ugen;
				thisctkno =
				SynthDef(("CTK" ++ ugen.class).asSymbol, {arg freq, low, high, phase, bus;
					Out.kr(bus, ugen.kr(freq, phase).range(low, high));
				});
				sddict.add(thisctkno);
			});
		})
		}

	label_ {arg aLabel;
		label = aLabel.asSymbol;
	}
}

// not really needed... but it does most of the things that CtkControl does
CtkAudio : CtkBus {
	var <isPlaying = false, <label;
	*new {arg numChans = 1, bus, server;
		^this.newCopyArgs(Dictionary.new, nil, server, bus, numChans).init;
		}

	*play {arg numChans = 1, bus, server;
		^this.new(numChans, bus, server).play;
		}

	*reserve {arg numChans, bus, server;
		^this.new(numChans, bus, server).reserve;
	}

	reserve {
		server.audioBusAllocator.reserve(bus, numChans, true);
	}

	play {
		isPlaying = true;
		}

	// free the id for further use
	free {
		server.audioBusAllocator.free(bus);
		}

	init {
		server = server ?? {Server.default};
		bus = bus ?? {server.audioBusAllocator.alloc(numChans)};
		}

	+ { arg index;
		^this.at(index)
	}

	at { arg index;
		if (index >= numChans) { Error("CtkAudio-at: index out of range").throw };
		^CtkAudio.new(1, bus + index, server);
	}

	asUGenInput {^bus}
	asMapInput {^(\a++bus).asSymbol}
	asCtkAudio {^this}

	label_ {arg aLabel;
		label = aLabel.asSymbol;
	}
}

/* this will be similar to ProcMod ... global envelope magic

CtkEvent can return and play a CtkScore - individual group, envbus?

with .play - needs to act like ProcMod
with .write, needs to act like CtkScore
with .addToScore - needs to act like .write, and return the CtkScore that is created, and append
	them

need to create a clock like object that will wait in tasks, advance time in .write situations
*/

/* CtkTimer needs to be a TempoClock when played, a timekeeper when used for NRT */

CtkTimer {
	var starttime, <curtime, <clock, <tempo, rtempo, isPlaying = false, <next = nil, start;

	*new {arg starttime = 0.0;
		^super.newCopyArgs(starttime, starttime).initCtkTimer;
		}

	initCtkTimer {
		this.tempo_(1);
		}

	play {
		isPlaying.not.if({
			clock = TempoClock.new;
			clock.tempo_(tempo);
			rtempo = tempo.reciprocal;
			isPlaying = true;
			}, {
			"This CtkClock is already playing".warn
			});
		}

	tempo_ {arg newTempo;
		tempo = newTempo;
		rtempo = newTempo.reciprocal
		}

	beats {
		^this.curtime;
		}

	free {
		isPlaying.if({
			clock.stop;
			isPlaying = false;
			})
		}

	now {
		isPlaying.if({
			^clock.elapsedBeats;
			}, {
			^curtime - starttime;
			})
		}

	wait {arg inval;
		isPlaying.if({
			(inval*rtempo).yield;
			}, {
			curtime = curtime + (inval*rtempo)
			});
		}

	next_ {arg inval;
		next = inval;
		(isPlaying.not and: {inval.notNil}).if({
			curtime = curtime + (inval*rtempo);
			})
		}
	}

CtkEvent : CtkObj {
	classvar <envsd;
	var <starttime, <>condition, <function, amp, <server, addAction, target, isRecording = false;
	var isPlaying = false, isReleasing = false, releaseTime = 0.0, <timer, clock,
		<envbus, inc, <group, <>for = 0, <>by = 1, envsynth, envbus, playinit, notes,
		score, <endtime, endtimeud, noFunc = false, eventDur;

	*new {arg starttime = 0.0, condition, amp = 1, function, addAction = 0, target = 1, server;
		^super.newCopyArgs(Dictionary.new, nil, starttime, condition, function, amp, server,
			addActions[addAction]).initCE(target);
		}

	initCE {arg argTarget;
		argTarget = argTarget.asUGenInput;
		target = argTarget ?? {1};
		server = server ?? {Server.default};
		timer = CtkTimer.new(starttime);
		(condition.isKindOf(Env) and: {condition.releaseNode.isNil}).if({
			endtime = condition.times.sum + starttime;
			endtimeud = false
			}, {
			endtime = starttime;
			endtimeud = true;
			});
		inc = 0;
		playinit = true;
		notes = [];
		function = function ?? {noFunc = true; {}};
		}

	function_ {arg newfunction;
		noFunc = false;
		function = newfunction;
		}

	record {
		score = CtkScore.new;
		isRecording = true;
		this.play;
		^score;
		}

	play {
		var loopif, initVal, initSched;
		server.serverRunning.if({
			isPlaying.not.if({
				isPlaying = true;
				timer.play;
				this.setup;
				clock.sched(starttime, {
					var now;
					now = timer.now;
					playinit.if({
						playinit = false;
						condition.isKindOf(Env).if({
							condition.releaseNode.isNil.if({
								clock.sched(condition.times.sum + 0.1, {this.clear});
								})
							});
						[group, envbus, envsynth].do({arg me;
							me.notNil.if({
								isRecording.if({
									score.add(me)
									});
								me.play
								})
							});
						});
					timer.next_(nil);
					function.value(this, group, envbus, inc, server);
					this.run;
					this.checkCond.if({
						timer.next;
						}, {
						initSched = (endtime > timer.now).if({endtime - timer.now}, {0.1});
						timer.clock.sched(initSched, {
//							((group.children.size == 0) and: {noFunc}).if({
							((group.children.size == 0)).if({
								this.free;
								}, {
								0.1;
								})
							})
						});
					})
				})
			}, {
			"Please boot the Server before trying to play an instance of CtkEvent".warn;
			})
		}

	run {
		notes.asArray.do({arg me;
			((me.starttime == 0.0) or: {me.starttime.isNil}).if({
				isPlaying.if({
					isRecording.if({score.add(me.copy.setStarttime(timer.now))});
					me.play(group);
					})
				}, {
				clock.sched(me.starttime, {
					isPlaying.if({
						isRecording.if({
							score.add(me.copy(timer.now))
							});
						me.play(group);
						});
					})
				});
			});
		notes = [];
		inc = inc + by;
		}

	setup {
//		var thisdur;
		group.notNil.if({group.free});
		envbus.notNil.if({envbus.free});
		clock = timer.clock;
		group = CtkGroup.new(addAction: addAction, target: target, server: server);
		condition.isKindOf(Env).if({
			envbus = CtkControl.new(initVal: condition.levels[0], starttime: starttime,
				server: server);
			eventDur = condition.releaseNode.isNil.if({condition.times.sum}, {nil});
			envsynth = envsd.new(duration: eventDur, target: group, server: server)
				.outbus_(envbus.bus).evenv_(condition).amp_(amp);
			}, {
			amp.isKindOf(Env).if({
				envbus = CtkControl.env(amp, starttime, server: server)
				}, {
				envbus = CtkControl.new(1, amp, starttime, server: server);
				})
			});
//
//		(target.isKindOf(CtkNote) || target.isKindOf(CtkGroup)).if({
//			target = target.node});
		}

	free {
		this.clear;
		}

	release {
		isPlaying.if({
			noFunc.if({noFunc = false});
			condition.isKindOf(Env).if({
				condition.releaseNode.notNil.if({
					envsynth.release(key: \evgate);
					this.releaseSetup(condition.releaseTime);
					}, {
					"The envelope for this CtkEvent doesn't have a releaseNode. Use .free instead".warn;})
				}, {
				"This CtkEvent doesn't use an Env as a condition control. Use .free instead".warn
				})
			}, {
			"This CtkEvent is not playing".warn
			});
		}

	releaseSetup {arg reltime;
		clock.sched(reltime, {this.clear});
		}

	clear {
		clock.clear;
		clock.stop;
		group.free;
		envbus.free;
		isPlaying = false;
		isRecording = false;
		this.initCE;
		}

	scoreClear {
		clock.clear;
		clock.stop;
		isPlaying = false;
		isRecording = false;
		this.initCE;
		}

	next_ {arg inval;
		timer.next_(inval);
		}

	curtime {
		^timer.curtime;
		}

	now {
		^timer.now;
		}

	checkCond {
		case
			{
			(timer.next == nil)// and: {noFunc.not}
			} {
			^noFunc;//false
			} {
			condition.isKindOf(Boolean) || condition.isKindOf(Function)
			} {
			^condition.value(timer, inc)
			} {
			condition.isKindOf(SimpleNumber)
			} {
			^inc < condition
			} {
			condition.isKindOf(Env)
			} {
			^condition.releaseNode.isNil.if({
				timer.now < (condition.times.sum + starttime);
				}, {
				(isReleasing || (releaseTime < condition.releaseTime))
				})
			} {
			true
			} {
			^false
			}
		}

	collect {arg ... ctkevents;
		var thisend;
		ctkevents = ctkevents.flat;
		endtimeud.if({
			ctkevents.do({arg ev;
				ev.endtime.notNil.if({
					thisend = ev.endtime + timer.now;
					(thisend > endtime).if({
						endtime = thisend
						})
					})
				})
			});
		notes = (notes ++ ctkevents).flat;
//		isPlaying.if({this.run});
 		}

	//  may not need this... or, if may be able to be simplified (just store objects to
	// the CtkScore ... or WOW! I THINK IT WILL JUST WORK!)

	score {arg sustime = 0;
		var curtime, idx, eventEnd, localEndtime;
		localEndtime = 0.0;
		// check first to make sure the condition, if it is an Env, has a definite duration
		condition.isKindOf(Env).if({
			condition.releaseNode.notNil.if({
				// use sustime to convert it to a finite Env
				idx = condition.releaseNode;
				condition.times = condition.times.insert(idx, sustime);
				condition.levels = condition.levels.insert(idx, condition.levels[idx]);
				condition.curves.isArray.if({
					condition.curves = condition.curves.insert(idx, \lin)
					});
				condition.releaseNode_(nil);
				});
			});
		score = CtkScore.new;
		this.setup;
		group.node;
		[envbus, envsynth].do({arg me;
			me.notNil.if({
				me.setStarttime(starttime);
				score.add(me)
			})
		});
		eventEnd = eventDur.notNil.if({starttime + eventDur});
		while({
			timer.next_(nil);
			curtime = timer.curtime;
			function.value(this, group, envbus, inc, server);
			notes.asArray.do({arg me;
				me.starttime.isNil.if({
					me.setStarttime(curtime)
					}, {
					me.setStarttime(me.starttime + curtime);
					});
				eventEnd.notNil.if({
					(me.endtime > eventEnd).if({
						me.setDuration(eventEnd - me.starttime)
						});
					(me.starttime > eventEnd).if({
						score.notes.remove(me)
						}, {
							(me.endtime < localEndtime).if({

								localEndtime = me.endtime
							});
						score.add(me)
						});
					}, {
						(me.endtime < localEndtime).if({

								localEndtime = me.endtime
						});
					score.add(me)
					})
				});
			notes = [];
			inc = inc + by;
			this.checkCond;
			});
		group.setStarttime(starttime);
		group.endtime_(score.endtime);
		score.add(group);
		this.scoreClear;
		^score;
		}

	*initClass {
//		addActions = IdentityDictionary[
//			\head -> 0,
//			\tail -> 1,
//			\before -> 2,
//			\after -> 3,
//			\replace -> 4,
//			0 -> 0,
//			1 -> 1,
//			2 -> 2,
//			3 -> 3,
//			4 -> 4
//			];
		StartUp.add({
		envsd = CtkNoteObject(
			SynthDef(\ctkeventenv_2561, {arg evgate = 1, outbus, amp = 1, timeScale = 1,
					lag = 0.01;
				var evenv;
				evenv = EnvGen.kr(
					Control.names(\evenv).kr(Env.newClear(30)), evgate,
						1, 0, timeScale, doneAction: 13) * Lag2.kr(amp, lag);
				Out.kr(outbus, evenv);
				})
			);
			})
		}

	}

// a simple object for catching any number of extra messages - mostly for storage and sorting
// messages are OSC messages. Will probably be only used internally

CtkMsg : CtkObj{
	var <starttime, <duration, <endtime, <messages, <target = 0, <>bufflag = false;
	var <>server; //, <singleShot;

	*new {arg server, starttime ... messages;
		^super.new.initMsgClass(server, starttime, messages);
		}

	initMsgClass {arg argServer, argStarttime, argMessages;
		server = argServer ?? {Server.default};
		messages = argMessages;
		starttime = argStarttime;
		}

	setStarttime {arg newStarttime;
		starttime = newStarttime;
		}

	addMessage {arg ... newMessages;
		messages = messages ++ newMessages;
		}

	bundle {
		var bundle;
		bundle = [starttime];
		messages.do({arg me; bundle = bundle.add(me)});
		^bundle;
		}

	msgBundle {
		var bundle;
		bundle = [];
		messages.do({arg me; bundle = bundle.add(me)});
		^bundle;
		}
	}

