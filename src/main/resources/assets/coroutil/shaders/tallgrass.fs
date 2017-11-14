#version 120

uniform sampler2D texture_sampler;

varying vec2 outTexCoord;
varying float outBrightness;
varying vec4 outRGBA;

void main()
{
    vec4 fragColor = texture2D(texture_sampler, outTexCoord);
	fragColor.x *= outRGBA.x * outBrightness;
	fragColor.y *= outRGBA.y * outBrightness;
	fragColor.z *= outRGBA.z * outBrightness;
	fragColor.w *= outRGBA.w;

    gl_FragColor = fragColor;
}