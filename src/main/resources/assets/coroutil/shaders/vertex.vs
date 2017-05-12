#version 120

in vec3 position;
in vec2 texCoord;

varying out vec2 outTexCoord;

uniform mat4 modelViewMatrix;
uniform mat4 projectionMatrix;

void main()
{
	gl_Position = projectionMatrix * modelViewMatrix * vec4(position, 1.0);
	outTexCoord = texCoord;
}