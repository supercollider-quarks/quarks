/******* by jostM http://www.glyph.de *******/
/******* Part of TabbedView2_QT Quark *******/




TabbedViewView :  QView{
	var <>onBeginClose;

  remove {
	onBeginClose.value(this);
	^super.remove;
  }
}
