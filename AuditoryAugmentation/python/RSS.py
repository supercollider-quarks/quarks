#!/usr/bin/env python

"""
RSS.py

Classes for working with RSS channels as arbitrary data structures.
Requires Python 2.2 or newer and PyXML 0.7.1 or newer.

  ChannelBase - Base class for RSS Channels.
  CollectionChannel - RSS Channel modeled as a URI-per-entry 
                    dictionary.
  TrackingChannel - RSS Channel modeled as an item-per-entry 
                    dictionary.
  RSSParser - Multi-format RSS/XML Parser.
  
Typically, the *Channel clases will be most useful to developers.  
  
This library provides tools for working with RSS feeds as data
structures. The core is an RSS parser capable of understanding most
RSS formats, and a serializer that produces RSS1.0. The RSS channel
itself can be represented as any arbitrary data structure; two such
structures are provided both as examples and to service common
usage. This approach allows channels to be manipulated and stored in
a fashion that suits both their semantics and the applications that
access them.

Both the parser and the serializer have the following limitations:
  - RSS 1.0 "rich content" modules are not supported
  - RSS 0.9x features that rely on attributes are not supported
  - RDF is not understood; this library does not expose statements or
    understand RDF syntax beyond that documented in RSS1.0 (taking 
    into account the previously listed limitations) 

The RSS format is made up of three metadata sections (channel,
image, and textinput) and a list of items. Each individual metadata
section and each item is passed around as an "item dictionary",
which is a Python dictionary with (namespace, localname) tuples as
keys. The values of the dictionaries are always strings; they may
contain markup, which will be rendered into the RSS/XML when
serialized.

Individual items are found by using an "item identifier"; this is a
channel-unique, string identifier for any given item. Item
identifiers may be generated in a variety of ways, depending on the
requirements of the channel.

Certain types of channel metadata are automatically generated, and
will not be returned or honored when accessed. They includes the
"items", "image" and "textinput" children of the channel element.
 
  
TODO:
  - any markup (and the content inside) in item or metadata children 
    (e.g., HTML in a <description> will be silently ignored.
  - test suite
  - a function (XPath-based?) to detect a channel's type and return
    the appropriate class.
  - pay attention to <rss:items> when appropriate.
"""

__license__ = """
Copyright (c) 2004 Mark Nottingham <mnot@pobox.com>

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
"""

__version__ = "0.47"

import UserDict, sys, codecs, sha, types, signal
import xml.sax as sax
import xml.sax.saxutils as saxutils
import cPickle as pickle
import cStringIO as StringIO
try: # accommodate older versions of python.
	from xml.sax.sax2exts import make_parser
except ImportError:
	from xml.sax import make_parser

versionURI = 'http://www.mnot.net/python/RSS.py?version=%s' % __version__


class _NamespaceMap:
    """
    Prefix <-> Namespace map.
    
    Hold prefix->namespace mappings, and generate new prefixes when 
    necessary. Exposes prefix->URI map as attributes, URI->prefix
    through getPrefix(URI).
    """
    
    def __init__(self):        
        self._nsID = 0  # seed for namespace prefix generation
        self._prefixMap = {}
        self.rdf = 'http://www.w3.org/1999/02/22-rdf-syntax-ns#'
        self.rss10 = 'http://purl.org/rss/1.0/'
        self.rss09 = 'http://my.netscape.com/rdf/simple/0.9/'
        self.rss091 = 'http://purl.org/rss/1.0/modules/rss091/'
        self.dc = 'http://purl.org/dc/elements/1.1/'
        self.syn = 'http://purl.org/rss/modules/syndication/'
        self.content = 'http://purl.org/rss/1.0/modules/content/'
        self.admin = 'http://webns.net/mvcb/'
        self.ag = 'http://purl.org/rss/modules/aggregation/'
        self.annotate = 'http://purl.org/rss/1.0/modules/annotate/'
        self.cp = 'http://my.theinfo.org/changed/1.0/rss/'
        self.company = 'http://purl.org/rss/1.0/modules/company'
        self.event = 'http://purl.org/rss/1.0/modules/event/'
        self.slash = 'http://purl.org/rss/1.0/modules/slash/'
        self.html = 'http://www.w3.org/html4/'
        
    def __setattr__(self, attr, value):
        self.__dict__[attr] = value
        if attr[0] != '_':
            self._prefixMap[value] = attr

    def getPrefix(self, URI):
        """
        Get the prefix for a given URI; generate one if it
        doesn't exist.
        """
        try:
            if URI == self.rss10:
                return None  # special case
            return self._prefixMap[URI]
        except KeyError:
            o = []
            d = self._nsID  
            while 1:
                o.insert(0, d % 26)
                d = d / 26
                if not d: break
            candidate = "".join(map(lambda a: chr(a+97), o))
            self._nsID = self._nsID + 1
            if candidate in self._prefixMap.values():
                candidate = self.getPrefix(URI)
            setattr(self, candidate, URI)
            return candidate


