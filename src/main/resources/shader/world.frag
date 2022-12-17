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
    else{
        FragColor = vec4(1., 0., 1., 1.);
    }
}