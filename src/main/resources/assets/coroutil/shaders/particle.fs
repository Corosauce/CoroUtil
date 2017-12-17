#version 120
#extension GL_EXT_gpu_shader4 : enable

uniform sampler2D texture_sampler;
uniform int fogmode;

varying vec2 outTexCoord;
flat varying float outBrightness;
varying vec4 outRGBA;

void main()
{

    float fogFactor = 0;


    if (fogmode == 0) {
        // Linear fog
        fogFactor = (gl_Fog.end - gl_FogFragCoord) * gl_Fog.scale;
    } else if (fogmode == 1) {
        // Exp fog
        fogFactor = exp(-gl_Fog.density * gl_FogFragCoord);
    }

    int lightMap = int(outBrightness);
    float r = float((lightMap >> 16) & 255) / 255.0;
    float g = float((lightMap >> 8) & 255) / 255.0;
    float b = float(lightMap & 255) / 255.0;

    vec4 fragColor = texture2D(texture_sampler, outTexCoord);
	fragColor.x *= outRGBA.x * r;
	fragColor.y *= outRGBA.y * g;
	fragColor.z *= outRGBA.z * b;
	fragColor.w *= outRGBA.w;

    if (outRGBA.w > 0) {
        fogFactor = clamp(fogFactor, 0.0, 1.0);
        gl_FragColor = mix(gl_Fog.color, fragColor, fogFactor);
        gl_FragColor.w = fragColor.w;
        //gl_FragColor = fragColor;
    } else {
        gl_FragColor = fragColor;
    }

    //gl_FragColor.w = 0.1;

    //gl_FragColor = fragColor;
}