ns = _NamespaceMap()

# possible namespaces for RSS docs (None included for 0.9x)
rssNamespaces = [ns.rss09, ns.rss10, None]

# major sections of a RSS file
rssSections = [ (ns.rss10, 'channel'), 
                (ns.rss10, 'image'), 
                (ns.rss10, 'textarea')
              ]

# RSS core element localnames
rssElements = ['rss', 'channel', 'image', 'textarea', 'item', 'items', 
               'title', 'link', 'description', 'url']

# RSS elements whose data is in an rdf:resource attribute
rdfResources = [    (ns.rss10, 'image'), 
                    (ns.rss10, 'textarea'), 
                    (ns.admin, 'errorReportsTo'), 
                    (ns.admin, 'generatorAgent'), 
                    (ns.annotate, 'reference'), 
                    (ns.cp, 'server')
               ]


class ChannelBase:
    """
    Base class for RSS Channels.
    
    A number of generic methods for accessing and setting channel
    data and metadata are exposed, for the benefit of subclasses.
    They may be used by applications as well, or the data structure
    of the subclass may be directly manipulated.
    """
    
    def __init__(self):
        self.encoding = 'utf-8'
        
    def listItems(self):
        """List the items in a channel, with a list of identifiers."""
        pass  # override me 
            
    def addItem(self, item, index=0):
        """Add an item to the channel. Expects an item dictionary."""
        pass  # override me

    def getItem(self, identifier):
        """Get the appropriate item dictionary for a given identifier."""
        pass  # override me

    def getMD(self, name):
        """
        Get the [name] metadata as an item dictionary, where type is
        a tuple (typically, in the ns:rss10 namespace, with a localname of 
        channel|image|textinput). MUST return an empty dictionary if the
        metadata isn't found.
        """ 
        pass  # override me
        
    def setMD(self, name, metadata):
        """
        Set the [name] metadata, where name is a tuple (typically, 
        it will be in the ns:rss10 namespace, and have a localname of
        channel|image|textinput), and metadata is an item dictionary.
        """
        pass  # override me
    
    def parse(self, url, timeout=30):
        """
        Fetch a channel representation from a URL and populate
        the channel.
        """
        dh = RSSParser(self)
        p = make_parser()
        p.setContentHandler(dh)
        p.setFeature(sax.handler.feature_namespaces, 1)
        signal.signal(signal.SIGALRM, self._timeout)
        signal.alarm(timeout)
        try:
            p.parse(str(url))  # URIs are ascii
        finally:
            signal.alarm(0)
        return dh

    def _timeout(self, **args):
        raise IOError, 'timeout'

    def parseFile(self, file):
        """Parse a file and populate the channel."""
        dh = RSSParser(self)
        p = make_parser()
        p.setContentHandler(dh)
        p.setFeature(sax.handler.feature_namespaces, 1)
        p.parseFile(file)
        return dh

    def __str__(self):
        return self.output(self.listItems())
    
    def output(self, items):
        """Return the items referred to by a list of identifiers."""
        assert type(items) is types.ListType, "items must be a list (%s)" % \
          type(items)
        out = StringIO.StringIO()
        o = _XMLGenerator(out, self.encoding, 'replace')
        channelMD = self.getMD((ns.rss10, "channel"))
        imageMD = self.getMD((ns.rss10, "image"))
        textinputMD = self.getMD((ns.rss10, "textinput"))
        channelMD[(ns.admin, 'generatorAgent')] = versionURI
        
        # gather namespaces, map prefixes
        namespaces = {ns.rdf: 1}
        namespaces.update(dict(
          channelMD.keys() + imageMD.keys() + textinputMD.keys()))
        [namespaces.update(dict(i.keys())) for i in map(self.getItem, items)]
        for namespace in namespaces.keys():
            o.startPrefixMapping(ns.getPrefix(namespace), namespace)
            
        # write the XML
        o.startDocument()
        o.startElementNS((ns.rdf, 'RDF'), None, {})
        o.ignorableWhitespace('\n')
        o.startElementNS(
          (ns.rss10, 'channel'), None, 
          {(ns.rdf, 'about'): channelMD[(ns.rss10, 'link')]})
        o.ignorableWhitespace('\n')
        
        # /channel
        for name, data in channelMD.items():
            if name in [(ns.rss10, 'items'), (ns.rss10, 'image'), 
              (ns.rss10, 'textinput')]: 
                continue
            o.ignorableWhitespace('  ')
            if name in rdfResources:
                o.startElementNS(name, None, {(ns.rdf, 'resource'): data})
            else:
                if "<" in data:
                    o.startElementNS(name, None, 
                      {(ns.rdf, "parseType"): "Literal"})
                else:
                    o.startElementNS(name, None, {})
                o.characters(data)
            o.endElementNS(name, None)
            o.ignorableWhitespace('\n')
            
        # /channel/items
        o.ignorableWhitespace('  ')
        o.startElementNS((ns.rss10, 'items'), None, {})
        o.startElementNS((ns.rdf, 'Seq'), None, {})
        o.ignorableWhitespace('\n')
        for id in items:
            o.ignorableWhitespace('   ')
            o.startElementNS((ns.rdf, 'li'), None, 
              {(ns.rdf, 'resource'): self.getItem(id).get((ns.rss10, 'link'), 
              _make_hash(self.getItem(id)))})
            o.endElementNS((ns.rdf, 'li'), None)
            o.ignorableWhitespace('\n')
        o.ignorableWhitespace('  ')
        o.endElementNS((ns.rdf, 'Seq'), None)
        o.endElementNS((ns.rss10, 'items'), None)
        o.ignorableWhitespace('\n')

        # /channel/image
        if imageMD.has_key((ns.rss10, 'url')):
            o.startElementNS((ns.rss10, 'image'), None,
              {(ns.rdf, 'about'): imageMD[(ns.rss10, 'url')]})
            o.endElementNS((ns.rss10, 'image'), None)
            o.ignorableWhitespace('\n')
            
        # /channel/textinput
        if textinputMD.has_key((ns.rss10, 'link')):
            o.startElementNS((ns.rss10, 'textinput'), None,
              {(ns.rdf, 'about'): textinputMD[(ns.rss10, 'link')]})
            o.endElementNS((ns.rss10, 'textinput'), None)
            o.ignorableWhitespace('\n')
        o.endElementNS((ns.rss10, 'channel'), None)
        o.ignorableWhitespace('\n')
        
        # /image
        if imageMD.has_key((ns.rss10, 'url')):
            o.startElementNS((ns.rss10, 'image'), None, 
              {(ns.rdf, 'about'): imageMD[(ns.rss10, 'url')]})
            for name, data in imageMD.items():
                o.ignorableWhitespace('  ')
                if name in rdfResources:
                    o.startElementNS(name, None, {(ns.rdf, 'resource'): data})
                else:
                    if "<" in data:
                        o.startElementNS(name, None, 
                          {(ns.rdf, "parseType"): "Literal"})
                    else:
                        o.startElementNS(name, None, {})
                    o.characters(data)
                o.endElementNS(name, None)
                o.ignorableWhitespace('\n')
            o.endElementNS((ns.rss10, 'image'), None)
            o.ignorableWhitespace('\n')
            
        # /textinput
        if textinputMD.has_key((ns.rss10, 'link')):
            o.startElementNS((ns.rss10, 'textinput'), None, 
              {(ns.rdf, 'about'): textinputMD[(ns.rss10, 'link')]})
            for name, data in textinputMD.items():
                o.ignorableWhitespace('  ')
                if name in rdfResources:
                    o.startElementNS(name, None, {(ns.rdf, 'resource'): data})
                else:
                    if "<" in data:
                        o.startElementNS(name, None, 
                          {(ns.rdf, "parseType"): "Literal"})
                    else:
                        o.startElementNS(name, None, {})
                    o.characters(data)
                o.endElementNS(name, None)
                o.ignorableWhitespace('\n')
            o.endElementNS((ns.rss10, 'textinput'), None)
            o.ignorableWhitespace('\n')
            
        # /item
        for id in items:
            item = self.getItem(id)
            o.startElementNS(
              (ns.rss10, 'item'), None, {(ns.rdf, 'about'):
              item.get((ns.rss10, 'link'), _make_hash(item))})
            o.ignorableWhitespace('\n')
            for name, data in item.items():
                o.ignorableWhitespace('  ')
                if name in rdfResources:
                    o.startElementNS(name, None, {(ns.rdf, 'resource'): data})
                else:
                    if "<" in data:
                        o.startElementNS(name, None, 
                          {(ns.rdf, "parseType"): "Literal"})
                    else:
                        o.startElementNS(name, None, {})
                    o.characters(data)
                o.endElementNS(name, None)
                o.ignorableWhitespace('\n')
            o.endElementNS((ns.rss10, 'item'), None)
            o.ignorableWhitespace('\n')
        o.endElementNS((ns.rdf, 'RDF'), None)
        o.endDocument()
        out.seek(0)
        return out.read()



