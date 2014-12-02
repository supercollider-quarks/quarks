{
	"patcher" : 	{
		"fileversion" : 1,
		"appversion" : 		{
			"major" : 5,
			"minor" : 1,
			"revision" : 9
		}
,
		"rect" : [ 153.0, 223.0, 922.0, 592.0 ],
		"bglocked" : 0,
		"defrect" : [ 153.0, 223.0, 922.0, 592.0 ],
		"openrect" : [ 0.0, 0.0, 0.0, 0.0 ],
		"openinpresentation" : 0,
		"default_fontsize" : 12.0,
		"default_fontface" : 0,
		"default_fontname" : "Arial",
		"gridonopen" : 0,
		"gridsize" : [ 15.0, 15.0 ],
		"gridsnaponopen" : 0,
		"toolbarvisible" : 1,
		"boxanimatetime" : 200,
		"imprint" : 0,
		"enablehscroll" : 1,
		"enablevscroll" : 1,
		"devicewidth" : 0.0,
		"boxes" : [ 			{
				"box" : 				{
					"maxclass" : "preset",
					"presentation_rect" : [ 480.0, 75.0, 100.0, 40.0 ],
					"numinlets" : 1,
					"patching_rect" : [ 720.0, 60.0, 100.0, 40.0 ],
					"numoutlets" : 4,
					"presentation" : 1,
					"id" : "obj-126",
					"outlettype" : [ "preset", "int", "preset", "int" ],
					"preset_data" : [ 						{
							"number" : 1,
							"data" : [ 5, "obj-18", "number", "int", -10, 5, "obj-28", "flonum", "float", 1.0, 5, "obj-41", "slider", "float", 117.0, 6, "obj-44", "rslider", "list", 78, 78, 5, "obj-64", "slider", "float", 0.0, 5, "obj-68", "flonum", "float", 0.001, 6, "obj-78", "rslider", "list", 63, 64, 5, "obj-72", "slider", "float", 0.0, 5, "obj-69", "flonum", "float", 0.001, 6, "obj-103", "rslider", "list", 82, 82, 5, "obj-97", "slider", "float", 0.0, 5, "obj-94", "flonum", "float", 0.001, 6, "obj-114", "rslider", "list", 66, 66, 5, "obj-108", "slider", "float", 0.0, 5, "obj-105", "flonum", "float", 0.001, 6, "obj-125", "rslider", "list", 64, 64, 5, "obj-119", "slider", "float", 0.0, 5, "obj-116", "flonum", "float", 0.001 ]
						}
, 						{
							"number" : 2,
							"data" : [ 5, "obj-18", "number", "int", -10, 5, "obj-28", "flonum", "float", 1.0, 5, "obj-41", "slider", "float", 117.0, 6, "obj-44", "rslider", "list", 78, 78, 5, "obj-64", "slider", "float", 0.0, 5, "obj-68", "flonum", "float", 0.001, 6, "obj-78", "rslider", "list", 63, 64, 5, "obj-72", "slider", "float", 0.0, 5, "obj-69", "flonum", "float", 0.001, 6, "obj-103", "rslider", "list", 61, 69, 5, "obj-97", "slider", "float", 126.0, 5, "obj-94", "flonum", "float", 37.735909, 6, "obj-114", "rslider", "list", 32, 58, 5, "obj-108", "slider", "float", 41.0, 5, "obj-105", "flonum", "float", 0.26753, 6, "obj-125", "rslider", "list", 64, 64, 5, "obj-119", "slider", "float", 0.0, 5, "obj-116", "flonum", "float", 0.001 ]
						}
, 						{
							"number" : 3,
							"data" : [ 5, "obj-18", "number", "int", -10, 5, "obj-28", "flonum", "float", 1.0, 5, "obj-41", "slider", "float", 117.0, 6, "obj-44", "rslider", "list", 58, 64, 5, "obj-64", "slider", "float", 29.0, 5, "obj-68", "flonum", "float", 0.133458, 6, "obj-78", "rslider", "list", 8, 126, 5, "obj-72", "slider", "float", 124.0, 5, "obj-69", "flonum", "float", 33.584938, 6, "obj-103", "rslider", "list", 61, 69, 5, "obj-97", "slider", "float", 126.0, 5, "obj-94", "flonum", "float", 37.735909, 6, "obj-114", "rslider", "list", 32, 58, 5, "obj-108", "slider", "float", 41.0, 5, "obj-105", "flonum", "float", 0.26753, 6, "obj-125", "rslider", "list", 44, 82, 5, "obj-119", "slider", "float", 117.0, 5, "obj-116", "flonum", "float", 22.336245 ]
						}
 ]
				}

			}
, 			{
				"box" : 				{
					"maxclass" : "comment",
					"text" : "duration and rate of change",
					"fontsize" : 12.0,
					"presentation_rect" : [ 735.0, 165.0, 157.0, 20.0 ],
					"numinlets" : 1,
					"patching_rect" : [ 735.0, 285.0, 157.0, 20.0 ],
					"numoutlets" : 0,
					"presentation" : 1,
					"id" : "obj-115",
					"fontname" : "Arial"
				}

			}
, 			{
				"box" : 				{
					"maxclass" : "flonum",
					"fontsize" : 12.0,
					"presentation_rect" : [ 1170.0, 289.0, 0.0, 0.0 ],
					"numinlets" : 1,
					"patching_rect" : [ 765.0, 510.0, 50.0, 20.0 ],
					"numoutlets" : 2,
					"id" : "obj-116",
					"fontname" : "Arial",
					"outlettype" : [ "float", "bang" ]
				}

			}
, 			{
				"box" : 				{
					"maxclass" : "newobj",
					"text" : "scale 0 127 0.001 40 1.06",
					"fontsize" : 12.0,
					"presentation_rect" : [ 1170.0, 259.0, 0.0, 0.0 ],
					"numinlets" : 6,
					"patching_rect" : [ 765.0, 480.0, 149.0, 20.0 ],
					"numoutlets" : 1,
					"id" : "obj-117",
					"fontname" : "Arial",
					"outlettype" : [ "" ]
				}

			}
, 			{
				"box" : 				{
					"maxclass" : "message",
					"text" : "/oo synth set durRate $1",
					"fontsize" : 12.0,
					"presentation_rect" : [ 1155.0, 319.0, 0.0, 0.0 ],
					"numinlets" : 2,
					"patching_rect" : [ 750.0, 540.0, 141.0, 18.0 ],
					"numoutlets" : 1,
					"id" : "obj-118",
					"fontname" : "Arial",
					"outlettype" : [ "" ]
				}

			}
, 			{
				"box" : 				{
					"maxclass" : "slider",
					"presentation_rect" : [ 750.0, 225.0, 140.0, 20.0 ],
					"numinlets" : 1,
					"patching_rect" : [ 750.0, 345.0, 140.0, 20.0 ],
					"numoutlets" : 1,
					"size" : 150.0,
					"presentation" : 1,
					"id" : "obj-119",
					"outlettype" : [ "" ]
				}

			}
, 			{
				"box" : 				{
					"maxclass" : "message",
					"text" : "/oo synth set durMin $1",
					"fontsize" : 12.0,
					"presentation_rect" : [ 1140.0, 184.0, 0.0, 0.0 ],
					"numinlets" : 2,
					"patching_rect" : [ 735.0, 405.0, 135.0, 18.0 ],
					"numoutlets" : 1,
					"id" : "obj-120",
					"fontname" : "Arial",
					"outlettype" : [ "" ]
				}

			}
, 			{
				"box" : 				{
					"maxclass" : "newobj",
					"text" : "s toSC",
					"fontsize" : 12.0,
					"presentation_rect" : [ 1140.0, 349.0, 0.0, 0.0 ],
					"numinlets" : 1,
					"patching_rect" : [ 735.0, 570.0, 47.0, 20.0 ],
					"numoutlets" : 0,
					"id" : "obj-121",
					"fontname" : "Arial"
				}

			}
, 			{
				"box" : 				{
					"maxclass" : "message",
					"text" : "/oo synth set durMax $1",
					"fontsize" : 12.0,
					"presentation_rect" : [ 1155.0, 214.0, 0.0, 0.0 ],
					"numinlets" : 2,
					"patching_rect" : [ 750.0, 435.0, 139.0, 18.0 ],
					"numoutlets" : 1,
					"id" : "obj-122",
					"fontname" : "Arial",
					"outlettype" : [ "" ]
				}

			}
, 			{
				"box" : 				{
					"maxclass" : "newobj",
					"text" : "/ 127.",
					"fontsize" : 12.0,
					"numinlets" : 2,
					"patching_rect" : [ 735.0, 375.0, 41.0, 20.0 ],
					"numoutlets" : 1,
					"id" : "obj-123",
					"fontname" : "Arial",
					"outlettype" : [ "float" ]
				}

			}
, 			{
				"box" : 				{
					"maxclass" : "newobj",
					"text" : "/ 127.",
					"fontsize" : 12.0,
					"numinlets" : 2,
					"patching_rect" : [ 795.0, 375.0, 41.0, 20.0 ],
					"numoutlets" : 1,
					"id" : "obj-124",
					"fontname" : "Arial",
					"outlettype" : [ "float" ]
				}

			}
, 			{
				"box" : 				{
					"maxclass" : "rslider",
					"presentation_rect" : [ 735.0, 195.0, 140.0, 21.0 ],
					"numinlets" : 2,
					"patching_rect" : [ 735.0, 315.0, 140.0, 21.0 ],
					"numoutlets" : 2,
					"presentation" : 1,
					"id" : "obj-125",
					"outlettype" : [ "", "" ]
				}

			}
, 			{
				"box" : 				{
					"maxclass" : "comment",
					"text" : "triggerrate and rate of change",
					"fontsize" : 12.0,
					"presentation_rect" : [ 555.0, 165.0, 169.0, 20.0 ],
					"numinlets" : 1,
					"patching_rect" : [ 555.0, 285.0, 169.0, 20.0 ],
					"numoutlets" : 0,
					"presentation" : 1,
					"id" : "obj-104",
					"fontname" : "Arial"
				}

			}
, 			{
				"box" : 				{
					"maxclass" : "flonum",
					"fontsize" : 12.0,
					"presentation_rect" : [ 992.0, 283.0, 0.0, 0.0 ],
					"numinlets" : 1,
					"patching_rect" : [ 585.0, 510.0, 50.0, 20.0 ],
					"numoutlets" : 2,
					"id" : "obj-105",
					"fontname" : "Arial",
					"outlettype" : [ "float", "bang" ]
				}

			}
, 			{
				"box" : 				{
					"maxclass" : "newobj",
					"text" : "scale 0 127 0.001 40 1.06",
					"fontsize" : 12.0,
					"presentation_rect" : [ 992.0, 253.0, 0.0, 0.0 ],
					"numinlets" : 6,
					"patching_rect" : [ 585.0, 480.0, 149.0, 20.0 ],
					"numoutlets" : 1,
					"id" : "obj-106",
					"fontname" : "Arial",
					"outlettype" : [ "" ]
				}

			}
, 			{
				"box" : 				{
					"maxclass" : "message",
					"text" : "/oo synth set trgRate $1",
					"fontsize" : 12.0,
					"presentation_rect" : [ 977.0, 313.0, 0.0, 0.0 ],
					"numinlets" : 2,
					"patching_rect" : [ 570.0, 540.0, 138.0, 18.0 ],
					"numoutlets" : 1,
					"id" : "obj-107",
					"fontname" : "Arial",
					"outlettype" : [ "" ]
				}

			}
, 			{
				"box" : 				{
					"maxclass" : "slider",
					"presentation_rect" : [ 570.0, 225.0, 140.0, 20.0 ],
					"numinlets" : 1,
					"patching_rect" : [ 570.0, 345.0, 140.0, 20.0 ],
					"numoutlets" : 1,
					"size" : 150.0,
					"presentation" : 1,
					"id" : "obj-108",
					"outlettype" : [ "" ]
				}

			}
, 			{
				"box" : 				{
					"maxclass" : "message",
					"text" : "/oo synth set trgMin $1",
					"fontsize" : 12.0,
					"presentation_rect" : [ 962.0, 178.0, 0.0, 0.0 ],
					"numinlets" : 2,
					"patching_rect" : [ 555.0, 405.0, 132.0, 18.0 ],
					"numoutlets" : 1,
					"id" : "obj-109",
					"fontname" : "Arial",
					"outlettype" : [ "" ]
				}

			}
, 			{
				"box" : 				{
					"maxclass" : "newobj",
					"text" : "s toSC",
					"fontsize" : 12.0,
					"presentation_rect" : [ 962.0, 343.0, 0.0, 0.0 ],
					"numinlets" : 1,
					"patching_rect" : [ 555.0, 570.0, 47.0, 20.0 ],
					"numoutlets" : 0,
					"id" : "obj-110",
					"fontname" : "Arial"
				}

			}
, 			{
				"box" : 				{
					"maxclass" : "message",
					"text" : "/oo synth set trgMax $1",
					"fontsize" : 12.0,
					"presentation_rect" : [ 977.0, 208.0, 0.0, 0.0 ],
					"numinlets" : 2,
					"patching_rect" : [ 570.0, 435.0, 135.0, 18.0 ],
					"numoutlets" : 1,
					"id" : "obj-111",
					"fontname" : "Arial",
					"outlettype" : [ "" ]
				}

			}
, 			{
				"box" : 				{
					"maxclass" : "newobj",
					"text" : "/ 255.",
					"fontsize" : 12.0,
					"numinlets" : 2,
					"patching_rect" : [ 555.0, 375.0, 41.0, 20.0 ],
					"numoutlets" : 1,
					"id" : "obj-112",
					"fontname" : "Arial",
					"outlettype" : [ "float" ]
				}

			}
, 			{
				"box" : 				{
					"maxclass" : "newobj",
					"text" : "/ 255.",
					"fontsize" : 12.0,
					"numinlets" : 2,
					"patching_rect" : [ 615.0, 375.0, 41.0, 20.0 ],
					"numoutlets" : 1,
					"id" : "obj-113",
					"fontname" : "Arial",
					"outlettype" : [ "float" ]
				}

			}
, 			{
				"box" : 				{
					"maxclass" : "rslider",
					"presentation_rect" : [ 555.0, 195.0, 140.0, 21.0 ],
					"numinlets" : 2,
					"patching_rect" : [ 555.0, 315.0, 140.0, 21.0 ],
					"numoutlets" : 2,
					"presentation" : 1,
					"id" : "obj-114",
					"outlettype" : [ "", "" ]
				}

			}
, 			{
				"box" : 				{
					"maxclass" : "comment",
					"text" : "playbackrate and rate of change",
					"fontsize" : 12.0,
					"presentation_rect" : [ 375.0, 165.0, 182.0, 20.0 ],
					"numinlets" : 1,
					"patching_rect" : [ 375.0, 285.0, 182.0, 20.0 ],
					"numoutlets" : 0,
					"presentation" : 1,
					"id" : "obj-93",
					"fontname" : "Arial"
				}

			}
, 			{
				"box" : 				{
					"maxclass" : "flonum",
					"fontsize" : 12.0,
					"presentation_rect" : [ 812.0, 287.0, 0.0, 0.0 ],
					"numinlets" : 1,
					"patching_rect" : [ 405.0, 510.0, 50.0, 20.0 ],
					"numoutlets" : 2,
					"id" : "obj-94",
					"fontname" : "Arial",
					"outlettype" : [ "float", "bang" ]
				}

			}
, 			{
				"box" : 				{
					"maxclass" : "newobj",
					"text" : "scale 0 127 0.001 40 1.06",
					"fontsize" : 12.0,
					"presentation_rect" : [ 812.0, 257.0, 0.0, 0.0 ],
					"numinlets" : 6,
					"patching_rect" : [ 405.0, 480.0, 149.0, 20.0 ],
					"numoutlets" : 1,
					"id" : "obj-95",
					"fontname" : "Arial",
					"outlettype" : [ "" ]
				}

			}
, 			{
				"box" : 				{
					"maxclass" : "message",
					"text" : "/oo synth set pchRate $1",
					"fontsize" : 12.0,
					"presentation_rect" : [ 797.0, 317.0, 0.0, 0.0 ],
					"numinlets" : 2,
					"patching_rect" : [ 390.0, 540.0, 143.0, 18.0 ],
					"numoutlets" : 1,
					"id" : "obj-96",
					"fontname" : "Arial",
					"outlettype" : [ "" ]
				}

			}
, 			{
				"box" : 				{
					"maxclass" : "slider",
					"presentation_rect" : [ 390.0, 225.0, 140.0, 20.0 ],
					"numinlets" : 1,
					"patching_rect" : [ 390.0, 345.0, 140.0, 20.0 ],
					"numoutlets" : 1,
					"size" : 150.0,
					"presentation" : 1,
					"id" : "obj-97",
					"outlettype" : [ "" ]
				}

			}
, 			{
				"box" : 				{
					"maxclass" : "message",
					"text" : "/oo synth set pchMin $1",
					"fontsize" : 12.0,
					"presentation_rect" : [ 782.0, 182.0, 0.0, 0.0 ],
					"numinlets" : 2,
					"patching_rect" : [ 375.0, 405.0, 137.0, 18.0 ],
					"numoutlets" : 1,
					"id" : "obj-98",
					"fontname" : "Arial",
					"outlettype" : [ "" ]
				}

			}
, 			{
				"box" : 				{
					"maxclass" : "newobj",
					"text" : "s toSC",
					"fontsize" : 12.0,
					"presentation_rect" : [ 782.0, 347.0, 0.0, 0.0 ],
					"numinlets" : 1,
					"patching_rect" : [ 375.0, 570.0, 47.0, 20.0 ],
					"numoutlets" : 0,
					"id" : "obj-99",
					"fontname" : "Arial"
				}

			}
, 			{
				"box" : 				{
					"maxclass" : "message",
					"text" : "/oo synth set pchMax $1",
					"fontsize" : 12.0,
					"presentation_rect" : [ 797.0, 212.0, 0.0, 0.0 ],
					"numinlets" : 2,
					"patching_rect" : [ 390.0, 435.0, 141.0, 18.0 ],
					"numoutlets" : 1,
					"id" : "obj-100",
					"fontname" : "Arial",
					"outlettype" : [ "" ]
				}

			}
, 			{
				"box" : 				{
					"maxclass" : "newobj",
					"text" : "/ 32.",
					"fontsize" : 12.0,
					"numinlets" : 2,
					"patching_rect" : [ 375.0, 375.0, 34.0, 20.0 ],
					"numoutlets" : 1,
					"id" : "obj-101",
					"fontname" : "Arial",
					"outlettype" : [ "float" ]
				}

			}
, 			{
				"box" : 				{
					"maxclass" : "newobj",
					"text" : "/ 32.",
					"fontsize" : 12.0,
					"numinlets" : 2,
					"patching_rect" : [ 435.0, 375.0, 34.0, 20.0 ],
					"numoutlets" : 1,
					"id" : "obj-102",
					"fontname" : "Arial",
					"outlettype" : [ "float" ]
				}

			}
, 			{
				"box" : 				{
					"maxclass" : "rslider",
					"presentation_rect" : [ 375.0, 195.0, 140.0, 21.0 ],
					"numinlets" : 2,
					"patching_rect" : [ 375.0, 315.0, 140.0, 21.0 ],
					"numoutlets" : 2,
					"presentation" : 1,
					"id" : "obj-103",
					"outlettype" : [ "", "" ]
				}

			}
, 			{
				"box" : 				{
					"maxclass" : "comment",
					"text" : "panning and rate of change",
					"fontsize" : 12.0,
					"presentation_rect" : [ 195.0, 165.0, 157.0, 20.0 ],
					"numinlets" : 1,
					"patching_rect" : [ 195.0, 285.0, 157.0, 20.0 ],
					"numoutlets" : 0,
					"presentation" : 1,
					"id" : "obj-81",
					"fontname" : "Arial"
				}

			}
, 			{
				"box" : 				{
					"maxclass" : "comment",
					"text" : "position and rate of change",
					"fontsize" : 12.0,
					"presentation_rect" : [ 15.0, 165.0, 155.0, 20.0 ],
					"numinlets" : 1,
					"patching_rect" : [ 15.0, 285.0, 155.0, 20.0 ],
					"numoutlets" : 0,
					"presentation" : 1,
					"id" : "obj-80",
					"fontname" : "Arial"
				}

			}
, 			{
				"box" : 				{
					"maxclass" : "flonum",
					"fontsize" : 12.0,
					"presentation_rect" : [ 629.0, 283.0, 0.0, 0.0 ],
					"numinlets" : 1,
					"patching_rect" : [ 225.0, 510.0, 50.0, 20.0 ],
					"numoutlets" : 2,
					"id" : "obj-69",
					"fontname" : "Arial",
					"outlettype" : [ "float", "bang" ]
				}

			}
, 			{
				"box" : 				{
					"maxclass" : "newobj",
					"text" : "scale 0 127 0.001 40 1.06",
					"fontsize" : 12.0,
					"presentation_rect" : [ 629.0, 253.0, 0.0, 0.0 ],
					"numinlets" : 6,
					"patching_rect" : [ 225.0, 480.0, 149.0, 20.0 ],
					"numoutlets" : 1,
					"id" : "obj-70",
					"fontname" : "Arial",
					"outlettype" : [ "" ]
				}

			}
, 			{
				"box" : 				{
					"maxclass" : "message",
					"text" : "/oo synth set panRate $1",
					"fontsize" : 12.0,
					"presentation_rect" : [ 614.0, 313.0, 0.0, 0.0 ],
					"numinlets" : 2,
					"patching_rect" : [ 210.0, 540.0, 144.0, 18.0 ],
					"numoutlets" : 1,
					"id" : "obj-71",
					"fontname" : "Arial",
					"outlettype" : [ "" ]
				}

			}
, 			{
				"box" : 				{
					"maxclass" : "slider",
					"presentation_rect" : [ 210.0, 225.0, 140.0, 20.0 ],
					"numinlets" : 1,
					"patching_rect" : [ 210.0, 345.0, 140.0, 20.0 ],
					"numoutlets" : 1,
					"size" : 150.0,
					"presentation" : 1,
					"id" : "obj-72",
					"outlettype" : [ "" ]
				}

			}
, 			{
				"box" : 				{
					"maxclass" : "message",
					"text" : "/oo synth set panMin $1",
					"fontsize" : 12.0,
					"presentation_rect" : [ 599.0, 178.0, 0.0, 0.0 ],
					"numinlets" : 2,
					"patching_rect" : [ 195.0, 405.0, 138.0, 18.0 ],
					"numoutlets" : 1,
					"id" : "obj-73",
					"fontname" : "Arial",
					"outlettype" : [ "" ]
				}

			}
, 			{
				"box" : 				{
					"maxclass" : "newobj",
					"text" : "s toSC",
					"fontsize" : 12.0,
					"presentation_rect" : [ 599.0, 343.0, 0.0, 0.0 ],
					"numinlets" : 1,
					"patching_rect" : [ 195.0, 570.0, 47.0, 20.0 ],
					"numoutlets" : 0,
					"id" : "obj-74",
					"fontname" : "Arial"
				}

			}
, 			{
				"box" : 				{
					"maxclass" : "message",
					"text" : "/oo synth set panMax $1",
					"fontsize" : 12.0,
					"presentation_rect" : [ 614.0, 208.0, 0.0, 0.0 ],
					"numinlets" : 2,
					"patching_rect" : [ 210.0, 435.0, 141.0, 18.0 ],
					"numoutlets" : 1,
					"id" : "obj-75",
					"fontname" : "Arial",
					"outlettype" : [ "" ]
				}

			}
, 			{
				"box" : 				{
					"maxclass" : "newobj",
					"text" : "/ 127.",
					"fontsize" : 12.0,
					"presentation_rect" : [ 599.0, 148.0, 0.0, 0.0 ],
					"numinlets" : 2,
					"patching_rect" : [ 195.0, 375.0, 41.0, 20.0 ],
					"numoutlets" : 1,
					"id" : "obj-76",
					"fontname" : "Arial",
					"outlettype" : [ "float" ]
				}

			}
, 			{
				"box" : 				{
					"maxclass" : "newobj",
					"text" : "/ 127.",
					"fontsize" : 12.0,
					"presentation_rect" : [ 659.0, 148.0, 0.0, 0.0 ],
					"numinlets" : 2,
					"patching_rect" : [ 255.0, 375.0, 41.0, 20.0 ],
					"numoutlets" : 1,
					"id" : "obj-77",
					"fontname" : "Arial",
					"outlettype" : [ "float" ]
				}

			}
, 			{
				"box" : 				{
					"maxclass" : "rslider",
					"presentation_rect" : [ 195.0, 195.0, 140.0, 21.0 ],
					"numinlets" : 2,
					"patching_rect" : [ 195.0, 315.0, 140.0, 21.0 ],
					"numoutlets" : 2,
					"size" : 255.0,
					"min" : -127,
					"presentation" : 1,
					"id" : "obj-78",
					"outlettype" : [ "", "" ]
				}

			}
, 			{
				"box" : 				{
					"maxclass" : "flonum",
					"fontsize" : 12.0,
					"numinlets" : 1,
					"patching_rect" : [ 45.0, 510.0, 50.0, 20.0 ],
					"numoutlets" : 2,
					"id" : "obj-68",
					"fontname" : "Arial",
					"outlettype" : [ "float", "bang" ]
				}

			}
, 			{
				"box" : 				{
					"maxclass" : "newobj",
					"text" : "scale 0 127 0.001 40 1.06",
					"fontsize" : 12.0,
					"numinlets" : 6,
					"patching_rect" : [ 45.0, 480.0, 149.0, 20.0 ],
					"numoutlets" : 1,
					"id" : "obj-66",
					"fontname" : "Arial",
					"outlettype" : [ "" ]
				}

			}
, 			{
				"box" : 				{
					"maxclass" : "message",
					"text" : "/oo synth set posRate $1",
					"fontsize" : 12.0,
					"presentation_rect" : [ 435.0, 251.0, 0.0, 0.0 ],
					"numinlets" : 2,
					"patching_rect" : [ 30.0, 540.0, 143.0, 18.0 ],
					"numoutlets" : 1,
					"id" : "obj-65",
					"fontname" : "Arial",
					"outlettype" : [ "" ]
				}

			}
, 			{
				"box" : 				{
					"maxclass" : "slider",
					"presentation_rect" : [ 30.0, 225.0, 140.0, 20.0 ],
					"numinlets" : 1,
					"patching_rect" : [ 30.0, 345.0, 140.0, 20.0 ],
					"numoutlets" : 1,
					"size" : 150.0,
					"presentation" : 1,
					"id" : "obj-64",
					"outlettype" : [ "" ]
				}

			}
, 			{
				"box" : 				{
					"maxclass" : "message",
					"text" : "/oo synth set posMin $1",
					"fontsize" : 12.0,
					"presentation_rect" : [ 422.0, 190.0, 0.0, 0.0 ],
					"numinlets" : 2,
					"patching_rect" : [ 15.0, 405.0, 137.0, 18.0 ],
					"numoutlets" : 1,
					"id" : "obj-59",
					"fontname" : "Arial",
					"outlettype" : [ "" ]
				}

			}
, 			{
				"box" : 				{
					"maxclass" : "newobj",
					"text" : "s toSC",
					"fontsize" : 12.0,
					"presentation_rect" : [ 421.0, 243.0, 0.0, 0.0 ],
					"numinlets" : 1,
					"patching_rect" : [ 15.0, 570.0, 47.0, 20.0 ],
					"numoutlets" : 0,
					"id" : "obj-58",
					"fontname" : "Arial"
				}

			}
, 			{
				"box" : 				{
					"maxclass" : "message",
					"text" : "/oo synth set posMax $1",
					"fontsize" : 12.0,
					"numinlets" : 2,
					"patching_rect" : [ 30.0, 435.0, 141.0, 18.0 ],
					"numoutlets" : 1,
					"id" : "obj-57",
					"fontname" : "Arial",
					"outlettype" : [ "" ]
				}

			}
, 			{
				"box" : 				{
					"maxclass" : "newobj",
					"text" : "/ 127.",
					"fontsize" : 12.0,
					"presentation_rect" : [ 419.0, 134.0, 0.0, 0.0 ],
					"numinlets" : 2,
					"patching_rect" : [ 15.0, 375.0, 41.0, 20.0 ],
					"numoutlets" : 1,
					"id" : "obj-52",
					"fontname" : "Arial",
					"outlettype" : [ "float" ]
				}

			}
, 			{
				"box" : 				{
					"maxclass" : "newobj",
					"text" : "/ 127.",
					"fontsize" : 12.0,
					"numinlets" : 2,
					"patching_rect" : [ 75.0, 375.0, 41.0, 20.0 ],
					"numoutlets" : 1,
					"id" : "obj-51",
					"fontname" : "Arial",
					"outlettype" : [ "float" ]
				}

			}
, 			{
				"box" : 				{
					"maxclass" : "rslider",
					"presentation_rect" : [ 15.0, 195.0, 140.0, 21.0 ],
					"numinlets" : 2,
					"patching_rect" : [ 15.0, 315.0, 140.0, 21.0 ],
					"numoutlets" : 2,
					"presentation" : 1,
					"id" : "obj-44",
					"outlettype" : [ "", "" ]
				}

			}
, 			{
				"box" : 				{
					"maxclass" : "comment",
					"text" : "main volume",
					"fontsize" : 12.0,
					"presentation_rect" : [ 300.0, 30.0, 79.0, 20.0 ],
					"numinlets" : 1,
					"patching_rect" : [ 390.0, 30.0, 79.0, 20.0 ],
					"numoutlets" : 0,
					"presentation" : 1,
					"id" : "obj-42",
					"fontname" : "Arial"
				}

			}
, 			{
				"box" : 				{
					"maxclass" : "slider",
					"presentation_rect" : [ 270.0, 15.0, 20.0, 140.0 ],
					"numinlets" : 1,
					"patching_rect" : [ 360.0, 15.0, 20.0, 140.0 ],
					"numoutlets" : 1,
					"size" : 150.0,
					"presentation" : 1,
					"id" : "obj-41",
					"outlettype" : [ "" ]
				}

			}
, 			{
				"box" : 				{
					"maxclass" : "comment",
					"text" : "release time in seconds\nfor fading out old synth",
					"linecount" : 2,
					"presentation_linecount" : 2,
					"fontsize" : 12.0,
					"presentation_rect" : [ 120.0, 45.0, 137.0, 34.0 ],
					"numinlets" : 1,
					"patching_rect" : [ 195.0, 165.0, 137.0, 34.0 ],
					"numoutlets" : 0,
					"presentation" : 1,
					"id" : "obj-40",
					"fontname" : "Arial"
				}

			}
, 			{
				"box" : 				{
					"maxclass" : "newobj",
					"text" : "loadmess 1.",
					"fontsize" : 12.0,
					"numinlets" : 1,
					"patching_rect" : [ 135.0, 195.0, 75.0, 20.0 ],
					"numoutlets" : 1,
					"id" : "obj-38",
					"fontname" : "Arial",
					"outlettype" : [ "" ],
					"hidden" : 1
				}

			}
, 			{
				"box" : 				{
					"maxclass" : "newobj",
					"text" : "pack s 1.",
					"fontsize" : 12.0,
					"numinlets" : 2,
					"patching_rect" : [ 60.0, 195.0, 59.0, 20.0 ],
					"numoutlets" : 1,
					"id" : "obj-32",
					"fontname" : "Arial",
					"outlettype" : [ "" ]
				}

			}
, 			{
				"box" : 				{
					"maxclass" : "flonum",
					"fontsize" : 12.0,
					"presentation_rect" : [ 60.0, 45.0, 50.0, 20.0 ],
					"numinlets" : 1,
					"patching_rect" : [ 135.0, 165.0, 50.0, 20.0 ],
					"numoutlets" : 2,
					"presentation" : 1,
					"id" : "obj-28",
					"fontname" : "Arial",
					"outlettype" : [ "float", "bang" ],
					"minimum" : 0.0
				}

			}
, 			{
				"box" : 				{
					"maxclass" : "comment",
					"text" : "db",
					"fontsize" : 12.0,
					"numinlets" : 1,
					"patching_rect" : [ 420.0, 195.0, 24.0, 20.0 ],
					"numoutlets" : 0,
					"id" : "obj-23",
					"fontname" : "Arial"
				}

			}
, 			{
				"box" : 				{
					"maxclass" : "newobj",
					"text" : "s toSC",
					"fontsize" : 12.0,
					"presentation_rect" : [ 672.0, 282.0, 0.0, 0.0 ],
					"numinlets" : 1,
					"patching_rect" : [ 60.0, 255.0, 47.0, 20.0 ],
					"numoutlets" : 0,
					"id" : "obj-21",
					"fontname" : "Arial"
				}

			}
, 			{
				"box" : 				{
					"maxclass" : "message",
					"text" : "/oo func value $1 $2",
					"fontsize" : 12.0,
					"presentation_rect" : [ 673.0, 259.0, 0.0, 0.0 ],
					"numinlets" : 2,
					"patching_rect" : [ 60.0, 225.0, 119.0, 18.0 ],
					"numoutlets" : 1,
					"id" : "obj-17",
					"fontname" : "Arial",
					"outlettype" : [ "" ]
				}

			}
, 			{
				"box" : 				{
					"maxclass" : "newobj",
					"text" : "tosymbol",
					"fontsize" : 12.0,
					"presentation_rect" : [ 673.0, 229.0, 0.0, 0.0 ],
					"numinlets" : 1,
					"patching_rect" : [ 60.0, 165.0, 59.0, 20.0 ],
					"numoutlets" : 1,
					"id" : "obj-19",
					"fontname" : "Arial",
					"outlettype" : [ "" ]
				}

			}
, 			{
				"box" : 				{
					"maxclass" : "message",
					"text" : "/Applications/SuperCollider344/sounds/a11wlk01-44_1.aiff",
					"linecount" : 3,
					"fontsize" : 12.0,
					"numinlets" : 2,
					"patching_rect" : [ 75.0, 105.0, 163.0, 46.0 ],
					"numoutlets" : 1,
					"id" : "obj-16",
					"fontname" : "Arial",
					"outlettype" : [ "" ]
				}

			}
, 			{
				"box" : 				{
					"maxclass" : "newobj",
					"text" : "conformpath @pathtype boot",
					"fontsize" : 12.0,
					"numinlets" : 1,
					"patching_rect" : [ 60.0, 75.0, 165.0, 20.0 ],
					"numoutlets" : 2,
					"id" : "obj-14",
					"fontname" : "Arial",
					"outlettype" : [ "", "int" ]
				}

			}
, 			{
				"box" : 				{
					"maxclass" : "button",
					"presentation_rect" : [ 60.0, 15.0, 20.0, 20.0 ],
					"numinlets" : 1,
					"patching_rect" : [ 60.0, 15.0, 20.0, 20.0 ],
					"numoutlets" : 1,
					"presentation" : 1,
					"id" : "obj-13",
					"outlettype" : [ "bang" ]
				}

			}
, 			{
				"box" : 				{
					"maxclass" : "newobj",
					"text" : "opendialog sound",
					"fontsize" : 12.0,
					"numinlets" : 1,
					"patching_rect" : [ 60.0, 45.0, 105.0, 20.0 ],
					"numoutlets" : 2,
					"id" : "obj-4",
					"fontname" : "Arial",
					"outlettype" : [ "", "bang" ]
				}

			}
, 			{
				"box" : 				{
					"maxclass" : "comment",
					"text" : "load a soundfile",
					"fontsize" : 12.0,
					"presentation_rect" : [ 75.0, 15.0, 95.0, 20.0 ],
					"numinlets" : 1,
					"patching_rect" : [ 75.0, 15.0, 95.0, 20.0 ],
					"numoutlets" : 0,
					"presentation" : 1,
					"id" : "obj-2",
					"fontname" : "Arial"
				}

			}
, 			{
				"box" : 				{
					"maxclass" : "newobj",
					"text" : "r toSC",
					"fontsize" : 12.0,
					"numinlets" : 0,
					"patching_rect" : [ 555.0, 135.0, 45.0, 20.0 ],
					"numoutlets" : 1,
					"id" : "obj-37",
					"fontname" : "Arial",
					"outlettype" : [ "" ]
				}

			}
, 			{
				"box" : 				{
					"maxclass" : "newobj",
					"text" : "s toSC",
					"fontsize" : 12.0,
					"numinlets" : 1,
					"patching_rect" : [ 360.0, 255.0, 47.0, 20.0 ],
					"numoutlets" : 0,
					"id" : "obj-36",
					"fontname" : "Arial"
				}

			}
, 			{
				"box" : 				{
					"maxclass" : "newobj",
					"text" : "loadmess 127",
					"fontsize" : 12.0,
					"numinlets" : 1,
					"patching_rect" : [ 390.0, 60.0, 85.0, 20.0 ],
					"numoutlets" : 1,
					"id" : "obj-35",
					"fontname" : "Arial",
					"outlettype" : [ "" ],
					"hidden" : 1
				}

			}
, 			{
				"box" : 				{
					"maxclass" : "newobj",
					"text" : "- 127",
					"fontsize" : 12.0,
					"numinlets" : 2,
					"patching_rect" : [ 360.0, 165.0, 38.0, 20.0 ],
					"numoutlets" : 1,
					"id" : "obj-33",
					"fontname" : "Arial",
					"outlettype" : [ "int" ]
				}

			}
, 			{
				"box" : 				{
					"maxclass" : "number",
					"fontsize" : 12.0,
					"numinlets" : 1,
					"patching_rect" : [ 360.0, 195.0, 50.0, 20.0 ],
					"numoutlets" : 2,
					"id" : "obj-18",
					"fontname" : "Arial",
					"outlettype" : [ "int", "bang" ]
				}

			}
, 			{
				"box" : 				{
					"maxclass" : "message",
					"text" : "/oo synth set vol $1",
					"fontsize" : 12.0,
					"numinlets" : 2,
					"patching_rect" : [ 360.0, 225.0, 114.0, 18.0 ],
					"numoutlets" : 1,
					"id" : "obj-12",
					"fontname" : "Arial",
					"outlettype" : [ "" ]
				}

			}
, 			{
				"box" : 				{
					"maxclass" : "newobj",
					"text" : "udpsend 127.0.0.1 57120",
					"fontsize" : 12.0,
					"numinlets" : 1,
					"patching_rect" : [ 555.0, 165.0, 147.0, 20.0 ],
					"numoutlets" : 0,
					"id" : "obj-3",
					"fontname" : "Arial"
				}

			}
 ],
		"lines" : [ 			{
				"patchline" : 				{
					"source" : [ "obj-124", 0 ],
					"destination" : [ "obj-122", 0 ],
					"hidden" : 0,
					"midpoints" : [  ]
				}

			}
, 			{
				"patchline" : 				{
					"source" : [ "obj-125", 1 ],
					"destination" : [ "obj-124", 0 ],
					"hidden" : 0,
					"midpoints" : [  ]
				}

			}
, 			{
				"patchline" : 				{
					"source" : [ "obj-125", 0 ],
					"destination" : [ "obj-123", 0 ],
					"hidden" : 0,
					"midpoints" : [  ]
				}

			}
, 			{
				"patchline" : 				{
					"source" : [ "obj-123", 0 ],
					"destination" : [ "obj-120", 0 ],
					"hidden" : 0,
					"midpoints" : [  ]
				}

			}
, 			{
				"patchline" : 				{
					"source" : [ "obj-116", 0 ],
					"destination" : [ "obj-118", 0 ],
					"hidden" : 0,
					"midpoints" : [  ]
				}

			}
, 			{
				"patchline" : 				{
					"source" : [ "obj-117", 0 ],
					"destination" : [ "obj-116", 0 ],
					"hidden" : 0,
					"midpoints" : [  ]
				}

			}
, 			{
				"patchline" : 				{
					"source" : [ "obj-119", 0 ],
					"destination" : [ "obj-117", 0 ],
					"hidden" : 0,
					"midpoints" : [  ]
				}

			}
, 			{
				"patchline" : 				{
					"source" : [ "obj-118", 0 ],
					"destination" : [ "obj-121", 0 ],
					"hidden" : 0,
					"midpoints" : [  ]
				}

			}
, 			{
				"patchline" : 				{
					"source" : [ "obj-120", 0 ],
					"destination" : [ "obj-121", 0 ],
					"hidden" : 0,
					"midpoints" : [  ]
				}

			}
, 			{
				"patchline" : 				{
					"source" : [ "obj-122", 0 ],
					"destination" : [ "obj-121", 0 ],
					"hidden" : 0,
					"midpoints" : [  ]
				}

			}
, 			{
				"patchline" : 				{
					"source" : [ "obj-113", 0 ],
					"destination" : [ "obj-111", 0 ],
					"hidden" : 0,
					"midpoints" : [  ]
				}

			}
, 			{
				"patchline" : 				{
					"source" : [ "obj-114", 1 ],
					"destination" : [ "obj-113", 0 ],
					"hidden" : 0,
					"midpoints" : [  ]
				}

			}
, 			{
				"patchline" : 				{
					"source" : [ "obj-114", 0 ],
					"destination" : [ "obj-112", 0 ],
					"hidden" : 0,
					"midpoints" : [  ]
				}

			}
, 			{
				"patchline" : 				{
					"source" : [ "obj-112", 0 ],
					"destination" : [ "obj-109", 0 ],
					"hidden" : 0,
					"midpoints" : [  ]
				}

			}
, 			{
				"patchline" : 				{
					"source" : [ "obj-111", 0 ],
					"destination" : [ "obj-110", 0 ],
					"hidden" : 0,
					"midpoints" : [  ]
				}

			}
, 			{
				"patchline" : 				{
					"source" : [ "obj-109", 0 ],
					"destination" : [ "obj-110", 0 ],
					"hidden" : 0,
					"midpoints" : [  ]
				}

			}
, 			{
				"patchline" : 				{
					"source" : [ "obj-107", 0 ],
					"destination" : [ "obj-110", 0 ],
					"hidden" : 0,
					"midpoints" : [  ]
				}

			}
, 			{
				"patchline" : 				{
					"source" : [ "obj-108", 0 ],
					"destination" : [ "obj-106", 0 ],
					"hidden" : 0,
					"midpoints" : [  ]
				}

			}
, 			{
				"patchline" : 				{
					"source" : [ "obj-106", 0 ],
					"destination" : [ "obj-105", 0 ],
					"hidden" : 0,
					"midpoints" : [  ]
				}

			}
, 			{
				"patchline" : 				{
					"source" : [ "obj-105", 0 ],
					"destination" : [ "obj-107", 0 ],
					"hidden" : 0,
					"midpoints" : [  ]
				}

			}
, 			{
				"patchline" : 				{
					"source" : [ "obj-103", 0 ],
					"destination" : [ "obj-101", 0 ],
					"hidden" : 0,
					"midpoints" : [  ]
				}

			}
, 			{
				"patchline" : 				{
					"source" : [ "obj-101", 0 ],
					"destination" : [ "obj-98", 0 ],
					"hidden" : 0,
					"midpoints" : [  ]
				}

			}
, 			{
				"patchline" : 				{
					"source" : [ "obj-102", 0 ],
					"destination" : [ "obj-100", 0 ],
					"hidden" : 0,
					"midpoints" : [  ]
				}

			}
, 			{
				"patchline" : 				{
					"source" : [ "obj-103", 1 ],
					"destination" : [ "obj-102", 0 ],
					"hidden" : 0,
					"midpoints" : [  ]
				}

			}
, 			{
				"patchline" : 				{
					"source" : [ "obj-94", 0 ],
					"destination" : [ "obj-96", 0 ],
					"hidden" : 0,
					"midpoints" : [  ]
				}

			}
, 			{
				"patchline" : 				{
					"source" : [ "obj-95", 0 ],
					"destination" : [ "obj-94", 0 ],
					"hidden" : 0,
					"midpoints" : [  ]
				}

			}
, 			{
				"patchline" : 				{
					"source" : [ "obj-97", 0 ],
					"destination" : [ "obj-95", 0 ],
					"hidden" : 0,
					"midpoints" : [  ]
				}

			}
, 			{
				"patchline" : 				{
					"source" : [ "obj-96", 0 ],
					"destination" : [ "obj-99", 0 ],
					"hidden" : 0,
					"midpoints" : [  ]
				}

			}
, 			{
				"patchline" : 				{
					"source" : [ "obj-98", 0 ],
					"destination" : [ "obj-99", 0 ],
					"hidden" : 0,
					"midpoints" : [  ]
				}

			}
, 			{
				"patchline" : 				{
					"source" : [ "obj-100", 0 ],
					"destination" : [ "obj-99", 0 ],
					"hidden" : 0,
					"midpoints" : [  ]
				}

			}
, 			{
				"patchline" : 				{
					"source" : [ "obj-78", 1 ],
					"destination" : [ "obj-77", 0 ],
					"hidden" : 0,
					"midpoints" : [  ]
				}

			}
, 			{
				"patchline" : 				{
					"source" : [ "obj-78", 0 ],
					"destination" : [ "obj-76", 0 ],
					"hidden" : 0,
					"midpoints" : [  ]
				}

			}
, 			{
				"patchline" : 				{
					"source" : [ "obj-77", 0 ],
					"destination" : [ "obj-75", 0 ],
					"hidden" : 0,
					"midpoints" : [  ]
				}

			}
, 			{
				"patchline" : 				{
					"source" : [ "obj-75", 0 ],
					"destination" : [ "obj-74", 0 ],
					"hidden" : 0,
					"midpoints" : [  ]
				}

			}
, 			{
				"patchline" : 				{
					"source" : [ "obj-76", 0 ],
					"destination" : [ "obj-73", 0 ],
					"hidden" : 0,
					"midpoints" : [  ]
				}

			}
, 			{
				"patchline" : 				{
					"source" : [ "obj-73", 0 ],
					"destination" : [ "obj-74", 0 ],
					"hidden" : 0,
					"midpoints" : [  ]
				}

			}
, 			{
				"patchline" : 				{
					"source" : [ "obj-71", 0 ],
					"destination" : [ "obj-74", 0 ],
					"hidden" : 0,
					"midpoints" : [  ]
				}

			}
, 			{
				"patchline" : 				{
					"source" : [ "obj-72", 0 ],
					"destination" : [ "obj-70", 0 ],
					"hidden" : 0,
					"midpoints" : [  ]
				}

			}
, 			{
				"patchline" : 				{
					"source" : [ "obj-70", 0 ],
					"destination" : [ "obj-69", 0 ],
					"hidden" : 0,
					"midpoints" : [  ]
				}

			}
, 			{
				"patchline" : 				{
					"source" : [ "obj-69", 0 ],
					"destination" : [ "obj-71", 0 ],
					"hidden" : 0,
					"midpoints" : [  ]
				}

			}
, 			{
				"patchline" : 				{
					"source" : [ "obj-68", 0 ],
					"destination" : [ "obj-65", 0 ],
					"hidden" : 0,
					"midpoints" : [  ]
				}

			}
, 			{
				"patchline" : 				{
					"source" : [ "obj-66", 0 ],
					"destination" : [ "obj-68", 0 ],
					"hidden" : 0,
					"midpoints" : [  ]
				}

			}
, 			{
				"patchline" : 				{
					"source" : [ "obj-64", 0 ],
					"destination" : [ "obj-66", 0 ],
					"hidden" : 0,
					"midpoints" : [  ]
				}

			}
, 			{
				"patchline" : 				{
					"source" : [ "obj-65", 0 ],
					"destination" : [ "obj-58", 0 ],
					"hidden" : 0,
					"midpoints" : [  ]
				}

			}
, 			{
				"patchline" : 				{
					"source" : [ "obj-59", 0 ],
					"destination" : [ "obj-58", 0 ],
					"hidden" : 0,
					"midpoints" : [  ]
				}

			}
, 			{
				"patchline" : 				{
					"source" : [ "obj-52", 0 ],
					"destination" : [ "obj-59", 0 ],
					"hidden" : 0,
					"midpoints" : [  ]
				}

			}
, 			{
				"patchline" : 				{
					"source" : [ "obj-57", 0 ],
					"destination" : [ "obj-58", 0 ],
					"hidden" : 0,
					"midpoints" : [  ]
				}

			}
, 			{
				"patchline" : 				{
					"source" : [ "obj-51", 0 ],
					"destination" : [ "obj-57", 0 ],
					"hidden" : 0,
					"midpoints" : [  ]
				}

			}
, 			{
				"patchline" : 				{
					"source" : [ "obj-44", 0 ],
					"destination" : [ "obj-52", 0 ],
					"hidden" : 0,
					"midpoints" : [  ]
				}

			}
, 			{
				"patchline" : 				{
					"source" : [ "obj-44", 1 ],
					"destination" : [ "obj-51", 0 ],
					"hidden" : 0,
					"midpoints" : [  ]
				}

			}
, 			{
				"patchline" : 				{
					"source" : [ "obj-41", 0 ],
					"destination" : [ "obj-33", 0 ],
					"hidden" : 0,
					"midpoints" : [  ]
				}

			}
, 			{
				"patchline" : 				{
					"source" : [ "obj-35", 0 ],
					"destination" : [ "obj-41", 0 ],
					"hidden" : 1,
					"midpoints" : [  ]
				}

			}
, 			{
				"patchline" : 				{
					"source" : [ "obj-38", 0 ],
					"destination" : [ "obj-28", 0 ],
					"hidden" : 1,
					"midpoints" : [  ]
				}

			}
, 			{
				"patchline" : 				{
					"source" : [ "obj-28", 0 ],
					"destination" : [ "obj-32", 1 ],
					"hidden" : 0,
					"midpoints" : [  ]
				}

			}
, 			{
				"patchline" : 				{
					"source" : [ "obj-32", 0 ],
					"destination" : [ "obj-17", 0 ],
					"hidden" : 0,
					"midpoints" : [  ]
				}

			}
, 			{
				"patchline" : 				{
					"source" : [ "obj-19", 0 ],
					"destination" : [ "obj-32", 0 ],
					"hidden" : 0,
					"midpoints" : [  ]
				}

			}
, 			{
				"patchline" : 				{
					"source" : [ "obj-17", 0 ],
					"destination" : [ "obj-21", 0 ],
					"hidden" : 0,
					"midpoints" : [  ]
				}

			}
, 			{
				"patchline" : 				{
					"source" : [ "obj-14", 0 ],
					"destination" : [ "obj-19", 0 ],
					"hidden" : 0,
					"midpoints" : [  ]
				}

			}
, 			{
				"patchline" : 				{
					"source" : [ "obj-14", 0 ],
					"destination" : [ "obj-16", 1 ],
					"hidden" : 0,
					"midpoints" : [  ]
				}

			}
, 			{
				"patchline" : 				{
					"source" : [ "obj-4", 0 ],
					"destination" : [ "obj-14", 0 ],
					"hidden" : 0,
					"midpoints" : [  ]
				}

			}
, 			{
				"patchline" : 				{
					"source" : [ "obj-13", 0 ],
					"destination" : [ "obj-4", 0 ],
					"hidden" : 0,
					"midpoints" : [  ]
				}

			}
, 			{
				"patchline" : 				{
					"source" : [ "obj-37", 0 ],
					"destination" : [ "obj-3", 0 ],
					"hidden" : 0,
					"midpoints" : [  ]
				}

			}
, 			{
				"patchline" : 				{
					"source" : [ "obj-33", 0 ],
					"destination" : [ "obj-18", 0 ],
					"hidden" : 0,
					"midpoints" : [  ]
				}

			}
, 			{
				"patchline" : 				{
					"source" : [ "obj-18", 0 ],
					"destination" : [ "obj-12", 0 ],
					"hidden" : 0,
					"midpoints" : [  ]
				}

			}
, 			{
				"patchline" : 				{
					"source" : [ "obj-12", 0 ],
					"destination" : [ "obj-36", 0 ],
					"hidden" : 0,
					"midpoints" : [  ]
				}

			}
 ]
	}

}
