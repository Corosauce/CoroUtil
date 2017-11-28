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

varying vec2 outTexCoord;
varying float outBrightness;
varying vec4 outRGBA;

uniform mat4 modelViewMatrixCamera;

uniform int time;
uniform float partialTick;
uniform float windDir;
uniform float windSpeed;

vec3 computeCorner(vec3 sway, vec3 angle, vec3 center) {
    return center + normalize(cross(sway, angle)) * 0.5;
}

void main()
{

    float radian = 0.0174533;
    int swayLag = 20;
    float index = meta.x;
    float animationID = meta.y;
    float heightIndex = meta.z;
    float rotation = rgba.w;

    float timeSmooth = (time-1) + partialTick;
    timeSmooth += index;

    float variance = 0.6;

    vec3 pos = vec3(0, 0, 0);
    vec3 angle = vec3(-1, 0, 1);
    if (rotation == 1) {
        angle = vec3(1, 0, 1);
    }

    //more performant but less accurate algorithm, use unless crazy mesh warping needed
    vec3 baseHeight = vec3(0, heightIndex-1, 0);
    vec3 baseHeight2 = vec3(0, heightIndex, 0);

    int timeModBottom = int(mod(((timeSmooth + ((heightIndex - 1 + 1) * swayLag)) * 2) + rotation, 360));
    vec3 swayBottom = vec3(sin(timeModBottom * radian) * variance, 1, cos(timeModBottom * radian) * variance);
    vec3 prevSway = swayBottom;
    vec3 bottom = baseHeight + swayBottom;

    int timeModTop = int(mod(((timeSmooth + ((heightIndex + 1) * swayLag)) * 2) + rotation, 360));
    vec3 sway = vec3(sin(timeModTop * radian) * variance, 1, cos(timeModTop * radian) * variance);
    vec3 top = baseHeight2 + sway;
    if (heightIndex == 0) {
        bottom = vec3(0, 0, 0);
        prevSway = vec3(0, 1, 0);
    }

    //more accurate but more expensive loop
    /*

    vec3 top = vec3(0, 0, 0);
    vec3 bottom = vec3(0, 0, 0);
    vec3 bottomNext = bottom;
    //verify
    vec3 sway = vec3(0, 0, 0);

    for (int i = 0; i <= heightIndex; i++) {
        prevSway = sway;
        timeMod = int(mod(((timeSmooth + ((i + 1) * swayLag)) * 2) + rotation, 360));
        sway = vec3(sin(timeMod * radian) * variance, 1, cos(timeMod * radian) * variance);
        sway = normalize(sway);

        top = bottomNext + sway;

        bottom = bottomNext;
        bottomNext = top;
    }*/

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

    //lazy, cheap dist to camera
    gl_FogFragCoord = abs(gl_Position.z);

	outTexCoord = texCoord;
	outBrightness = alphaBrightness.y;

	outRGBA = vec4(rgba.x, rgba.y, rgba.z, alphaBrightness.x);
}