class TrackingChannel(ChannelBase, UserDict.UserDict):
    """
    RSS Channel modeled as a URI-per-entry dictionary.
    
    Item identifiers are (uri, index) tuples, where uri is
    the rdf:about or rss:link URI, and index indicates the
    position in a list of a number of times that URI has 
    appeared in the channel.
    
    This allows "tracking" channels that track the state of
    a group of resources, such as stock tickers, file state
    changes, etc.

    For example:
    
    {
        (ns.rss10, "channel"):  {
                     (ns.rss10, "title"): "the channel",
                     (ns.rss10, "description"): "whatever",
        },
        (ns.rss10, "items"): 
           ["http://example.com/foo", "htp://example.com/bar", ... ],
        "http://example.com/foo" [
              {
                (ns.rss10, "title"): "item 1",
                (ns.rss10, "link"): "http://example.com/",
                (ns.rss10, "description"): "foo",
              },
              {
                (ns.rss10, "title"): "item 1 revised",
                (ns.rss10, "link"): "http://example.com/",
                (ns.rss10, "description"): "foo revisited",
              },
        ]
                
        "http://example.com/bar" [
                ...
        ]
    }
    
    """
    
    def __init__(self, data={}):
        ChannelBase.__init__(self)
        UserDict.UserDict.__init__(self, data)
        self.data[(ns.rss10, 'items')] = []

    def listItems(self):
        return self[(ns.rss10, 'items')]
        
    def addItem(self, item, index=0):
    	if index == -1: index = len(self.data[(ns.rss10, 'items')])
        uri = item.get((ns.rss10, "link"), _make_hash(item)) # shoudn't happen
        if not self.data.has_key(uri):
            self.data[uri] = [item]
        else:
            self.data[uri].append(item)
        self.data[(ns.rss10, 'items')].insert(index, (uri, len(self.data[uri])))
        
    def getItem(self, identifier):
        (uri, index) = identifier
        try:
            return self.data[uri][index-1]
        except (KeyError, IndexError):
            return {}
        
    def getMD(self, name):
        return self.data.get(name, {})
        
    def setMD(self, name, metadata):
        self.data[name] = metadata

            
    
