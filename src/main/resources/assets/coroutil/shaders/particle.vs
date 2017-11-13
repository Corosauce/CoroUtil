#version 120

attribute vec3 position;
attribute vec2 texCoord;
attribute vec3 vertexNormal;
attribute mat4 modelViewMatrix;
attribute float brightness;
//attribute vec4 rgba;
attribute vec4 rgbaTest;
//in vec2 texOffset;

varying vec2 outTexCoord;
varying float outBrightness;
varying vec4 outRGBA;

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

	//temp
	//rgba.x = 1;
	//rgba.y = 1;
	//rgba.z = 1;
	//rgba.w = 1;

	outRGBA = rgbaTest;
}