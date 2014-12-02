
/* 
 * SuperCollider3 source file "DOMElement.sc" 
 * Written by Jens Gulden, jgulden@cs.tu-berlin.de.
 * Licensed under the GNU General Public License (GPL),
 * this software comes with NO WARRANTY.
 */

// --- class DOMElement -------------------------------------------------------
//
// <p></p>
// @poseidon-object-id [sm$1eed0fb:10d5552be97:-7f58]  
DOMElement : DOMNode {

    // --- relationships

    var attrs; // 0..*-relation to type  DOMAttr



    // --- new(owner, tagname) : DOMElement -----------------------------------
    //      
    *new { arg owner, tagname; // types DOMDocument, String        
        ^super.new.init(owner, DOMNode.node_ELEMENT, tagname);
    } // end new        


    // --- getTagName() : String ----------------------------------------------
    //    
    getTagName {        
        ^this.getNodeName; // type String
    } // end getTagName        


    // --- getAttribute(name) : String ----------------------------------------
    //     
    getAttribute { arg name; // type String        
        var n;
        n = this.getAttributeNode(name);
        if ( n != nil , {
            ^n.getNodeValue;
        },{
            ^nil;
        });
    } // end getAttribute        


    // --- setAttribute(name, value) :  ---------------------------------------
    //     
    setAttribute { arg name, value; // types String, String        
        var attr;
        
        attr = this.getAttributeNode(name);
        if ( attr == nil , {
            attr = DOMAttr.new.init(this);
            attributes.put(name, attr);
        });
        attr.setNodeValue(value);
    } // end setAttribute        


    // --- removeAttribute(name) : void ---------------------------------------
    //    
    removeAttribute { arg name; // type String        
        this.getAttributes.removeAt(name);
    } // end removeAttribute        


    // --- getAttributeNode(name) : DOMAttr -----------------------------------
    //     
    getAttributeNode { arg name; // type String        
        ^this.getAttributes.at(name);
    } // end getAttributeNode        


    // --- getElementsByTagName(tagname) : List -------------------------------
    //     
    getElementsByTagName { arg tagname; // type String        
        ^this.select( { arg node;  ( (node.getNodeType == DOMNode.node_ELEMENT) && ( tagname == node.getNodeName ) ) } );
    } // end getElementsByTagName        


    // --- normalize() : void -------------------------------------------------
    //   
    normalize {        
        var n2;
        
        children.do({ arg node, index;
            if ( node.getNodeType == DOMNode.node_TEXT , {
                if ( (index < (children.size-1)) , {
                    n2 = children[index+1];
                    if ( n2.getNodeType == DOMNode.node_TEXT , { // found two directly adjacent text-nodes: join them
                        node.appendData( n2.getData );
                        this.removeChild( n2 );
                        this.normalize; // recursion to cover possible next occurrence
                        ^this; // break looping over children, return
                    });
                });
            });
        });
    } // end normalize        


    // --- getText() : String -------------------------------------------------
    //    
    getText {        
        var n;
        n = this.getFirstChild;
        if ( n != nil , {
            if ( n.getNodeType == DOMNode.node_TEXT , {
                ^n.getText;
            });
        },{
            ^nil; // type  String
        });
    } // end getText        


    // --- getElement(tagname) : DOMElement -----------------------------------
    //     
    getElement { arg tagname; // type String        
        ^this.detect( { arg node;  ( (node.getNodeType == DOMNode.node_ELEMENT) && ( tagname == node.getNodeName ) ) } ); // type  DOMElement
    } // end getElement        


    // --- hasAttributes() : boolean ------------------------------------------
    //    
    hasAttributes {        
        ^(not (this.getAttributes.isEmpty)); // type boolean
    } // end hasAttributes        


    // --- hasAttribute(name) : boolean ---------------------------------------
    //     
    hasAttribute { arg name; // type String        
        ^this.getAttributes.keys.includes(name); // type String
    } // end hasAttribute        


    // --- parse(parentNode, pos) : void --------------------------------------
    //     
    parse { arg parentNode, pos; // types DOMNode, int        
        var xml;
        var attr;
        var p;
        var oldpos;
        // pos initially points to the first char after '<', the beginning of the tagname (no whitespace allowed there)
        // next whitespace divides tagname from possible attributes, > lets start-tag end, /> closes tag
        startIndex = pos;
        xml = this.getOwnerDocument.getNodeValue;
        while ( { ( pos < xml.size ) && ( not ( " \t\n\r/>".includes( xml[pos] ) ) ) } , { pos = pos + 1 } ); // find whitespace delimiter after tagname
        splitIndex = pos;
        // do not get tagname via copyRange yet, only on request later
        // (the application might not even be interested in the tag name, so why do more strcp than needed)
        // also do not parse attributes yet, only search for tag end now
        // (attributes will be parsed on first access between position splitIndex and endIndex)
        pos = this.getOwnerDocument.skipUntil(pos, ">/");
        // tag ends: children or directly closed?
        endIndex = pos;
        if ( pos >= xml.size , {
            this.getOwnerDocument.parseError("unfinished start-tag (" ++ this.getNodeName ++ ")", pos);
        });
        if ( xml[pos] == $> , { // not directly closed: is start-tag, end-tag required, children possible
            pos = pos + 1;
            // pos now on first char after '>', end-tag or children may start
            nodeName = xml.copyRange(startIndex, splitIndex - 1); // tagname needed to validate end tag
            p = "</" ++ nodeName; // end-pattern
            while ( { not (xml.containsStringAt(pos, p)) } , { // no end-tag yet
                // DOMDocument handles general parsing of nodes of any kind and appends them as children
                oldpos = pos;
                pos = this.getOwnerDocument.parse(this, pos); // indirect recursion
                if ( ( (pos == oldpos) || (pos >= xml.size) ) , {
                    this.getOwnerDocument.parseError("unclosed tag, end-tag </" ++ this.getNodeName ++ "> expected", pos);
                });
            });
            pos = pos + p.size;
            while ( { xml[pos].isSpace } , { pos = pos + 1 } ); // allow whitespace before end-tag is finished with '>'
            if ( xml[pos] != $> , {
                this.getOwnerDocument.parseError("unfinished end-tag " ++ this.getNodeName, pos);
            });
        },{ // else: directly closed tag ( .../> )
            pos = pos + 1;
            if ( xml[pos] != $> , {
                this.getOwnerDocument.parseError("unfinished tag " ++ this.getNodeName, pos);
            });
            pos = pos + 1;
        });
        ^(pos + 1);
    } // end parse        


    // --- format(indentLevel) : String ---------------------------------------
    //     
    format { arg indentLevel; // type int        
        var xml;
        var attrVal;
        var whitespace;
        var selfIndent;
        var childIndent;
        var type;
        var multiline;
        
        if (indentLevel == nil, { indentLevel = 0 });
        whitespace = this.getOwnerDocument.preserveWhitespace;
        xml = "<" ++ this.getTagName;
        this.getAttributes.keysDo({ arg attrName;
            attrVal = this.getAttribute(attrName);
            xml = xml ++ " " ++ attrName ++ "=\"" ++ attrVal.escapeChar(34.asAscii) ++ "\""; // 34=\"
        });
        if ( this.hasChildNodes , {
            xml = xml ++ ">";
            selfIndent = Toolbox.repeat(" ", this.getOwnerDocument.indent * indentLevel);
            childIndent = Toolbox.repeat(" ", this.getOwnerDocument.indent * (indentLevel+1));
            type = this.getFirstChild.getNodeType;
            if ( ( (this.getChildNodes.size == 1) && ( (type == DOMNode.node_TEXT) || (type == DOMNode.node_COMMENT) ) ) , {
                multiline = Toolbox.isMultiline(this.getFirstChild.getData); // no indentation if single line text or comment
            },{
                multiline = true; // treat any other node type as multiline
            });
            if ( ( (not (whitespace)) && (this.getOwnerDocument.indent > 0) && (multiline) ) , {
                // indentation
                xml = xml ++ Char.nl;
                this.getChildNodes.do({ arg node, index;
                    if ( index > 0 , {
                        xml = xml ++ Char.nl;
                    });
                    xml = xml ++ childIndent ++ node.format(indentLevel + 1);
                });
                xml = xml ++ Char.nl ++ selfIndent;
            },{ // else: no indentation
                this.getChildNodes.do({ arg node;
                    xml = xml ++ node.format(0);
                });
            });
            xml = xml ++ "</" ++ this.getTagName ++ ">";
        },{ // else
            xml = xml ++ "/>";
        });
        ^xml;
    } // end format        


    // --- parseAttributes() : void -------------------------------------------
    //   
    parseAttributes {        
        var pos;    
        var attr;
        var xml = this.getOwnerDocument.getNodeValue;
        var n;
        if ( attributes == nil, { // not cached already
            // parse attributes, this is done once on first access to attibutes and then cached
            attributes = Dictionary.new;
            pos = splitIndex;
            while ( { xml[pos].isSpace } , { // attribute(s) may follow
                pos = pos + 1;
                while ( { xml[pos].isSpace } , { pos = pos + 1 } ); // skip whitespace [ && (pos < this.endIndex) implicit]
                if ( pos < endIndex , {
                    // parse attribute
                    attr = DOMAttr.new;
                    pos = attr.parse(this, pos, endIndex); // special to DOMAttr: initialization done via parse
                    n = attr.getNodeName;
                    attributes.put(n, attr);
                });
            });
        });
    } // end parseAttributes        


    // --- initAttributes(attrs) :  -------------------------------------------
    //    
    initAttributes { arg attrs; // type Dictionary        
        attributes = attrs;
    } // end initAttributes        


    // --- getNodeName() : String ---------------------------------------------
    //    
    getNodeName {        
        // overwrites DOMNode.getNodeName
        
        if ( nodeName == nil, { // first access, cache now
            nodeName = this.getOwnerDocument.getNodeValue.copyRange(startIndex, splitIndex - 1);
        });
        ^nodeName;
    } // end getNodeName        


    // --- getAttributes() : Dictionary ---------------------------------------
    //    
    getAttributes {        
        // overwrites DOMNode.getAttributes        
        this.parseAttributes;
        ^attributes; // type Dictionary
    } // end getAttributes        


} // end DOMElement
