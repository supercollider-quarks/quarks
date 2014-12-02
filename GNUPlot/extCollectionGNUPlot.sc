
+ ArrayedCollection {
	gnuplot { GNUPlot.plot( this ) }
	gnuplotd { |ns=1,label=""| GNUPlot.plotd( this, ns, label ) }
	gnuhistod { |ns=1,label="",verb=true| GNUPlot.plotdHisto( this, ns, label, verb ) }
}

+ Env {
	gnuplot { GNUPlot.plotenv( this ) }
	gnuplotd { |ns=1,label=""| GNUPlot.plotd( this, ns, label ) }
}

+ AbstractFunction{
	
	gnuplot{ arg n=500, from = 0.0, to = 1.0;
		var array = Array.interpolation(n, from, to);
		var res = array.collect { |x| this.value(x) };
	
		GNUPlot.plot(res);
	}
	
	surf3{ |rect,grid = 30,hidden3d = true, pm3d = true,extracmds|
	
		var xyz, gnuplot, grain, specX, specY;
		
		rect = rect ? Rect(0,0,1,1);
		specX = [rect.left,rect.left+rect.width,\linear].asSpec;
		specY = [rect.top,rect.top+rect.width,\linear].asSpec;
			
		gnuplot = GNUPlot.new;
		grain = 1/grid;  
		xyz =  (0,grain .. 1).collect{|x|  (0,grain .. 1).collect{|y| 
			 [specX.map(x),specY.map(y),this.value(specX.map(x),specY.map(y))]
		 } };
		gnuplot.surf3(xyz, "a Function", hidden3d, pm3d,extracmds:extracmds);
		
		
	}	

}