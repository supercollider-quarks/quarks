
/* 
 * SuperCollider3 source file "Toolbox.sc" 
 * Written by Jens Gulden, jgulden@cs.tu-berlin.de.
 * Licensed under the GNU General Public License (GPL),
 * this software comes with NO WARRANTY.
 */

// --- class Toolbox ----------------------------------------------------------
//
// <p></p>
// @poseidon-object-id [sm$1eed0fb:10d5552be97:-7ccf]  
Toolbox {

    // --- trimLeft(s) : String -----------------------------------------------
    //     
    *trimLeft { arg s; // type String        
        var i = 0;
        
        if ( s.size > 0 , {
            if ( s[0].isSpace , {
                while ( { (i < (s.size-1)) && (s[i].isSpace) } , { i = i + 1 } );
                if ( ((i < (s.size-1)) || (not (s[i].isSpace))) , {
                    ^s.copyToEnd(i);
                },{
                    ^"";
                });
            },{
                ^s;
            });
        },{
            ^"";
        });
    } // end trimLeft        


    // --- trimRight(s) : String ----------------------------------------------
    //     
    *trimRight { arg s; // type String        
        var i = s.size - 1;
        if ( i >= 0 , {
            if ( s[i].isSpace , {
                while ( { ( i > 0 ) && (s[i].isSpace) } , { i = i - 1 } );
                if ( ((i > 0) || (not (s[i].isSpace))) , {
                    ^s.copyFromStart(i);
                },{
                    ^"";
                });
            },{
                ^s;
            });
        },{
            ^"";
        });
    } // end trimRight        


    // --- trim(s) : String ---------------------------------------------------
    //     
    *trim { arg s; // type String        
        ^Toolbox.trimLeft( Toolbox.trimRight( s ) );
    } // end trim        


    // --- repeat(s, n) : String ----------------------------------------------
    //      
    *repeat { arg s, n; // types String, int        
        if ( n == 0 , {
            ^"";
        },{
            ^(s ++ Toolbox.repeat( s, n - 1 )); // recursion
        });
    } // end repeat        


    // --- isMultiline(s) : boolean -------------------------------------------
    //     
    *isMultiline { arg s; // type String        
        ^( s.find(Char.nl) != nil ); // type  boolean
    } // end isMultiline        


    // --- split(s, delim) : List ---------------------------------------------
    //      
    *split { arg s, delim; // types String, String        
        var l = List.new;
        var f;
        var pos;
        
        f = { arg s;
            pos = s.find(delim);
            if ( pos != nil , {
                l.add(s.copyFromStart(pos-1));
                f.value(s.copyToEnd(pos+delim.size)); // recursion
            },{
                l.add(s);
            });
        };        
        f.value(s);
        ^l; // type List
    } // end split        


    // --- splitLines(s) : List -----------------------------------------------
    //     
    *splitLines { arg s; // type String        
        ^Toolbox.split(s, Char.nl);
    } // end splitLines        


    // --- join(l, delim) : String --------------------------------------------
    //      
    *join { arg l, delim; // types List, String        
        var s = nil;
        
        l.do({ arg e;
            if ( s == nil , {
                s = e;
            },{
                s = s ++ delim ++ e;
            });
        });
        ^s;
    } // end join        


    // --- joinLines(l) : String ----------------------------------------------
    //     
    *joinLines { arg l; // type List        
        ^Toolbox.join(l, Char.nl); // type  String
    } // end joinLines        


    // --- indent(s, shift) : String ------------------------------------------
    //      
    *indent { arg s, shift; // types String, int        
        var lines;
        var ind;
        
        lines = Toolbox.splitLines(s);
        ind = Toolbox.repeat(" ", shift);
        lines.do({ arg l, index;
            lines.put(index, (ind ++ Toolbox.trimLeft(l)));
        });
        ^Toolbox.joinLines(lines);
    } // end indent        


    // --- unindent(s, shift) : String ----------------------------------------
    //      
    *unindent { arg s, shift; // types String, int        
        // if shift==-1: unindent to maximum possible (at least one line will start without blanks, oters are shifted relatively)
        var lines;
        var spaces;
        
        lines = Toolbox.splitLines(s);
        if ( shift == -1 , { // first find shift value: minimum number of blanks at the beginning of lines
            lines.do({ arg line;
                spaces = line.size - Toolbox.trimLeft(line).size;
                if ( ( (shift == -1) || (spaces < shift) ) , {
                    shift = spaces;
                });
            });
        });
        
        if ( shift > 0 , {
            lines.do({ arg line, index;
                line = line.copyToEnd(shift); // cut left part
                lines.put(index, line);
            });
        });
        
        ^Toolbox.joinLines(lines); // type  String
    } // end unindent        


    // --- indentAbs(s, shift) : String ---------------------------------------
    //      
    *indentAbs { arg s, shift; // types String, int        
        // indent to absolute shifting, i.e. independently of original indentation        
        ^Toolbox.indent( Toolbox.unindent(s, -1), shift ); // type  String
    } // end indentAbs        


} // end Toolbox
