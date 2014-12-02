
/* 
 * SuperCollider3 source file "DOMCharacterData.sc" 
 * Written by Jens Gulden, jgulden@cs.tu-berlin.de.
 * Licensed under the GNU General Public License (GPL),
 * this software comes with NO WARRANTY.
 */

// --- class DOMCharacterData -------------------------------------------------
//
// <p></p>
// @poseidon-object-id [sm$1eed0fb:10d5552be97:-7ef7]  
DOMCharacterData : DOMNode {

    // --- attributes

    var until; // type  String
    var start; // type  String
    var end; // type  String


    // --- getData() : String -------------------------------------------------
    //    
    getData {        
        ^this.getNodeValue; // type String
    } // end getData        


    // --- setData(data) : void -----------------------------------------------
    //    
    setData { arg data; // type String        
        this.setNodeValue(data);
    } // end setData        


    // --- getLength() : int --------------------------------------------------
    //    
    getLength {        
        ^this.getData.size; // type int
    } // end getLength        


    // --- substringData(offset, count) : String ------------------------------
    //      
    substringData { arg offset, count; // types int, int        
        ^this.getData.copyRange(offset, offset + count - 1); // type String
    } // end substringData        


    // --- appendData(data) : String ------------------------------------------
    //     
    appendData { arg data; // type String        
        this.setData( this.getData ++ data );
    } // end appendData        


    // --- insertData(offset, data) : void ------------------------------------
    //     
    insertData { arg offset, data; // types int, String        
        this.setData( this.getData.copyFromStart(offset) ++ data ++ this.getData.copyToEnd(offset + 1) );
    } // end insertData        


    // --- deleteData(offset, count) : void -----------------------------------
    //     
    deleteData { arg offset, count; // types int, int        
        this.setData( this.getData.copyFromStart(offset) ++ this.getData.copyToEnd(offset + count + 1) );
    } // end deleteData        


    // --- replaceData(offset, count, data) :  --------------------------------
    //      
    replaceData { arg offset, count, data; // types int, int, String        
        this.deleteData(offset, count);
        this.insertData(offset, data);
    } // end replaceData        


    // --- init(owner, type, name, value, s, e, u) : void ---------------------
    //          
    init { arg owner, type, name, value, s, e, u; // types DOMDocument, int, String, String, String, String, String        
        super.init(owner, type, name, value);
        start = s;
        end = e;
        until = u;
    } // end init        


    // --- parse(parentNode, pos) : int ---------------------------------------
    //      
    parse { arg parentNode, pos; // types DOMNode, int        
        var xml;
        var xmlRest;
        var delim;
        
        startIndex = pos;
        if ( end != "" , {
            delim = end; // dedicated end code (e.g. ']]>' for DOMCDATASection)
        },{ // else
            delim = until; // (e.g. DOMText)
        });
        xml = this.getOwnerDocument.getNodeValue;
        endIndex = xml.find(delim, false, pos); // undocumented parameters: ignoreCase=false, offset=pos
        if ( endIndex == nil, {
            if ( end != "", {
                this.getOwnerDocument.parseError("unfinished " ++ this.getNodeName.copyToEnd(1), pos);
            },{ // else
                pos = xml.size; // DOMText at end of input
                endIndex = pos - 1;
            });
        },{ // else
            pos = endIndex;
            if ( end != "" , { // delimiter is part of node
                pos = pos + delim.size;
            });            
            endIndex = endIndex - 1;
        });
        // don't do copyRange to set nodeValue, only on first access and then cache (see getNodeValue)
        ^pos;
    } // end parse        


    // --- format(indentLevel) : String ---------------------------------------
    //     
    format { arg indentLevel; // type int        
        var formatted;
        var indentStr;
        var endformatted = "";
        
        if (indentLevel  == nil, { indentLevel = 0 });
        formatted = this.getDataFormatted;
        if ( ( ( not (this.getOwnerDocument.preserveWhitespace) ) && ( (this.getNodeType == DOMNode.node_TEXT) || (this.getNodeType == DOMNode.node_COMMENT) ) ) , { // maybe perform indentation
            if ( Toolbox.isMultiline(formatted) , {
                if ( this.getNodeType == DOMNode.node_COMMENT, {
                    indentStr = Toolbox.repeat(" ", this.getOwnerDocument.indent * indentLevel);
                    indentLevel = indentLevel + 1; // text block is again indented
                    endformatted = Char.nl ++ indentStr; // prepare for end-mark (-->)
                });
                formatted = Toolbox.trim( Toolbox.indentAbs( Toolbox.trim(formatted), (indentLevel * this.getOwnerDocument.indent) ) ) ++ endformatted;
            });
        });
        ^(start ++ formatted ++ end); // type String
    } // end format        


    // --- getDataFormatted() : String ----------------------------------------
    //    
    getDataFormatted {        
        // returns data prepared for output into XML document
        // will be overwritten by DOMText to perform entity encoding first
        ^this.getData; // type  String
    } // end getDataFormatted        


    // --- getNodeValue() : String --------------------------------------------
    //    
    getNodeValue {        
        // overwrites DOMNode.getNodeValue
        
        if ( nodeValue == nil, { // first access, cache now
            nodeValue = this.getOwnerDocument.getNodeValue.copyRange(startIndex, endIndex);
        });
        ^nodeValue;
    } // end getNodeValue        


} // end DOMCharacterData
