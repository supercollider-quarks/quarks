GTexQuad : UGen {
	*gr 
	{ 
		arg 
			vector1 = [-0.5, -0.5, 0], 
			vector2 = [0.5, -0.5, 0], 
			vector3 = [0.5, 0.5, 0], 
			vector4 = [-0.5, 0.5, 0], 
			tex_coord1 = [0, 0], 
			tex_coord2 = [1, 0], 
			tex_coord3 = [1,1], 
			tex_coord4 = [0,1], 
			tex_index = 0,
			normal = [0, 0, 1];

		^this.multiNew(
			'audio', 
			vector1[0], vector1[1], vector1[2], 
			vector2[0], vector2[1], vector2[2], 
			vector3[0], vector3[1], vector3[2], 
			vector4[0], vector4[1], vector4[2], 
			tex_coord1[0], tex_coord1[1], 
			tex_coord2[0], tex_coord2[1], 
			tex_coord3[0], tex_coord3[1], 
			tex_coord4[0], tex_coord4[1], 
			tex_index, 
			normal[0], normal[0], normal[0]
		);
	}
}

