#version 120

//seldom changing or 1 time use data - non instanced:
attribute vec3 position; //mesh pos
attribute vec2 texCoord;
attribute vec3 vertexNormal; //unused
//seldom - instanced
attribute mat4 modelMatrix; //used to be modelViewMatrix, separate from view matrix
attribute vec4 rgba; //4th entry, alpha not used here, might as well leave vec4 unless more efficient to separate things to per float/attrib entries
//often changed data - instanced
attribute float alpha;
attribute float brightness;
//alpha?
//
//attribute vec4 rgbaTest;
//in vec2 texOffset;

varying vec2 outTexCoord;
varying float outBrightness;
varying vec4 outRGBA;

uniform mat4 modelViewMatrixCamera;
//uniform mat4 projectionMatrix;

//uniform int numCols;
//uniform int numRows;

void main()
{
	gl_Position = modelViewMatrixCamera * modelMatrix * vec4(position, 1.0);

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

	outRGBA = new vec4(rgba.x, rgba.y, rgba.z, alpha);
}