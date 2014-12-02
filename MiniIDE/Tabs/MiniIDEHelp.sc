/*
MiniIDEHelp
by Jonathan Siemasko
Contact: schemawound@yahoo.com
Homepage: http://schemawound.com/
*/

MiniIDEHelp : MiniIDETab{
	var <webView;
	classvar <tabLabel = \Help;

	*new{|bounds|
		^super
			.new(bounds)
			.init();
	}

	init{
		view = window.view;
		webView = WebView()
		    .bounds_(bounds)
		    .enterInterpretsSelection_(true)
		    .url_("http://doc.sccode.org/Search.html");

		layout = VLayout(webView);
	}
}