ImpactGui { 			// assumes just one joystick! maybe Impact should have instances for every joystick?
	classvar ascImage; 
	var <w, <txview, <blox, <pxEdit, <watcher; 
	
	*new { ^super.new.init; }
	
	init {
		w = SCWindow("ImpactGui", Rect(100, 100, 520, 540)).front;
		w.view.decorator_(FlowLayout(w.view.bounds));
		txview = SCTextView(w,Rect(0,0, 510, 330))
			.hasVerticalScroller_(true)
			.autohidesScrollers_(true)
			.string_("")
			.font_(Font("Courier", 6));
		txview.string_(ascImage);
		
		blox = ["W", "S", "E", "N"].collect { |ch, i| 
			w.view.decorator.nextLine;
			(1..4).collect {Ê|j| 
					SCButton(w, Rect(0,0,80, 18))
						.states_([
							[ "px_" ++ ch ++"_" ++ j, Color.black, Color(0,0,0,0) ], 
							[ "px_" ++ ch ++"_" ++ j, Color.black, Color.green ] 
						])
						.action_({ |btn| if (btn.value > 0, { 
								blox.do { |b| if (b !== btn, {Êb.value_(0) }) }
							});
						});
			};
		}.flop.flat;
		w.view.decorator.nextLine;
	//	pxEdit = NodeProxyEditor(win: w);
		
		watcher = Watcher("impactGui", { this.updateBlox }, 0.2, { w.isClosed });
	}

	setPX {Ê|index=0| blox[index].value_(1).doAction }
	
	updateBlox { 
	//	"ipGui".postln;
		{ this.setPX((Impact.q.padOffsets.choose ? [0]).sum) }.defer
	}
	
	
	*initClass { ascImage = 
"                      $B$@@8    $B$@@                                             $B$@@8    $B$@@                       
                     B$B00B$  G     gG                                           0000BBB00B$00$B$@0                     
                    B$0g880gggG       gG                  @@@@@$              G8            G0$BBBB$                    
                  G80gggg   ^            GG             8g@@@@@@@@          GGg  ---               GBBg                 
                88g8 -                  ^ g           8ggBBB0000000g0ggg00008     ^--        CG8G      $                
              88 ^  ^  CCC                 8                               G       ^--                G0$               
            CG88                -C           8     : : :  :     :                           -C@$$@@-      g             
           G88-                   ^        .     ^  $ B  @ @    @                ^  -  - ^ 0B$$ $$$         g           
          G8^      .BB8G08 0@                      ^G  ^ $  ^  -^        ^ ^  .^  -        B 0 C0$$C -       8          
        G8G      $$@@80B088@@0$@       ^     -                      .                      ^gB  0$C  ---      8         
       GGC     B@@ 0$@8g880@ @$@@$      .                       .   .     ^     ^ ^         .        -^ $$ ^   @        
      GGG     @@@@@ @$0ggG0 @@@@B8$               C                     .  ^   C . 0$BGB$$          ^BB$g $$B   8       
     8G8     g@$0$$$ $Bg$8 GG88G00$$       -                         C             Gg0 BBBB        ^ G00  $B$   $       
    Gg8g     $@@$$$$$$@@B@B000000$$$    ^      800000              $BB00B          $CG888B8         ^$8C880$B   B       
    8888     $@@@$0 G0$$$$$8 $$B0$@$    ^C^     00g88^    -  ^       ^             .8@0B$-      .       0g  --  0       
    Gg8g      @@$8 G8@@$$$@@$ 8$@@$                                       ^ CC C              $$$$$          ^-0B8      
    88G0g      0g $$@@@@$@$@@@ G@@     ^         .        B0B00                             $g0g88$$^.^.      8BB0      
    8g8008      8@@@@@@@@@@@@@@@8        @@@@@@$   C      ggg0B^     .   $@@@@@@@@          BC8   BB ^ .    ^0BB$0C     
   888g0008        $@@@@@@@@@@        $@@@@@@@@@@@         0BB         $@@@@@@@@@@@@        ^0G-  $0. . .^ CB00B$B8     
   G88g000ggC                        @@@@@BB@@@@@@@-  C               @@@@$@@$@@@@@@@^ .         ^      ^ 08g00B$B0     
   Gg80B000g8g .                    @@@@8C8@0$@@@@@$^                B@@@$G 0@$@@@@@@$-         ^     G8  G88g0BBB0     
   8g80B000gg8G g8            CG   8@@@@gG8  B0$@@@@       ^         $@@@@08   G$@@@@$-.^g  C CCCC        G88g0B$@B     
   gg80B000gg88G C     CC    C 8   G@@@@@$0   0$@@@$                 0@@@@@$$ Cg$@@@@$^  gG   CC      CC   G8g00B$$     
  C88g0B000gg8G   C        C   0C   $@@@@@@@@@@@@@@$   G              B@@@@@@@@@@@@@@   g88   CCC     CC   88g00BBB     
  C88gBBB0g0g88   CC     C    88B    B@@@@@@@B$@@@8^              G     @@@@@@@@@@@ ^   Bgg8    C C  C    G88gg00$B0    
   880B00000g88G   C CC C   G88g0B     8@@@@@@@@     80 C8GG8888GC 08      8@@$g     ^g$00g8G     C CCC    G8ggg0BBB    
  G880BB000gg88GG     CC   GG88gB$$g               Gg088g88g000ggg8Gg88             g$$$0$g88G    CC  CC  GG888g0BBB    
  8gg0BBB000gg88G        GG88880$$$$$08B       ^ 888gg0$BB@$@@@@@@@00gg88GG     8880$$$$B$0g8GG    CCCCC  GG88g800BB    
  G80BB00000g88GG        GG888gB@@@@$B00gg8g8888888gg$$    C  C g@@$@Bgg8888888888g0$$@@@BBg888G           G888g0B$BB   
  g80BBBB00g888G        GG888g0B@@@@$B00gg8888888g$@$G g@@0       @@@@@@$g8888888g0B$  0@@00g888G          GG88gg0B$B   
  g80BBBB00g88         GGG888g0$  0@@@@@$B0B$@$$@@$    G@@$      G GB@@@@$$$$$$$$$8     0@$008888GG        G888ggBB$BB  
  gg0BB0B0gg88G        G8888g0$G    80@@@@$$$$B8        80g            Gg0B00g8G        G0@$0g8888GGG      G888g00B$$B8 
  ggBBBB00gg8GG       G8888g00g                                                           B@$0g888GGGGG     G8gg00B$$BB 
  800BBB00g888G  G g G88888g0B                                                             0@$gg8888GGGG   G8888g0BB$$$ 
 880BBBB00g888GG G8GG88888ggB                                                               g@$g88888GG   GGG888g00B$$@ 
 G80BBBB00gg88GG8GGG88888ggBG                                                                0@$g88888GGGGGGG88gg00B$$$$
 800BB0000g888G888888888ggBG                                                                  0@Bg88888GGGGGG888gg0BB$BB
 g00BBBB0gg88888888888gggB8                                                                      8$088888888G8G8888g00B$
 80BBB000gg888888888888g0                                                                         GBgg8888G8G8G888ggg0B$
 800BB000gg8888888888g0                                                                            0gg88888888888ggg0B00
  80BB000gg88888888g80                                                                              0Bg888888G888g00000 
  G8B0B00gg8888g8888                                                                                 B0gg88888g8ggg0G   
    g0000B00000ggg                                                                                     0$$0gg00000 G    
      B00000000                                                                                            G8 gGG       
"	}
}