
/* 
 * SuperCollider3 source file "DOMProcessingInstruction.sc" 
 * Written by Jens Gulden, jgulden@cs.tu-berlin.de.
 * Licensed under the GNU General Public License (GPL),
 * this software comes with NO WARRANTY.
 */

// --- class DOMProcessingInstruction -----------------------------------------
//
// <p></p>
// @poseidon-object-id [sm$1eed0fb:10d5552be97:-7faf]  
DOMProcessingInstruction : DOMNode {

    // --- new(owner, target, data) :  ----------------------------------------
    //      
    *new { arg owner, target, data; // types DOMDocument, String, String        
        ^super.new.init(owner, DOMNode.node_PROCESSING_INSTRUCTION, target, data);
    } // end new        


    // --- getTarget() : String -----------------------------------------------
    //    
    getTarget {        
        ^this.getNodeName;
    } // end getTarget        


    // --- getData() : String -------------------------------------------------
    //    
    getData {        
        ^this.getNodeValue;
    } // end getData        


    // --- setData(data) :  ---------------------------------------------------
    //    
    setData { arg data; // type String        
        setNodeValue(data);
    } // end setData        


    // --- parse(parentNode, pos) : int ---------------------------------------
    //      
    parse { arg parentNode, pos; // types DOMNode, int        
        var xml;
        // pos points on first char of target name
        xml = this.getOwnerDocument.getNodeValue;
        startIndex = pos;
        while ( { (pos < xml.size) && (not ( xml[pos].isSpace) ) } , { pos = pos + 1 } ); // skip until whitespace
        if ( pos >= xml.size , {
            this.getOwnerDocument.parseError("unfinished processing instruction", pos);
        });
        splitIndex = pos;
        while ( { (pos < xml.size) && (xml[pos] != $>) } , { pos = pos + 1 } ); // skip to end
        if ( pos >= xml.size , {
            this.getOwnerDocument.parseError("unfinished processing instruction", pos);
        });
        endIndex = pos - 1;
        ^(pos + 1);
    } // end parse        


    // --- format(indentLevel) : String ---------------------------------------
    //     
    format { arg indentLevel; // type int        
        var indentStr;
        if (indentLevel  == nil, { indentLevel = 0 });
        indentStr = Toolbox.repeat(" ", this.getOwnerDocument.indent * indentLevel);
        ^(indentStr ++ "<?" ++ this.getNodeName ++ " " ++ this.getData ++ "?>");
    } // end format        


    // --- getNodeName() : String ---------------------------------------------
    //    
    getNodeName {        
        // overwrites DOMNode.getNodeName
        
        if ( nodeName == nil, { // first access, cache now
            nodeName = this.getOwnerDocument.getNodeValue.copyRange(startIndex, splitIndex - 1);
        });
        ^nodeName;
    } // end getNodeName        


    // --- getNodeValue() : String --------------------------------------------
    //    
    getNodeValue {        
        // overwrites DOMNode.getNodeValue
        
        if ( nodeValue == nil, { // first access, cache now
            nodeValue = this.getOwnerDocument.getNodeValue.copyRange(splitIndex + 1, endIndex - 1);
        });
        ^nodeValue;
    } // end getNodeValue        


} // end DOMProcessingInstruction