class CollectionChannel(ChannelBase, UserDict.UserDict):
    """
    RSS Channel modeled as an item-per-entry dictionary.
    
    Each Item is hashed to create a unique entry in the 
    dictionary, no matter how many times a particular 
    URI is in the channel. 
    
    This allows "collection" channels, which are typically
    used for news updates, etc.
    
    For example:
    
    {
        (ns.rss10, "channel"):  {
                     (ns.rss10, "title"): "the channel",
                     (ns.rss10, "description"): "whatever",
                        },
        (ns.rss10, "items"): ["ID1", "ID2", ... ],
        "ID1" {
                (ns.rss10, "title"): "item 1",
                (ns.rss10, "link"): "http://example.com/",
                (ns.rss10, "description"): "foo",
              },
        "ID2" {
                ...
              }
    }
    
    Note that:
    - items are keyed by a hash-data URI; metadata is keyed
       by a (namespace, localname) tuple. 
    - (ns.rss10, items) is a property; it cannot be 
      manipulated without manipulating the corresponding 
      (sub-)items (delete, add)
    - likewise, all item's are properties; adding, deleting,
      appending an item modifies (ns.rss10, items) 
      correspondingly    
    """
    
    def __init__(self, data={}):
        ChannelBase.__init__(self)
        UserDict.UserDict.__init__(self, data)
        self.data[(ns.rss10, 'items')] = []
           
    def listItems(self):
        return self.data[(ns.rss10, 'items')]

    def addItem(self, item, index=0):
        """append an item dictionary to the channel"""
    	if index == -1: index = len(self.data[(ns.rss10, 'items')])		
        ID = _make_hash(item)
        self.data[ID] = item
        self.data[(ns.rss10, 'items')].insert(index, ID)

    def getItem(self, identifier):
        return self.data.get(identifier, {})
        
    def getMD(self, name):
        return self.data.get(name, {})
        
    def setMD(self, name, metadata):
        self.data[name] = metadata


