#version 120

in vec3 position;
in vec2 texCoord;
in vec3 vertexNormal;
in mat4 modelViewMatrix;
in float brightness;
//in vec2 texOffset;

varying out vec2 outTexCoord;
varying out float outBrightness;

//uniform mat4 modelViewMatrix;
uniform mat4 projectionMatrix;

//uniform int numCols;
//uniform int numRows;

void main()
{
	gl_Position = projectionMatrix * modelViewMatrix * vec4(position, 1.0);

	// Support for texture atlas, update texture coordinates
    //float x = (texCoord.x / numCols + texOffset.x);
    //float y = (texCoord.y / numRows + texOffset.y);

	outTexCoord = texCoord;
	outBrightness = brightness;
}