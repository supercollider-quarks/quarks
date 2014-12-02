/* (c) 2010-2013 Stefan Nussbaumer */
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

OSCCommands {

	classvar <window;
	classvar collectFunc, running=false, cmdList;

	*initClass {
		cmdList = ();
		if(Main.versionAtLeast(3, 5), {
			collectFunc = { |msg, time, addr, recvPort|
				if(msg[0] != '/status.reply', {
					cmdList.put(msg[0], msg[1..].size);
				})
			}
		}, {
			collectFunc = { |time, addr, msg|
				if(msg[0] != '/status.reply', {
					cmdList.put(msg[0], msg[1..].size);
				})
			}
		})
	}

	*collect { |play=true|
		var displayList;
		if(play, {
			if(running == false, {
				if(Main.versionAtLeast(3, 5), {
					thisProcess.addOSCRecvFunc(collectFunc);
				}, {
					thisProcess.recvOSCfunc = collectFunc;
				});
				CmdPeriod.add({ this.collect(false) });
				running = true;
			})
		}, {
			if(Main.versionAtLeast(3, 5), {
				thisProcess.removeOSCRecvFunc(collectFunc);
			}, {
				thisProcess.recvOSCfunc = nil;
			});
			CmdPeriod.remove({ this.collect(false) });
			running = false;
		});
	}

	*saveCmdSet { |deviceName|
		var thisDeviceName, allDevices, cmdsPath;

		deviceName ?? {
			Error("Please provide the device- or application-name whose command-names you want to save.").throw;
		};

		this.collect(false);

		thisDeviceName = deviceName.asSymbol;
		cmdsPath = this.filenameSymbol.asString.dirname;
		if(File.exists(cmdsPath+/+"OSCCommands"), {
			allDevices = Object.readArchive(cmdsPath +/+ "OSCCommands");
		}, {
			allDevices = ();
		});

		allDevices.put(thisDeviceName, cmdList).writeArchive(cmdsPath+/+"OSCCommands");
		cmdList.clear;
	}

	*makeWindow {
		var flow, fields = (), deviceNameField, saveBut;
		var progress, progressStates, progressRoutine, collectRoutine, stopFunc;
		var makeField, nextFields;
		var staticTextFont = Font("Arial", 10);
		var staticTextColor = Color(0.2, 0.2, 0.2);
		var textFieldFont = Font("Andale Mono", 9);
		var textFieldFontColor = Color.black;
		var textFieldBg = Color.white;

		OSCCommands.collect;

		makeField = { |cmds|
			if(fields.keys.size < cmds.size, {
				nextFields = cmds.keys.difference(fields.keys);
				nextFields.do({ |nf|
					fields.put(nf, ());
					fields[nf].cmdName = StaticText(window, 390@20)
						.background_(Color(1.0, 1.0, 1.0, 0.5))
					;
					if(cmds[nf] == 1, {
						fields[nf].cmdName.string_(nf.asString+"("++cmds[nf]+"slot)");
					}, {
						fields[nf].cmdName.string_(nf.asString+"("++cmds[nf]+"slots)");
					});
					// flow.shift(0, 0);
					fields[nf].removeBut = Button(window, flow.indentedRemaining.width-20@20)
						.states_([
							["remove", Color.white, Color.blue],
							["add", Color.white, Color.red],
						])
					;
					flow.nextLine;
				})
			})
		};

		if(window.isNil or:{ window.isClosed }, {
			window = Window("OSC-command-name collector", Rect(
				Window.screenBounds.width/2-250,
				Window.screenBounds.height/2-250,
				500, 500
			), scroll: true);

			window.onClose_({
				this.collect(false);
				[progressRoutine, collectRoutine].do(_.stop);
				cmdList.clear;
			});

			window.view.decorator = flow = FlowLayout(window.view.bounds, 7@7, 3@3);

			progress = StaticText(window, flow.indentedRemaining.width@30).font_(Font("Arial", 20, true));

			flow.nextLine.shift(0, 0);

			progressStates = Pseq([
				"collecting",
				"collecting .",
				"collecting . .",
				"collecting . . .",
				"collecting . . . .",
				"collecting . . . . .",
				"collecting . . . . . .",
				"collecting . . . . . . .",
				"collecting . . . . . . . .",
				"collecting . . . . . . . . .",
				"collecting . . . . . . . . . .",
				"collecting . . . . . . . . . . .",
				"collecting . . . . . . . . . . . .",
				"collecting . . . . . . . . . . . . .",
				"collecting . . . . . . . . . . . . . .",
				"collecting . . . . . . . . . . . . . . .",
				"collecting . . . . . . . . . . . . . . . .",
				"collecting . . . . . . . . . . . . . . . . .",
				"collecting . . . . . . . . . . . . . . . . . .",
				"collecting . . . . . . . . . . . . . . . . . . .",
				"collecting . . . . . . . . . . . . . . . . . . . .",
				"collecting . . . . . . . . . . . . . . . . . . . . .",
				"collecting . . . . . . . . . . . . . . . . . . . . . .",
				"collecting . . . . . . . . . . . . . . . . . . . . . . .",
				"collecting . . . . . . . . . . . . . . . . . . . . . . . .",
				"collecting . . . . . . . . . . . . . . . . . . . . . . . . .",
				"collecting . . . . . . . . . . . . . . . . . . . . . . . . . .",
				"collecting . . . . . . . . . . . . . . . . . . . . . . . . . . .",
				"collecting . . . . . . . . . . . . . . . . . . . . . . . . . . . .",
				"collecting . . . . . . . . . . . . . . . . . . . . . . . . . . . . .",
				"collecting . . . . . . . . . . . . . . . . . . . . . . . . . . . . . .",
				"collecting . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . .",
				"collecting . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . .",
				"collecting . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . .",
			], inf).asStream;
			progressRoutine = fork({
				loop({
					progress.string_(progressStates.next);
					0.5.wait;
				})
			}, AppClock);

			StaticText(window, 260@40).string_("Collecting command-names will stop as soon as you close this window or save the device's commands. You can only save the command-names after setting a device-name.").font_(staticTextFont);

			// flow.shift(0, 0);

			deviceNameField = TextField(window, 144@40)
				.font_(Font("Andale Mono", 15))
				.string_("< device-name >")
			;

			// flow.shift(0, 0);

			saveBut = Button(window, flow.indentedRemaining.width-20@40)
				.states_([["save", Color.white, Color(0.15, 0.5, 0.15)]])
				.font_(Font("Arial", 15, true))
				.action_({ |b|
					if(deviceNameField.string != "< device-name >" and:{ deviceNameField.string.size > 0 }, {
						this.collect(false);
						[progressRoutine, collectRoutine].do(_.stop);
						fields.pairsDo({ |k, v|
							if(v.removeBut.value == 1, {
								cmdList.removeAt(k);
							})
						});
						this.saveCmdSet(deviceNameField.string.asSymbol);
						window.close;
					})
				})
			;

			collectRoutine = fork({
				loop({
					0.1.wait;
					makeField.(cmdList);
				})
			}, AppClock);
		});

		window.front;
	}

	*deviceCmds { |deviceName|
		var thisDeviceName, thisCmds, cmdsPath;

		deviceName !? { thisDeviceName = deviceName.asSymbol };
		cmdsPath = this.filenameSymbol.asString.dirname +/+ "OSCCommands";
		if(File.exists(cmdsPath), {
			thisCmds = Object.readArchive(cmdsPath);
			if(deviceName.notNil, { ^thisCmds[thisDeviceName] }, { ^thisCmds });
		});
		^nil;
	}

	*clearCmdsAt { |deviceName|
		var cmdsPath, cmds;
		cmdsPath = this.filenameSymbol.asString.dirname +/+ "OSCCommands";
		cmds = Object.readArchive(cmdsPath);
		cmds.removeAt(deviceName.asSymbol);
		cmds.writeArchive(cmdsPath);
	}

	*storedDevices {
		var cmdsPath, cmds;
		cmdsPath = this.filenameSymbol.asString.dirname +/+ "OSCCommands";
		cmds = Object.readArchive(cmdsPath);
		^cmds.keys;
	}

}