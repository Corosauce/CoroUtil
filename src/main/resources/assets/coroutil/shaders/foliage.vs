#version 120

in int gl_VertexID;
in int gl_InstanceID;
//seldom changing or 1 time use data - non instanced:
attribute vec3 position; //mesh pos
attribute vec2 texCoord;
attribute vec3 vertexNormal; //unused
//seldom - instanced
attribute mat4 modelMatrix; //used to be modelViewMatrix, separate from view matrix
attribute vec4 rgba; //4th entry, alpha not used here, might as well leave vec4 unless more efficient to separate things to per float/attrib entries
attribute vec3 meta;
//often changed data - instanced
attribute vec2 alphaBrightness;
//attribute float alpha;
//attribute float brightness;
//alpha?
//
//attribute vec4 rgbaTest;
//in vec2 texOffset;

varying vec2 outTexCoord;
varying float outBrightness;
varying vec4 outRGBA;

//uniform mat4 projectionMatrix;

uniform mat4 modelViewMatrixCamera;
//uniform mat4 modelViewMatrixClassic;

uniform int time;
uniform float partialTick;
uniform float windDir;
uniform float windSpeed;


//uniform int numCols;
//uniform int numRows;

mat4 rotationMatrix(vec3 axis, float angle) {
    axis = normalize(axis);
    float s = sin(angle);
    float c = cos(angle);
    float oc = 1.0 - c;

    return mat4(oc * axis.x * axis.x + c,           oc * axis.x * axis.y - axis.z * s,  oc * axis.z * axis.x + axis.y * s,  0.0,
                oc * axis.x * axis.y + axis.z * s,  oc * axis.y * axis.y + c,           oc * axis.y * axis.z - axis.x * s,  0.0,
                oc * axis.z * axis.x - axis.y * s,  oc * axis.y * axis.z + axis.x * s,  oc * axis.z * axis.z + c,           0.0,
                0.0,                                0.0,                                0.0,                                1.0);
}

vec3 computeCorner(vec3 sway, vec3 angle, vec3 center) {
    vec3 cp = cross(sway, angle);
    cp = normalize(cp);
    cp = cp * 0.5;
    vec3 pos = center.xyz;
    pos += cp;
    return pos;
}

void main()
{

    float radian = 0.0174533;
    int swayLag = 7;
    float index = meta.x;
    float animationID = meta.y;
    float heightIndex = meta.z;
    float rotation = rgba.w;

    if (heightIndex >= 0 && (gl_VertexID == 0 || gl_VertexID == 3)) {
        //heightIndex += 1;
    }

    float timeSmooth = (time-1) + partialTick;
    //int timeMod = int(mod((timeSmooth + gl_InstanceID * 3) * 2, 360));
    //int timeMod = int(mod((timeSmooth + index * 3) * 10, 360));
    int timeMod = int(mod(((timeSmooth + ((/*heightIndex*/0 + 1) * swayLag)) * 2) + rotation, 360));

    float variance = 0.3;//windSpeed * 0.5;

    vec3 sway = vec3(sin(timeMod * radian) * variance, 1, sin(timeMod * radian) * variance);
    //temp
    //sway = vec3(0, 1, 0);

    sway = normalize(sway);
    vec3 prevSway = vec3(0, 1, 0);
    sway = prevSway;


    //for now assume bottom and top will be the correct values for their height, fix that after
    //verified that mesh vert order = gl_VertexID order

    //drawn in order of a U shape starting top left
    vec3 pos = vec3(0, 0, 0);
    vec3 angle = vec3(-1, 0, 1);
    if (rotation == 1) {
        angle = vec3(1, 0, 1);
    }

    /*if (heightIndex == 0) {
        top = top0;
        bottom = bottom0;
    } else if (heightIndex == 1) {
        top = top1;
        bottom = bottom1;
    } else {

    }*/

    vec3 top = vec3(0, 0, 0);
    vec3 bottom = vec3(0, 0, 0);
    vec3 bottomNext = bottom;

    //vec3 swayNext = sway;

    for (int i = 0; i <= heightIndex; i++) {
        prevSway = sway;
        timeMod = int(mod(((timeSmooth + ((/*heightIndex*/i + 1) * swayLag)) * 2) + rotation, 360));
        sway = vec3(sin(timeMod * radian) * variance, 1, sin(timeMod * radian) * variance);
        sway = normalize(sway);

        top = bottomNext + sway;

        bottom = bottomNext;
        bottomNext = top;

        //prevSway = oldSway;
    }

    if (gl_VertexID == 0) {
        pos = computeCorner(sway, angle, top);
    } else if (gl_VertexID == 1) {
        pos = computeCorner(prevSway, angle, bottom);
    } else if (gl_VertexID == 2) {
        angle = angle * -1;
        pos = computeCorner(prevSway, angle, bottom);
    } else if (gl_VertexID == 3) {
        angle = angle * -1;
        pos = computeCorner(sway, angle, top);
    }

    gl_Position = modelViewMatrixCamera * modelMatrix * vec4(pos.x, pos.y, pos.z, 1.0);

    //vec4
    //gl_Position

    //from example:
    //vec4 eyePos = gl_ModelViewMatrix * gl_Vertex;
    //gl_FogFragCoord = abs(eyePos.z/eyePos.w);

    //vec4 eyePos = gl_ModelViewMatrix * vec4(position, 1.0);
    //vec4 eyePos = modelViewMatrixCamera * modelMatrix * vec4(position, 1.0);
    //gl_FogFragCoord = abs(eyePos.z/eyePos.w);

    //this is for distance to camera
    //gl_FogFragCoord = alpha;

    //my math is bad and i should feel bad... but this works
    gl_FogFragCoord = abs(gl_Position.z);
    //gl_FogFragCoord = 6;

	// Support for texture atlas, update texture coordinates
    //float x = (texCoord.x / numCols + texOffset.x);
    //float y = (texCoord.y / numRows + texOffset.y);

	outTexCoord = texCoord;
	outBrightness = alphaBrightness.y;

	//temp
	//rgba.x = 1;
	//rgba.y = 1;
	//rgba.z = 1;
	//rgba.w = 1;

	outRGBA = vec4(rgba.x, rgba.y, rgba.z, alphaBrightness.x);
}