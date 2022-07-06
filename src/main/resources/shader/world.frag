#version 330 core
out vec4 FragColor;

in vec3 textureData;

uniform sampler2D WALL_TEXTURE;
uniform sampler2D FLOOR_TEXTURE;
uniform sampler2D CEILING_TEXTURE;

bool isNear(float a, float b){
    return abs(a-b) < .01;
}

void main()
{
    if(isNear(textureData.z, .0)){
        FragColor = texture(WALL_TEXTURE, vec2(textureData.x, textureData.y));
    }
    else if(isNear(textureData.z, .1)){
        FragColor = texture(FLOOR_TEXTURE, vec2(textureData.x, textureData.y));
    }
    else if(isNear(textureData.z, .2)){
        FragColor = texture(CEILING_TEXTURE, vec2(textureData.x, textureData.y));
    }
    else{
        FragColor = vec4(1., 0., 1., 1.);
    }
}