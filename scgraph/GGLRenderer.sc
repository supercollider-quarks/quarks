GGLRenderer : UGen 
{
	*gr
	{ 
		arg 
			in,

			clear_mode  = 0,        // 0 = clear to clear_color, 1 = alpha blend clear_color
			clear_color = [0, 0, 0, 1],

			lighting =     0,       // 0 = off, 1 = on

			culling =      1,       // 0 = off, 1 = on
			transparency = 0,       // 0 = off, 1 = on

			perspective  = 1,       // 0 = orthographic, 1 = stereographic

			fov =          90,      // degrees
			near_plane =    0.1,    // near viewing plane distance
			far_plane =  1000.0,    // far viewing plane distance

			eye =    [0, 0, 10],    // eye coordinates of viewer
			center = [0, 0,  0],    // coordinates of what we look at
			up =     [0, 1,  0],    // up vector of viewer

			fog =           0,      // 0 = off, 1 = on
			fog_mode =      0,      // 0 = GL_LINEAR, 1 = GL_EXP, and 2 = GL_EXP2
			fog_density =   0,      // between 0 and 1
			fog_start   = 30.0,
			fog_end     = 100.0,
			fog_niceness =  0,      // 0 = don't care, 1 = fastest, 2 = nicest

			fog_color = [0, 0, 0, 1],

			texturing  = 0;         // 0 - off, 1 = on


		^this.multiNewList([
			'audio',
			in, 
			clear_mode, 
			clear_color[0], clear_color[1], clear_color[2], clear_color[3],
			lighting, 
			culling, 
			transparency, 
			perspective, 
			fov, 
			near_plane, 
			far_plane, 
			eye[0], eye[1], eye[2], 
			center[0], center[0], center[0], 
			up[0], up[1], up[2], 
			fog, 
			fog_mode, 
			fog_density, 
			fog_start,  
			fog_end, 
			fog_niceness, 
			fog_color[0], fog_color[1], fog_color[2], fog_color[3], 
			texturing
		]);
	}
}

