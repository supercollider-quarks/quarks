// this file is part of redUniverse toolkit /redFrik


//--keeper of worlds
RedUniverse {
	classvar <>worlds;
	*clear {worlds= nil}							//usually not needed
	*add {|redWorld| worlds= worlds.add(redWorld)}		//usually automatic - no need to call this
	*remove {|redWorld| worlds.remove(redWorld)}		//after interpolate
	*migrate {|redObj, toWorld|						//move one object
		redObj.world.remove(redObj);
		toWorld.add(redObj);
	}
	*migrateAll {|fromWorld, toWorld|				//move all objects
		fromWorld.objects.copy.do{|o| this.migrate(o, toWorld)};
		^toWorld
	}
	*interpolate {|aWorld, bWorld, percent= 0.5|		//return a new world
		^[aWorld, bWorld][percent.clip(0, 1).round].species.new(
			aWorld.dim.collect{|x, i| x.blend(bWorld.dim.clipAt(i), percent)},
			aWorld.gravity.collect{|x, i| x.blend(bWorld.gravity.clipAt(i), percent)},
			aWorld.maxVel.blend(bWorld.maxVel, percent),
			aWorld.damping.blend(bWorld.damping, percent)
		)
	}
	*interpolateMigrate {|aWorld, bWorld, percent= 0.5|	//return a new world and migrate objects
		var newWorld= this.interpolate(aWorld, bWorld, percent);
		var aObjs= aWorld.objects.copyRange(0, (aWorld.objects.size*(1-percent)).asInteger-1);
		var bObjs= bWorld.objects.copyRange(0, (bWorld.objects.size*percent).asInteger-1);
		aObjs.do{|o| this.migrate(o, newWorld)};
		bObjs.do{|o| this.migrate(o, newWorld)};
		^newWorld
	}
	*write {|path| worlds.writeArchive(path)}			//save all worlds and containing objects
	*read {|path| worlds= Object.readArchive(path)}	//clear current worlds
}
