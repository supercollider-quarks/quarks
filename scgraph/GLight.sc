GLight : UGen {
	*gr 
	{ 
		arg 
			index = 0,

			on = 1,

			// if w != 0 the light is treated as positional (at homogenous coordinates x,y,z,w)
			position = [0,0,0,1],

			spot_direction = [0,0,0],

			spot_exponent = 0,
			spot_cutoff = 180,
			
			ambient_color = [0,0,0,1],
			
			diffuse_color = [0,0,0,1],
			
			specular_color = [0,0,0,1],
			
			constant_attenuation = 1,
			linear_attenuation = 0,
			quadratic_attenuation = 0;

		^this.multiNew
		(
			'audio', 
			index, 
			on, 
			position[0], position[1], position[2], position[3], 
			spot_direction[0], spot_direction[1], spot_direction[2], 
			spot_exponent, 
			spot_cutoff, 
			ambient_color[0], ambient_color[1], ambient_color[2], ambient_color[3], 
			diffuse_color[0], diffuse_color[1], diffuse_color[2], diffuse_color[3], 
			specular_color[0], specular_color[1], specular_color[2], specular_color[3], 
			constant_attenuation, 
			linear_attenuation, 
			quadratic_attenuation
		);
	}
}
