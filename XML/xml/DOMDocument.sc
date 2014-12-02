
/* 
 * SuperCollider3 source file "DOMDocument.sc" 
 * Written by Jens Gulden, jgulden@cs.tu-berlin.de.
 * Licensed under the GNU General Public License (GPL),
 * this software comes with NO WARRANTY.
 */

// --- class DOMDocument ------------------------------------------------------
//
// DOMDocument is the main class for acessing XML documents.
// This class and all related DOMXxx-classes implement a subset of the DOM-Level-1
// specification for XML parsers, adopted to the the SuperCollider language. See
// http://www.w3.org/TR/REC-DOM-Level-1/level-one-core.html for the full level 1
// specification of the Document Object Model (DOM).
// -
// All interfaces specified by the DOM, if used at all in this implementation, are
// directly mapped to SuperCollider classes. The original interface inheritance
// hierarchy is thus preserved in the class hierarchy (which is not necessarily
// required for the implementation of interfaces, see DOM-Level-1, 1.1.2).
// -
// Classes implementing the DOM interfaces are:
// DOMDocument -
// The central class for accessing XML documents. This is the only
// class whose instances get created using 'DOMDocument.new', instances of any
// other class are either created internally during parsing of an XML-string,
// or programmatically through calls on 'DOMDocument.createElement(..)',
// 'DOMDocument.createText(..)', etc.
// DOMElement -
// Represents an XML-tag.
// DOMAttr -
// Represents an attribute of an XML-tag. The implementation considers this class as
// a helper class and only partially implements the DOM specification of DOMAttr.
// Attribute values should only be accessed via the corresponding
// getAttribute, setAttribute methods of DOMElement.
// DOMCharacterData -
// Abstract class. This is the common superclass of all nodes in the document which
// carry free-form string content.
// DOMText -
// Text in the document.
// DOMComment -
// Comment in the document (ignored by default during parsing, set DOMDocument.parseComments=true to
// get access to comment nodes).
// DOMCDATASection -
// Raw text section.
// DOMProcessingInstruction -
// Processing instruction nodes.
// DOMNode -
// The common superclass of all nodes. Each node can also be accessed via the
// methods of this class only, which the DOM calls a 'flat'-access to the document
// nodes (flat in terms of meta-model classes, the document-structure of course
// is always composed of nodes in a tree structure described by the parent-children
// relationship, see DOM-Level-1, 1.1.4).
// -
// Some interfaces specified in the DOM-Level-1 are not implemented by this
// adoption:
// DOMString -
// Not implemented, SuperCollider's own String class is used for best integration
// into SuperCollider.
// NodeList, NamedNodeMap -
// Not implemented, SuperCollider's own collection classes are used for best integration
// into SuperCollider.
// DOMException -
// Not implemented, in most cases errors will result from SuperCollider's own
// exception handling, especially in cases of errors resulting from collection classes.
// Parse errors are handled by method DOMDocument.parseError which by default
// will simply output a text message and exit the program. This might be
// overwritten for a more subtle handling of parse errors.
// DOMDocumentType, DOMNotation -
// Not implemented. DTDs are no longer up-to-date technology anyway (use XML-Schema
// instead), and in most cases it will not be necessary to validate an XML document
// from within SuperCollider. External tools could easily be used in cases where
// DTD-based validation is required.
// Calling DOMDocument.getDoctype will always return nil (as allowed by the
// specification, see DOM-Level-1, 1.3). During parsing, doctype declarations will be
// treated like comments.
// DOMEntity, DOMEntityReference -
// Not implemented, only a fixed set of simple character-entities in Text-nodes
// is supported.
// -
// As a notation convention, all methods marked as 'public' ('+') in the UML class diagram
// belong to the implementations of DOM interfaces.
// In contrast to that, all methods marked as 'friendly' ('~') are additions specific to this
// SuperCollider-implementations. The code generated from the class diagram does,
// however, not distinguish between different levels of visibility, so the 'friendly' methods
// are still publicly accessibly from any other SuperCollider class.
// (There are also methods marked as 'protected' ('#'), these are intended to be internal
// methods of the implementation. They should not be called from outside code.)
// -
// Note: The DOM-Level-1 specification covers version 1.0 of the XML standard only.
// Newer versions of the DOM also include features like XML namespaces, which are
// explicitly not supported by this adoption, but might not be required for most
// applications within SuperCollider anyway.
// @poseidon-object-id [sm$1eed0fb:10d5552be97:-7e70]                                                                                    
DOMDocument : DOMNode {

    // --- attributes

    var <>filename = "<none>"; // type  String
    var <>parseComments = false; // type  boolean
    var <>preserveWhitespace = false; // type  boolean
    var <>indent = 4; // type  int
    classvar standardEntities; // type  TwoWayDictionary


    // --- relationships

    var documentElement; // 0..1-relation to type  DOMElement



    // --- new(filename) : void -----------------------------------------------
    //    
    *new { arg filename; // type String        
        ^super.new.initFromFile(filename);
    } // end new        


    // --- getDoctype() : DOMNode ---------------------------------------------
    //    
    getDoctype {        
        // explicitly not implemented, DTDs are not supported by this implementation
        ^nil; // type  DOMNode
    } // end getDoctype        


    // --- getImplementation() : DOMImplementation ----------------------------
    //    
    getImplementation {        
        ^DOMImplementation.instance; // type  DOMImplementation
    } // end getImplementation        


    // --- getDocumentElement() : DOMElement ----------------------------------
    //    
    getDocumentElement {        
        ^documentElement; // type DOMElement
    } // end getDocumentElement        


    // --- createElement(tagname) : DOMElement --------------------------------
    //     
    createElement { arg tagname; // type String        
        var n;
        
        n = DOMElement.new(this, tagname);
        n.initAttributes(Dictionary.new);
        ^n; // type DOMElement
    } // end createElement        


    // --- createDocumentFragment() : DOMDocumentFragment ---------------------
    //    
    createDocumentFragment {        
        ^DOMDocumentFragment.new(this); // type DOMElement
    } // end createDocumentFragment        


    // --- createTextNode(text) : DOMText -------------------------------------
    //     
    createTextNode { arg text; // type String        
        ^DOMText.new(this, text); // type DOMText
    } // end createTextNode        


    // --- createComment(comment) : DOMComment --------------------------------
    //     
    createComment { arg comment; // type String        
        ^DOMComment.new(this, comment); // type DOMComment
    } // end createComment        


    // --- createCDATASection(cdata) : DOMCDATASection ------------------------
    //     
    createCDATASection { arg cdata; // type String        
        ^DOMCDATASection.new(this, cdata); // type DOMCDATASection
    } // end createCDATASection        


    // --- createProcessingInstruction(target, data) : DOMProcessingInstruction
    //      
    createProcessingInstruction { arg target, data; // types String, String        
        ^DOMProcessingInstruction.new(this, target, data); // type DOMProcessingInstruction
    } // end createProcessingInstruction        


    // --- getElementsByTagName(tagname) : List -------------------------------
    //     
    getElementsByTagName { arg tagname; // type String        
        var d;
        d = this.getDocumentElement;
        if ( d != nil , { 
            ^d.getElementsByTagName(tagname); // type  List
        },{
            ^nil;
        });
    } // end getElementsByTagName        


    // --- read(file) : void --------------------------------------------------
    //    
    read { arg file; // type File        
        var s;
        s = String.readNew(file);
        this.parseXML(s);
    } // end read        


    // --- write(file) : void -------------------------------------------------
    //    
    write { arg file; // type File        
        var s;
        
        s = this.format;
        file.putString(s);
    } // end write        


    // --- parseXML(xml) : void -----------------------------------------------
    //    
    parseXML { arg xml; // type String        
        startIndex = 0;
        endIndex = xml.size;
        nodeValue = xml; // nodeValue of document is whole xml string
        this.parse(this, 0);
    } // end parseXML        


    // --- format() : String --------------------------------------------------
    //    
    format {        
        var xml = "";        
        this.getChildNodes.do({ arg node;
            xml = xml ++ node.format(0);
        });
        ^xml;
    } // end format        


    // --- importNode(node) : DOMNode -----------------------------------------
    //     
    importNode { arg node; // type DOMNode        
        var a;
        a = node.getAttributes;
        if ( a != nil , {
            a.do({ arg attr; // make sure that elements will have there attributes parsed
                attr.getNodeName;
                attr.getNodeValue;
            });
        });
        node.init(this, node.getNodeType, node.getNodeName, node.getNodeValue); // re-init to overwrite owner-document
        node.getChildNodes.do({ arg n;
            this.importNode(n);
        });
        ^node; // type  DOMNode
    } // end importNode        


    // --- initFromFile(filename) :  ------------------------------------------
    //    
    initFromFile { arg filename; // type String        
        var f;
        
        this.init(nil, DOMNode.node_DOCUMENT, "#document");
        if ( filename != nil , {
            f = File(filename, "r");
            this.filename = filename;
            this.read(f);
            f.close;        
        });
    } // end initFromFile        


    // --- parse(parentNode, pos) : int ---------------------------------------
    //      
    parse { arg parentNode, pos; // types DOMNode, int        
        // Main parsing function.
        // The idea is to originally parse positions only and remember them
        // in each node's startIndex and endIndex. The actual copyRange happens
        // only on first request of the corresponding values, and then stays
        // cached. This way of parsing is expected to provide best performance.
        var n;
        var trimmed;
        if ( pos < endIndex, {
            if ( nodeValue[pos] == $< , { // tag
                if ( nodeValue[pos + 1] == $!, { // starting with "<!..."
                    if ( nodeValue.containsStringAt(pos + 2, "--"), { 
                        // Comment
                        n = DOMComment.new(this);
                        pos = n.parse(parentNode, pos + 4);
                        if ( parseComments , {
                            parentNode.appendChild(n);
                        });
                    },{
                        if ( nodeValue.containsStringAt(pos + 2, "[CDATA["), { 
                            // CDATA section
                            n = DOMCDATASection.new(this);
                            pos = n.parse(parentNode, pos + 9);
                            parentNode.appendChild(n);
                        },{ // else: anything starting with "<!", e.g. "<!DOCTYPE ...". Treat this as dummy-comments, DTDs are not supported.
                            n = DOMComment.new(this);
                            n.markDummyDoctype; // special start and end
                            pos = n.parse(parentNode, pos + 2);
                            if ( parseComments , {
                                parentNode.appendChild(n);
                            });
                        });
                    });
                },{ // else: starting with '<', but no ! following
                    if ( nodeValue[pos + 1] == $? , { // processing instruction
                        n = DOMProcessingInstruction.new(this);
                        pos = n.parse(parentNode, pos + 2);
                        parentNode.appendChild(n);
                    },{ // else
                        if ( nodeValue[pos + 1] == $/ , { // end-tag: upper recursion level handles this, return here
                            ^pos;
                        },{ // else: regular tag
                            n = DOMElement.new(this);
                            pos = n.parse(parentNode, pos + 1);
                            parentNode.appendChild(n);
                            if ( ( (parentNode === this) && (documentElement == nil) ) , { // set documentElement (root-tag)
                                documentElement = n;
                            });
                        });
                    });
                });
            },{ // else: text
                // Text
                n = DOMText.new(this);
                pos = n.parse(parentNode, pos);
                trimmed = n.getData;
                trimmed = Toolbox.trim( trimmed );
                if ( ( preserveWhitespace || ( trimmed != "" ) ) , { // ignore whitespace-only text if !preserveWhitespace
                    if ( not (preserveWhitespace) , {
                        n.setData(trimmed); // always return trimmed text if not preserveWhitespace
                    });
                    parentNode.appendChild(n);
                });
            });            
            // one node has been parsed now, continue recursively with next sibling
            ^this.parse(parentNode, pos); // recursion
        },{ // else: end of input reached
            ^endIndex;
        });
    } // end parse        


    // --- parseQuoted(pos, result) : int -------------------------------------
    //      
    parseQuoted { arg pos, result; // types int, Ref        
        // returns next character position after quoted string, resultString vie Ref
        // pos points at initial quote ( ' or " )
        var s;
        var q;
        var esc;
        var xml;
        xml = this.getNodeValue;
        q = xml[pos]; // char used for quoting
        if ( (q.ascii != 34) && (q.ascii != 39), { // " or '
            ("XML parser: quoted value (starting with ' or \") expected in file " ++ this.filename ++ ", line " ++ this.linenr(pos)).die; //" (ws08)
        });
        s = "";
        esc = false;
        pos = pos + 1;
        while ( { ( (pos < xml.size) && ( (xml[pos] != q) || (esc==true) ) ) } , {
            if ( ( (xml[pos] != $\\) || (esc==true) ) , {
                s = s ++ xml[pos];
                esc = false;
            },{
                esc = true;
            });
            pos = pos + 1;
        });
        if ( (pos >= xml.size) , {
            ("XML parser: unclosed quoted string at end of document in file " ++ this.filename ++ ", expected " ++ q).die;
        });
        result.set(s);
        ^(pos + 1);
    } // end parseQuoted        


    // --- linenr(pos) : int --------------------------------------------------
    //     
    linenr { arg pos; // type int        
        // find out in which line a charter index is located (may be costy, because when reached here, we are creating the string for an error message)
        var s;
        var ss; // String[]
        
        s = nodeValue.copyFromStart(pos);
        ss = s.split(Char.nl); // linux
        ^ss.size;
    } // end linenr        


    // --- skipUntil(pos, delim) : int ----------------------------------------
    //      
    skipUntil { arg pos, delim; // types int, String        
        var xml = this.getNodeValue;
        while ( { not ( delim.includes(xml[pos]) ) } , {
            if ( (xml[pos].ascii == 34) || (xml[pos].ascii == 39) , { // " or ', quoted part starts
            	
                pos = this.parseQuoted( pos, Ref.new( nil ) ); // dummy Ref
            },{
                pos = pos + 1;
            });
            if ( pos > xml.size, {
                ("Unclosed string at end of document in file " ++ this.filename ++ ", expected one of '" ++ delim ++ "'").die;
            });
        });
        ^pos; // type int
    } // end skipUntil        


    // --- parseError(message, pos) :  ----------------------------------------
    //     
    parseError { arg message, pos; // types String, int        
        message = "XML parser: " ++ message;
        if (this.filename != nil , {
            message = message ++ " in file " ++ this.filename;
        });
        message = message ++ ", line " ++ this.linenr(pos);
        message.die; // stop executing program
    } // end parseError        


    // --- initStandardEntities() : void --------------------------------------
    //   
    *initStandardEntities {        
        // entity-handling is very simple, based on string replacement.    
        if ( standardEntities == nil , {
            standardEntities = TwoWayDictionary.new;
            standardEntities.add( "&" -> "amp" );
            standardEntities.add( "<" -> "lt" );
            standardEntities.add( ">" -> "gt" );
        });
    } // end initStandardEntities        


    // --- decodeStandardEntities(s) : String ---------------------------------
    //     
    *decodeStandardEntities { arg s; // type String        
        var amppos = s.indexOf( $& );
        var sempos;
        var entity;
        var resolved;
        
        DOMDocument.initStandardEntities;
        if ( amppos != nil , {
            sempos = s.indexOf( $; , amppos );
            if ( sempos != nil , {
                entity = s.copyRange(amppos + 1, sempos - 1);
                if ( entity[0] == $# , {
                    // convert from ASCII code
                    resolved = entity.copyToEnd(1).asInteger.asAscii;
                },{
                    // look up in tabe
                    resolved = standardEntities.atValue(entity);
                });
                ^( s.copyFromStart(amppos - 1) ++ resolved ++ DOMDocument.decodeStandardEntities( s.copyToEnd(sempos + 1) ) ); // recursion
            },{
                ^s;
            });
        },{
            ^s;
        });
    } // end decodeStandardEntities        


    // --- encodeStandardEntities(s) : String ---------------------------------
    //     
    *encodeStandardEntities { arg s; // type String        
        var pos;
        var c;
        var r;
        var k;
        
        DOMDocument.initStandardEntities;
        // replace standard entities
        k = standardEntities.keys.asList;
        k.remove("&");
        k.insert(0, "&"); // make sure & comes first
        k.do({ arg ch;
            pos = 0;
            while ( { pos < s.size } , {
                if ( s[pos] == ch[0] , { // found a character to replace with entity (first run of loop must replace & with &amp;)
                    r = "&" ++ standardEntities.at(ch) ++ ";";
                    s = s.copyFromStart(pos - 1) ++ r ++ s.copyToEnd(pos + 1);
                    pos = pos + r.size;
                },{
                    pos = pos + 1;
                });
            });
        });
        // replace non-standard characters with ASCII-entity encoding
        pos = 0;
        while ( { pos < s.size } , {
            c = s[pos];
            if ( ( ( not ( c.isSpace ) ) && ( not ( c.isPrint && ( c.isAlphaNum || c.isPunct || c.isSpace ) ) ) ) , { // found a character to replace
                r = "&#" ++ s[pos].ascii ++ ";";
                s = s.copyFromStart(pos - 1) ++ r ++ s.copyToEnd(pos + 1);
                pos = pos + r.size;
            },{ // else
                pos = pos + 1;
            });
        });
        ^s; // type String
    } // end encodeStandardEntities        


} // end DOMDocument
