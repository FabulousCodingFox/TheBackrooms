#version 330 core
out vec4 FragColor;

in vec3 textureData;
in vec3 positionData;
in float aoData;

uniform sampler2D WALL_TEXTURE;
uniform sampler2D FLOOR_TEXTURE;
uniform sampler2D CEILING_TEXTURE;

uniform vec3 camPos;
uniform int renderDistance;
uniform bool lightingEnabled;

uniform vec2 iResolution;
uniform float iTime;

bool isNear(float a, float b){
    return abs(a-b) < .01;
}

void main()
{
    float camDistData = aoData;
    if(lightingEnabled) camDistData = camDistData - (length(positionData - camPos) / renderDistance);

    if(isNear(textureData.z, .0)){
        FragColor = texture(WALL_TEXTURE, vec2(textureData.x, textureData.y)) * camDistData;
    }
    else if(isNear(textureData.z, .1)){
        FragColor = texture(FLOOR_TEXTURE, vec2(textureData.x, textureData.y)) * camDistData;
    }
    else if(isNear(textureData.z, .2)){
        FragColor = texture(CEILING_TEXTURE, vec2(textureData.x, textureData.y)) * camDistData;
    }
    else if(isNear(textureData.z, .3)){
        // https://www.shadertoy.com/view/4tjSDt

        float s = 0.0, v = 0.0;
        vec2 uv = (gl_FragCoord.xy / iResolution.xy) * 2.0 - 1.;
        float time = (iTime-2.0)*58.0;
        vec3 col = vec3(0);
        vec3 init = vec3(sin(time * .0032)*.3, .35 - cos(time * .005)*.3, time * 0.002);
        for (int r = 0; r < 100; r++)
        {
            vec3 p = init + s * vec3(uv, 0.05);
            p.z = fract(p.z);
            // Thanks to Kali's little chaotic loop...
            for (int i=0; i < 10; i++)	p = abs(p * 2.04) / dot(p, p) - .9;
            v += pow(dot(p, p), .7) * .06;
            col +=  vec3(v * 0.2+.4, 12.-s*2., .1 + v * 1.) * v * 0.00003;
            s += .025;
        }
        FragColor = vec4(clamp(col, 0.0, 1.0), 1.0) * camDistData;
    }
    else{
        FragColor = vec4(1., 0., 1., 1.);
    }
}