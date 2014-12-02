/* $Id: DataCollector.sc 54 2009-02-06 14:54:20Z nescivi $
 *
 * Copyright (C) 2009, Marije Baalman <nescivi _at_ gmail _dot_ com>
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA
 */

// a DataCollector collects data over time, which can be used for realtime analysis
// inspired by MemoryRecorder developed by Alberto de Campo and Marije Baalman

DataCollector{
	classvar <>folder = "DataCollectorBackup/";
	classvar <>initialized = false;

	var <>name,	<>collection;

	//*initClass { this.makeBackupFolder }

	*makeBackupFolder {
		var testfile, testname = "zzz_memory_test_delete_me.txt";
		testfile = File(folder ++ testname, "w");
		if (testfile.isOpen.not)
			{ unixCmd("mkdir" + folder) }
			{ testfile.close;  unixCmd("rm" + folder ++ testname) };
		initialized = true;
	}

	*makeList { ^SortedList[].function_({ |a,b| a[\date] > b[\date] }); }


	*new { |name,collection|
		^super.new.init(name,collection);
	}

	init{ |name,collection|
		if ( initialized.not ){
			this.class.makeBackupFolder;
		};
		this.name = name;
		this.collection = this.class.makeList.addAll(collection);
	}

	addData{ |data,date,overwrite=true|
		var trace;
		if ( date.notNil, {
			trace = (\data:data, \date: date)
		},{
			trace = (\data: data )
		});
		^this.addTrace( trace, overwrite );
	}

	addMetaData{ |mdata,date|
		var metadata;
		if ( date.notNil, {
			metadata = (\metadata: mdata, \date: date) },{
				metadata = (\metadata: mdata ) });
		this.addTrace( metadata );
	}

	addTrace{ |trace,overwrite=true|
		if (trace[\date].isNil, { trace[\date] = Date.getDate.asSortableString });

		if (collection.includes(trace), { ("? trace already stored ").postln; ^this });

		// overwrites data in the trace as needed
		if ( collection.detectIndex{ |it| it[\date] == trace[\date] }.notNil and: overwrite, {
			collection.detect{ |it| it[\date] == trace[\date] }.putAll( trace );
			},{
			collection.add(trace);
		});
		^trace[\date]
	}

	at{ |index|
		if (collection.size == 0, { ^nil });
		^collection.at(index);
	}

	getLast{ |howmany|
		//		^collection.copyToEnd( collection.size - howmany - 1 );
		^collection.copyFromStart( howmany - 1 );
	}

	/// ========  backup functions ===========
	printAll {
		(this.class.asString + name).postln;
		this.all.do { |mem, i|
			("collection:").postln;
			mem.printAll
		}
	}

	storeArgs {
		^[name, collection.array]
	}

	saveTo { |path|
		var file, res = false;
		file = File(path, "w");
		if (file.isOpen) {
			res = file.write(this.asCompileString);
			file.close;
		};
		^res;
	}
	*fromFile { |path| ^path.load; }

	backup {
		var filename = (folder ++ this.name ++ "_" ++ Date.localtime.stamp ++ ".txt").post;
		this.saveTo( filename );
		^filename;
	}

	lastBackupPath { ^pathMatch(folder ++ this.name ++ "*").maxItem; }

	recover {
		var copy, path;
		path = this.lastBackupPath;
		if (path.isNil) { ("no backup found for " + name).postln; ^this };

		copy = path.load;
		if (copy.notNil, { this.init(*copy.storeArgs.postln) }, { "no backup found".postln });
	}

}