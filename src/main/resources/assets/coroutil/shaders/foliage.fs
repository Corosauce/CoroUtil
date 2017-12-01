#version 120

uniform sampler2D texture_sampler;
uniform int fogmode;
uniform int stipple[64];

varying vec2 outTexCoord;
varying float outBrightness;
varying vec4 outRGBA;
varying float outAlphaInt;

void main()
{

    //considering range 0.9 to 1.0 is quite costly, and provides minimal visual difference, this is more efficient
    //scratch that, stipple is crazy expensive in many scenarios, damn
    if (outRGBA.w < 0.9) {

        //int alphaInt = 255 - int(outRGBA.w * 255);

        ivec2 coord = ivec2(gl_FragCoord.xy - 0.5);

        if (stipple[int(mod(coord.x, 8) + mod(coord.y, 8) * 8)] < outAlphaInt - 1) {
            discard;
        }
    }

    float fogFactor = 0;
    if (fogmode == 0) {
        // Linear fog
        fogFactor = (gl_Fog.end - gl_FogFragCoord) * gl_Fog.scale;
    } else if (fogmode == 1) {
        // Exp fog
        fogFactor = exp(-gl_Fog.density * gl_FogFragCoord);
    }

    //0 = full fog
    //1 = no fog

    vec4 fragColor = texture2D(texture_sampler, outTexCoord);
	fragColor.x *= outRGBA.x * outBrightness;
	fragColor.y *= outRGBA.y * outBrightness;
	fragColor.z *= outRGBA.z * outBrightness;
	fragColor.w *= outRGBA.w;

	/*if (stipple[1] == 0) {
	    fragColor.w = 1;
	}*/





	if (outRGBA.w > 0) {
        fogFactor = clamp(fogFactor, 0.0, 1.0);
        gl_FragColor = mix(gl_Fog.color, fragColor, fogFactor);
        gl_FragColor.w = fragColor.w;
        //gl_FragColor = fragColor;
    } else {
        gl_FragColor = fragColor;
    }

    //gl_FragColor = fragColor;
}