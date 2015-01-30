/*
    Unit Library
    The Game Of Life Foundation. http://gameoflife.nl
    Copyright 2006-2011 Miguel Negrao, Wouter Snoei.

    GameOfLife Unit Library: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    GameOfLife Unit Library is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with GameOfLife Unit Library.  If not, see <http://www.gnu.org/licenses/>.
*/

UArchivable {

	var <>filePath, <lastVersionSaved;

    isDirty{
	    ^lastVersionSaved !? { |x| this.asTextArchive !=  x} ? true
	}

    archiveAsCompileString { ^true }

    // Subclasses need to implement this to get all the goodies !
    getInitArgs { this.subclassResponsibility(thisMethod) }

    *readTextArchive { |pathname|
	    var res, sub;
	    res = pathname.load;
	    sub = this.subclasses;
	    sub = sub ? [];
	    if( res.class == this or: { this.subclasses.includes( res.class ) } ) {
	        res.filePath_(pathname);
	        res.readTextArchiveAction;
		   ^res;
	    } {
		    "%:readTextArchive - wrong type (%)\n".postf( this, res );
		    ^nil;
	    }
    }

    readTextArchive { |pathname|
	    var res;
	    res = this.class.readTextArchive( pathname );
	    if( res.notNil ) {
		    this.init( res.getInitArgs );
	    };
	    filePath = pathname;
    }
    
    textArchiveFileExtension { ^nil }

    write { |path, overwrite=false, ask=true, successAction, cancelAction|
	    var writeFunc;
	    writeFunc = { |overwrite, ask, path|
		    var text;
		    
		    GlobalPathDict.relativePath = path.dirname;
		    text = this.asTextArchive;
		    GlobalPathDict.relativePath = nil;
		    
		    if( this.textArchiveFileExtension.notNil ) {
			    path = path.replaceExtension( this.textArchiveFileExtension );
		    };
		    File.checkDo( path, { |f|
				f.write( text );
				successAction.value(path);
			}, overwrite, ask);
	    };

	    if( path.isNil ) {
		    Dialog.savePanel( { |pth|
			    path = pth;
			    writeFunc.value(true,false,path);
		    }, cancelAction );
	    } {
		    writeFunc.value(overwrite,ask,path);
	    };
    }

    read { |path, action|
         var score;

        if( path.isNil ) {
		    Dialog.getPaths( { |paths|
	             this.readTextArchive( paths[0] );
	             action.value(score);
	        });
	    } {
	            path = path.standardizePath;
	            this.readTextArchive( path );
	            action.value(score);
	    };
    }

    *read { |path, action|
        var score;

        if( path.isNil ) {
		    Dialog.getPaths( { |paths|
	             score = this.readTextArchive( paths[0] );
	             action.value(score);
	             score
	        });
	    } {
	            path = path.standardizePath;
	            score = this.readTextArchive( path );
	            action.value(score);
	            ^score
	    };
    }

    save { |successAction, cancelAction|
	    if(this.isDirty){
            filePath !? { |x| this.write(x,true, true,
                { |x| filePath = x; lastVersionSaved = this.asTextArchive; this.onSaveAction; successAction.value}, cancelAction) } ?? {
                this.saveAs(nil,successAction, cancelAction)
            }
        }
	}

	saveAs { |path, successAction, cancelAction|
	    this.write(path, true, true,
	        { |x| filePath = x; lastVersionSaved = this.asTextArchive; this.onSaveAction; successAction.value}, cancelAction)
	}

	onSaveAction{ this.subclassResponsibility(thisMethod) }
	
	readTextArchiveAction{ this.subclassResponsibility(thisMethod) }

}