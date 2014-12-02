JITBlock : SoundBlock {
	var <>server;
	var <>type;
	var <>faceChangeAction, <>invisibleAction, <>blockUpdateAction;
	
	*new{|color=\red, number=0, visualsAddr, type|
		^super.new(color, number, visualsAddr).initHome(type)
	}
	
	initHome {|aType|
		type = aType;
	}


	performFaceChange {|face|
		super.performFaceChange(face);
		faceChangeAction.value(this, face)
	}

	performInvisible {
		super.performInvisible;
		invisibleAction.value(this)
	}

	performBlockUpdate {
		super.performBlockUpdate;
		blockUpdateAction.value(this)
	}

	others {
		^Data2ParamBlock.all difference: [this];
	}
}
