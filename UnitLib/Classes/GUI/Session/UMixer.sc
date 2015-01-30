/*
    Unit Library
    The Game Of Life Foundation. http://gameoflife.nl
    Copyright 2006-2011 Miguel Negrao, Wouter Snoei.

    GameOfLife Unit Library: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    GameOfLife Unit Library is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with GameOfLife Unit Library.  If not, see <http://www.gnu.org/licenses/>.
*/

UMixer {

     var <mainComposite, <mixerView, <scoreListView, font, <parent, <bounds;
     var <>scoreList;
     var <scoreController, <unitControllers;
     var <soloedTracks;

     *new{ |score, parent, bounds| ^super.new.init(score, parent,bounds) }

     init { |score, inParent, inBounds|

        scoreList = [score];
        soloedTracks  = [];
        font = Font( Font.defaultSansFace, 11 );
        parent = inParent ? Window("UMixer",Rect(100,1000,800,372)).front;
        if(parent.respondsTo(\onClose_)){ parent.onClose_({this.remove}) };
        bounds = inBounds ? Rect(0,0,800,372);

        this.addCurrentScoreControllers;
        unitControllers = List.new;
        mainComposite = ScrollView(parent,bounds).resize_(5)
            .background_( Color.grey(0.8) );
        this.makeMixerView

     }

     addCurrentScoreControllers {

         if( scoreController.notNil ) {
	        scoreController.remove;
	    };
        scoreController = SimpleController( scoreList.last );

		scoreController.put(\numEventsChanged, {
		    this.update;
		});
	}

	remove {
        (unitControllers++[scoreController]).do(_.remove)
    }

	update {
	    this.remake;
	}

    currentScore {
        ^scoreList.last
    }

    isInnerScore {
        ^(scoreList.size > 1)
    }

    remake {

        if(scoreListView.notNil){
            scoreListView.remove;
            scoreListView = nil
        };
        if(scoreList.size > 1) {
            this.makeScoreListView;
        };
        this.makeMixerView;
    }

    addtoScoreList{ |score|
        scoreList = scoreList.add(score);
        this.addCurrentScoreControllers;
        this.remake;
    }

    goToHigherScore{ |i|
        scoreList = scoreList[..i];
        this.addCurrentScoreControllers;
        fork{ { this.remake; }.defer }
    }

    makeScoreListView{
        var listSize = scoreList.size;
        scoreListView = CompositeView(mainComposite,Rect(0,0,4 + ((60+4)*(listSize-1)) + 4,24));
        scoreListView.addFlowLayout;
        scoreList[..(listSize-2)].do{ |score,i|
            SmoothButton(scoreListView,60@16)
                .states_([[(i+1).asString++": "++score.name, Color.black, Color.clear]])
                .font_( font )
                .border_(1).background_(Color.grey(0.8))
                .radius_(5)
                .canFocus_(false)
                .action_({
                    this.goToHigherScore(i);
                })
        };
        SmoothButton(scoreListView,16@16)
            .states_([[\up, Color.black, Color.clear]])
            .font_( font )
            .border_(1).background_(Color.grey(0.8))
            .radius_(5)
            .canFocus_(false)
            .action_({
                UMixer( this.currentScore )
            })

    }

     makeMixerView{
        var spec, maxTrack,count, cview,w,level,bounds, width,top,main,scroll, evs;
		var score = this.currentScore;
		var events = score.events;
        var viewBounds;
        unitControllers.do(_.remove);
		evs = events.select({ |x| x.canFreeSynth && x.hideInGUI.not });
        maxTrack = evs.collect{ |event| event.track }.maxItem + 1;
		count = 0;
		spec = [-90,12,\db].asSpec;

        if(mixerView.notNil) {
            mixerView.visible_(false);
            mixerView.focus(false);
            mixerView.remove;
        };

        mixerView = CompositeView(mainComposite, Rect(0,24,44*evs.size+4,338));
        mixerView.addFlowLayout;

        maxTrack.do{ |j|
			evs.do{ |event,i|
				var cview,faders, eventsFromFolder, ctl, sl, bt;
				if(event.track == j){
				if(event.isFolder.not){
					cview = CompositeView(mixerView,40@330);
					cview.decorator = FlowLayout(cview.bounds);
					if( [ Color ].includes( event.getTypeColor.class ) ) {
						cview.background_( event.getTypeColor.copy.alpha_(0.5) );
					} {
						cview.background_(Color(0.58208955223881, 0.70149253731343, 0.83582089552239, 1.0));
					}; 
					cview.decorator.shift(0,24);
					sl = EZSmoothSlider.new(cview, Rect(0,0,32,240), events.indexOf(event), spec, layout:\vert)
						.value_(event.getGain)
						.action_({ |v|
								event.setGain(v.value);
						});
					bt = SmoothButton(cview,32@20)
					    .states_(
					        [[ "s", Color.black, Color.clear ],
					        [  "s", Color.yellow, Color.clear ]] )
                        .canFocus_(false)
                        .border_(1).background_(Color.grey(0.8))
                        .value_(score.soloed.includes(event).binaryValue)
                        .action_({ |v|
                            score.solo(event, v.value.booleanValue)
                        });
                    bt = SmoothButton(cview,32@20)
                        .states_(
                            [[ "m", Color.black, Color.clear ],
                            [  "m", Color.red, Color.clear ]] )
                        .canFocus_(false)
                        .border_(1).background_(Color.grey(0.8))
                        .value_(score.softMuted.includes(event).binaryValue)
                        .action_({ |v|
                            score.softMute(event, v.value.booleanValue)
                        });
                    ctl = SimpleController(event)
                        .put(\gain,{ sl.value = event.getGain; });
                        //.put( \muted, { bt.value = event.muted.binaryValue } );
                    unitControllers.add(ctl);

				}{
						eventsFromFolder = event.allEvents.select(_.canFreeSynth)
						.collect{ |event| (\event: event,\oldLevel: event.getGain) };
					cview = CompositeView(mixerView,40@330);
					cview.decorator = FlowLayout(cview.bounds);
					if( [ Color ].includes( event.getTypeColor.class ) ) {
						cview.background_( event.getTypeColor.copy.alpha_(0.5) );
					} {
						cview.background_(Color(0.28208955223881, 0.50149253731343, 0.23582089552239, 1.0));
					}; 
					SmoothButton(cview,32@20).states_([["open"]])
						.radius_(3)
						.action_({
							{ this.addtoScoreList(event) }.defer(0.1)
						});
					EZSmoothSlider.new(cview, Rect(0,0,32,240), events.indexOf(event), spec, layout:\vert)
						.value_(0)
						.action_({ |v|
							eventsFromFolder.do{ |dict|
								dict[\event].setGain(dict[\oldLevel]+v.value);
							};
						});
				    bt = SmoothButton(cview,32@20)
                        .states_(
                            [[ "s", Color.black, Color.clear ],
                            [  "s", Color.yellow, Color.clear ]] )
                        .canFocus_(false)
                        .border_(1).background_(Color.grey(0.8))
                        .value_(score.soloed.includes(event).binaryValue)
                        .action_({ |v|
                            score.solo(event, v.value.booleanValue)
                        });
                    bt = SmoothButton(cview,32@20)
                        .states_(
                            [[ "m", Color.black, Color.clear ],
                            [  "m", Color.red, Color.clear ]] )
                        .canFocus_(false)
                        .border_(1).background_(Color.grey(0.8))
                        .value_(score.softMuted.includes(event).binaryValue)
                        .action_({ |v|
                            score.softMute(event, v.value.booleanValue)
                        });
				};

				}
			}
		}


    }

     refresh{  }
}