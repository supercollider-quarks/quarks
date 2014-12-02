(
w = Window().front;
w.layout = VLayout().spacing_(2)
);

x = 3.collect { JITView(nil, w).label_(\otto) };
x.do(_.number);

3.do { EZSlider(w, nil, \test) };
w.view.removeAll;

// not working yet, Layout does not accept
// composite guis like EZSlider:
(
w = Window(bounds:Rect(200,200,200,200));
w.layout_(VLayout(
	EZSlider(w, nil, \test, \freq),
	TextField(), TextField(),
        HLayout( Button(), Button(), Button() )
    )
);
w.front;
)
