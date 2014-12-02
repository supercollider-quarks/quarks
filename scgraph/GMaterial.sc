GMaterial : UGen {
	*gr { arg 
			in,
			shinyness = 0,
			ambient  = [0,0,0,1],
			diffuse  = [0,0,0,1],
			specular = [0,0,0,1],
			emissive = [0,0,0,1];

		^this.multiNew
		(
			'audio', 
			in, 
			shinyness, 
			ambient[0], ambient[1], ambient[2], ambient[3], 
			diffuse[0], diffuse[1], diffuse[2], diffuse[3], 
			specular[0], specular[1], specular[2], specular[3], 
			emissive[0], emissive[1], emissive[2], emissive[3]
		);
	}
}


