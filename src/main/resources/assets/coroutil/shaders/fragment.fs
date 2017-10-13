#version 120

varying vec2 outTexCoord;
varying float outBrightness;
varying vec4 outRGBA;
//in vec3 mvPos;
//varying out vec4 fragColor;

uniform sampler2D texture_sampler;

float test = 0.5;

void main()
{
	gl_FragColor = texture2D(texture_sampler, outTexCoord);
	gl_FragColor.x *= outRGBA.x * outBrightness;
	gl_FragColor.y *= outRGBA.y * outBrightness;
	gl_FragColor.z *= outRGBA.z * outBrightness;
	gl_FragColor.w *= outRGBA.w;
}