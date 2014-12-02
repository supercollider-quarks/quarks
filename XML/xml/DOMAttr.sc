
/* 
 * SuperCollider3 source file "DOMAttr.sc" 
 * Written by Jens Gulden, jgulden@cs.tu-berlin.de.
 * Licensed under the GNU General Public License (GPL),
 * this software comes with NO WARRANTY.
 */

// --- class DOMAttr ----------------------------------------------------------
//
// <p></p>
// @poseidon-object-id [sm$1eed0fb:10d5552be97:-7d89]  
DOMAttr : DOMNode {


    // --- parse(parentNode, pos, endIndex) : void ----------------------------
    //      
    parse { arg parentNode, pos, endIndex; // types DOMNode, int, int        
        // pos points on first char of key/value-pair, thus the beginning of the attribute name 
        var xml;
        var nodeValueRef;
        // special: init here
        this.init(parentNode);
        startIndex = pos;
        xml = parent.getOwnerDocument.getNodeValue;
        while ( { ( (pos < endIndex) &&  (not ( "= \t\n\r/>".includes( xml[pos] ) ) ) ) } , { pos = pos + 1 } ); // scan attribute name
        nodeName = xml.copyRange(startIndex, pos - 1);
        if ( pos < endIndex, {
            while ( { xml[pos].isSpace } , { pos = pos + 1 } ); // allow whitespace
            // now: either a '=' and a value follows, or we are finished and it's a flag-attribute without value
            splitIndex = pos;
            if ( xml[pos] == $=, { // '=', value follows                
                pos = pos + 1;
                while ( { xml[pos].isSpace } , { pos = pos + 1 } ); // allow whitespace
                //pos = parent.getOwnerDocument.parseQuoted(pos `nodeValue);
                nodeValueRef = Ref.new(nil);
                //pos = parent.getOwnerDocument.parseQuoted(pos, nodeValue.asRef );
                pos = parent.getOwnerDocument.parseQuoted(pos, nodeValueRef );
                nodeValue = nodeValueRef.value;
                endIndex = pos;
            },{
                nodeValue = "";
            });
        },{
            parent.getOwnerDocument.parseError("invalid attribute name " ++ nodeName, pos);
        });
        ^pos;
    } // end parse        


    // --- format(indentLevel) : String ---------------------------------------
    //     
    format { arg indentLevel; // type int        
        // nothing here, attribute formatting is done by DOMElement
        ^nil; // type String
    } // end format        


    // --- init(parentNode) :  ------------------------------------------------
    //    
    init { arg parentNode; // type DOMNode        
        nodeType = DOMNode.node_ATTRIBUTE;
        ownerDocument = parentNode.getOwnerDocument;
        parent = parentNode;        
    } // end init        


} // end DOMAttr
