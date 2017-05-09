#version 120

in vec3 position;
in vec3 inColour;

varying out vec3 exColour;

uniform mat4 worldMatrix;
uniform mat4 projectionMatrix;

void main()
{
	gl_Position = projectionMatrix * worldMatrix * vec4(position, 1.0);
	exColour = inColour;
}