// drawInputName is a boolean flag, it selects if input names are
// drawn in the box representing the UGen.  It is false by default.

DotViewer.drawInputName = true;

// Values for OSX.  The "open" command uses the same table as the
// finder uses to resolve which application to open a file with.  If
// the OSX GraphViz package is installed and the file associations set
// correctly this will open a drawing of the UGen graph.  See:
// www.pixelglow.com/graphviz/ for GraphViz OSX packages.

(
DotViewer.directory = "/tmp";
DotViewer.viewer = "open";
)

// Values for Linux. The "dotty" command is provided with the AT&T
// graphviz release.

(
DotViewer.directory = "/tmp";
DotViewer.viewer = "dotty";
)

// Inspect

[DotViewer.directory, DotViewer.viewer];
