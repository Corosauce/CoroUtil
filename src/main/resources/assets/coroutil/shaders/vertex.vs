#version 120

in vec3 position;
in vec3 inColour;

varying out vec3 exColour;

void main()
{
	gl_Position = vec4(position, 1.0);
	exColour = inColour;
}