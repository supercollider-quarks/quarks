

ServerTreeGui : ObjectGui { // model is Server

	*makeWindow { arg server;
		var gui;
		server = server ? Server.default;
		gui = super.new(server);
		server.getQueryTree({ arg rootData;
			gui.gui(nil,nil,rootData)
		})
	}
	gui { arg layout,bounds,root;

		var server;
		server = model;

		server.getQueryTree({ arg root;

			var renderChild,indent = 0,w,f;
			var nodeWatcher,y=0,renderBox, layout;

			nodeWatcher = NodeWatcher.newFrom(server);

			if(layout.isNil,{
				w = Window("Server Node Tree",Rect(0,0,1220,820),scroll:true);
				f = CompositeView(w,Rect(0,0,1210,10000));
				w.front;
			},{
				f = CompositeView(layout,bounds ?? {layout.bounds})
			});

			renderBox = { arg func;
				var box;
				box = f.flow(func,Rect(indent * 5,y,900,600));
				box.resizeToFit;
				box.background = Color.yellow(alpha:0.1);
				y = y + box.bounds.height + 4;
			};

			renderChild = { arg data;
				data.use({
					var node;

					if(~nodeType == Group,{
						node = nodeWatcher.nodes.at(~id) ?? {Group.basicNew(server,~id)};

						renderBox.value({ arg l;
							SimpleLabel(l,("Group(" ++ ~id ++ ")")).background_(ServerLogGui.colorForNodeID(~id) ).bold;
							//ToggleButton(l,"pause",{ arg way; node.run(way) },init:true);
							SimpleButton(l,"free",{ node.free });
							if(\Annotations.asClass.notNil, {
								Annotations.guiFindNode(~id, l);
							});
						});

						indent = indent + 8;
						~children.do { arg child;
							renderChild.value(child);
						};
						indent = indent - 8;

					},{
						node = nodeWatcher.nodes.at(~id) ?? {Synth.basicNew(~defName,server,~id)};
						renderBox.value({ arg l;
							SimpleLabel(l,("Synth(" ++ ~id ++ ")")).background_(ServerLogGui.colorForNodeID(~id) ).bold;
							DefNameLabel(~defName,server,l);
							SimpleButton(l,"trace",{
								node.trace;
							});
							//ToggleButton(l,"pause",{ arg way; node.run(way) },init:true);
							SimpleButton(l,"free",{ node.free });
							ActionButton(l,"log...",{
								ServerLog.guiMsgsForSynth(node);
							});
							l.startRow;
							if(\Annotations.asClass.notNil, {
								Annotations.guiFindNode(~id, l);
							});
							~controls.keysValuesDo { arg k,v;
								l.startRow;
								ArgName(k,l,100);
								SimpleLabel(l,v,100);
							};
						})
					});
				});
			};
			renderChild.value(root);
		})
	}
}


BussesTool {

	var <>server;

	*new { arg server;
		^super.new.server_(server ? Server.default).gui
	}
	gui { arg layout,bounds;
		var resize = false,w;
		if(layout.isNil,{
			w = Window("Busses",bounds ?? {Rect(0,0,1000,1000)},scroll: true).front;
			layout = FlowView(w);
			resize=true
		});
		SimpleLabel( layout, "Audio Busses",layout.bounds.width);
		if(\Patch.asClass.notNil,{
			server.audioBusAllocator.blocks.do({ |b|
				var listen,bus;
				listen = Patch({ In.ar( b.start, b.size ) });
				layout.startRow;
				ToggleButton( layout,"listen",{
					listen.play
				},{
					listen.stop
				});
				SimpleLabel( layout, b.start.asString + "(" ++ b.size.asString ++ ")",100 );

				if(\Annotations.asClass.notNil, {
					Annotations.guiFindBus(b.start,\audio,layout);
				});

				if(\BusPool.asClass.notNil,{
					bus = BusPool.findBus(server,b.start);
					if(bus.notNil,{
						layout.flow({ |f|
							var ann;
							ann = BusPool.getAnnotations(bus);

							if(ann.notNil,{
								ann.keysValuesDo({ |client,name|
									f.startRow;
									InspButton(client,f);
									SimpleLabel(f,":"++name);
								});
							});
						})
					});
				});
				SimpleButton(layout,"log...",{
					ServerLog.guiMsgsForBus(b.start,b.size)
				});
				SimpleButton(layout,"free",{
					Bus(\audio,b.start,b.size).free
				});
			});
		},{
			"BussesTool requires cruciallib for now".inform;
		});
	}
}


