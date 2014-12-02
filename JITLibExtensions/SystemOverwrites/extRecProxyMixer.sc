/*
   RecordProxyMixer was in JITLib until 3.6.6, and moved to JITLibExtensions for 3.7.0.
   To avoid clashes between duplicate classes across versions,
   RecordProxyMixer in JITLibExtensions is now renamed RecordProxyMixer2,
   and ProxyMixer button uses that class name now.
*/

+ ProxyMixer {

	makeTopLine {

		PopUpMenu(arZone, Rect(10, 10, 110, skin.headHeight))
				.items_([\existingProxies, \activeProxies, \playingProxies])
				.action_({ arg view; selectMethod = view.items[view.value] })
				.font_(font);

		Button(arZone, Rect(10, 10, 50, skin.headHeight))
				.states_(
					[["reduce", skin.fontcolor, skin.offColor]]				)
				.action_({ object !? { object.reduce } }).font_(font);
		Button(arZone, Rect(10, 10, 30, skin.headHeight))
				.states_(
					[["doc", skin.fontcolor, skin.offColor]]				)
				.action_({ object !? { object.document } }).font_(font);
		Button(arZone, Rect(10, 10, 45, skin.headHeight))
				.states_(
					[["docSel", skin.fontcolor, skin.offColor]]				)
				.action_({
					object !? { object.document(this.selectedKeys) }
				}).font_(font);

		Button(arZone, Rect(10, 10, 60, skin.headHeight))
				.font_(font)
				.states_([
						["openKr", skin.fontcolor, skin.offColor],
						["openEdit", skin.fontcolor, skin.offColor],
						["closeEdit", skin.fontcolor, skin.offColor]
					])
				.value_(1)
				.action_({ |b| this.switchSize(b.value) });

		Button(arZone, Rect(10, 10, 50, skin.headHeight))
				.font_(font)
				.states_(
					[	["Record", Color.red, skin.offColor]					])
				.action_({ RecordProxyMixer2(this, parent.bounds.resizeTo(472, 100)) });

	}
}