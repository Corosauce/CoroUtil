#version 120

in vec2 outTexCoord;
in vec3 mvPos;
varying out vec4 fragColor;

uniform sampler2D texture_sampler;

void main()
{
	fragColor = texture2D(texture_sampler, outTexCoord);
}