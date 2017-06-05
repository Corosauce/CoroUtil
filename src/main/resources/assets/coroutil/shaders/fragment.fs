#version 120

in vec2 outTexCoord;
in float outBrightness;
in vec4 outRGBA;
//in vec3 mvPos;
varying out vec4 fragColor;

uniform sampler2D texture_sampler;

float test = 0.5;

void main()
{
	fragColor = texture2D(texture_sampler, outTexCoord);
	fragColor.x *= outRGBA.x * outBrightness;
	fragColor.y *= outRGBA.y * outBrightness;
	fragColor.z *= outRGBA.z * outBrightness;
	fragColor.w *= outRGBA.w;
}