class _XMLGenerator(saxutils.XMLGenerator):
    """
    Modified XMLGenerator.

    Allows modification of encoding error handling, and tries to
    encode problematic characters as Latin-1 to work around some
    implementations.
    """
    
    def __init__(self, out=None, encoding='iso-8859-1', errors='strict'):
        saxutils.XMLGenerator.__init__(self, out=out, encoding=encoding)
        if out is None:
            out = sys.stdout
        self._out = codecs.lookup(encoding)[3](out, errors)

    def characters(self, content):
        try:
            self._out.write(sax.saxutils.escape(content))
        except UnicodeError:  # hack for broken content
            self._out.write(sax.saxutils.escape(unicode(content, 'Latin-1')))
        
        
class RSSParser(sax.handler.ContentHandler):
    """
    Multi-format RSS/XML Parser.
    
    Parse XML into RSS Channel objects. May optionally be passed a 
    Channel() instance to append to.
    
    Formats understood include:
      - RSS 0.9
      - RSS 0.91
      - RSS 0.92
      - RSS 1.0 (EXCEPT "rich content" modules)
      
    "Core" RSS elements are normalized to the RSS1.0 namespace.
    """
    
    def __init__(self, channel, encoding='utf-8'):
        sax.handler.ContentHandler.__init__(self)
        self.channel = channel
        self.encoding = encoding
        self._context = []
        self._tmp_item = {}
        self._tmp_md = {    (ns.rss10, "channel"): {},
                            (ns.rss10, "image"): {},
                            (ns.rss10, "textinput"): {},
                       }
        self._tmp_buf = ''
        self.version = None

    def startElementNS(self, name, qname, attrs):
        if name[1] is 'rss':  # sniff version
            if name[0] is None:
                self.version = attrs.get('version', None)
            else: 
                self.version = name[0]
        # normalize the rss namespace
        if name[0] in rssNamespaces and name[1] in rssElements:  
            name = (ns.rss10, name[1])
        elif name[0] is None:
            name = (ns.rss091, name[1])
        self._context.append(name)
        if name == (ns.rss10, 'item'):
            self._tmp_item = {}
            self._tmp_buf = ''
        elif len(self._context) > 1 and \
          self._context[-2] == (ns.rss10, 'item') and \
          name in rdfResources:
            self._tmp_item[name] = attrs[(ns.rdf, 'resource')]
		
    
    def endElementNS(self, name, qname):
        # normalize the rss namespace
        if name[0] in rssNamespaces and name[1] in rssElements:  
            name = (ns.rss10, name[1])
        elif name[0] is None:
            name = (ns.rss091, name[1])
        if (ns.rss10, 'item') in self._context:
            if self._context[-1] == (ns.rss10, 'item'):  # end of an item
                self.channel.addItem(self._tmp_item, len(self.channel))
                self._tmp_item = {}
            elif self._context[-2] == (ns.rss10, 'item'):  # an item's child
                if name not in rdfResources:
                    self._tmp_item[name] = self._tmp_buf.strip()
            else:  # an item's grandchild
                pass  ###
        elif len(self._context) > 2 and self._context[-2] in rssSections: 
            # metadata
            self._tmp_md[self._context[-2]][name] = self._tmp_buf.strip()
        self._tmp_buf = ''
        self._context.pop()
        
    def endDocument(self):
        for name, metadata in self._tmp_md.items():
            self.channel.setMD(name, metadata)

    def characters(self, content):
        self._tmp_buf = self._tmp_buf + content.encode(self.encoding)


def _make_hash(data):
    return "hash-data:SHA:" + sha.new(pickle.dumps(data)).hexdigest()[:20]


if __name__ == "__main__":
    # a simple test
    c = TrackingChannel()
    c.parse(sys.argv[1])
    print c
