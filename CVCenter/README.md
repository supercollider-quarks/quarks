CVCenter is a container of CVs as well as it provides a graphical control-interface that lets you connect synth-controls or controls within patterns to hardware midi-sliders or OSC-controllers. A CV models a value constrained by a ControlSpec. Within CVCenter they get represented within CVWidgets that can hold one or more CVs. Each of them can be connected to an unlimited number GUI-elements or/and control parameters in running Synths or set values in Patterns. As CV inherits from Stream any instance can be used as any other Pattern (Pseq, Prand, Pwhite etcetc.).

For more info check out the CVCenter- resp. CVWidget-helpfile.

Installation
------------
1. get SuperCollider from http://sourceforge.net/projects/supercollider/files/ (or install SuperCollider from source. For more instructions see: http://supercollider.github.io/development/building.html or cunsult the specific READMEs that come with the program).
2. install the required extensions via the Quarks-mechanism:
	- Conductor
	- TabbedView
	- wslib (optional)
	- cruciallib (optional)  
	see the Quarks-helpfile for more information on how to do this.
2. after installing SuperCollider and the required extensions put all content of CVCenter in your user-app-support directory. Execute the following line SuperCollider to see where that is:

		Platform.userExtensionDir

Under OSX this will resolve to:

	~/Library/Application Support/SuperCollider/Extensions

Under Linux this will resolve to:

	~/.local/share/SuperCollider/Extensions

__Note:__ A newer version of CVCenter already exists, including a MultiSlider widget, editable shortcuts and lots of other improvements. However, this version is hosted on Github only (and will probably stay there for a little longer...): [https://github.com/nuss/CVCenter]. If you're interested in trying out that version I recommend to uninstall the quark (i.e. remove the symbolic links in your <user-app-support-dir>/quarks) and install CVCenter via git to user-app-support-dir:

	$ cd /path/to/your/user-app-support-dir
	$ git clone https://github.com/nuss/CVCenter.git

Choosing this installation method will always allow you to switch between the old and new version easily. All fixes will first go into the github repository and then being ported to the quarks repository on sourceforge.

Using Windows the mechanism should apply as well. However, the author of this document currently doesn't know what the result of the query will be...

Note: if you're on Linux you will need to have installed SuperCollider >= 3.5 as CVCenter depends on QtGUI. Under MacOSX CVCenter *should* be compatible with SC >= 3.4 resp. QtGUI as well as Cocoa. 
Under Windows it's recommended to use the latest version of SuperCollider as it comes with full Qt-support and the new SC-IDE. Get it here: http://sourceforge.net/projects/supercollider/files/Windows/3.6/SuperCollider-3.6.6-win32.exe/download
