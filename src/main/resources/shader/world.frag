#version 330 core
out vec4 FragColor;

in vec3 textureData;

uniform sampler2D WALL_TEXTURE;
uniform sampler2D WALL_TEXTURE_B;

bool isNear(float a, float b){
    return abs(a-b) < 0.01;
}

void main()
{
    if(isNear(textureData.z, 0.0)){
        FragColor = texture(WALL_TEXTURE, vec2(textureData.x, textureData.y));
    }else{
        FragColor = texture(WALL_TEXTURE_B, vec2(textureData.x, textureData.y));
    }
}