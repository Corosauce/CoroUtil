#version 120

in vec3 exColour;
varying out vec4 fragColor;

void main()
{
	fragColor = vec4(exColour, 1.0);
}