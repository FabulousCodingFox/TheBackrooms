#version 330 core
out vec4 FragColor;

in vec2 textureCoordinate;
in float textureNum;

uniform sampler2D WALL_TEXTURE;
uniform sampler2D WALL_TEXTURE_B;

void main()
{
    if(textureNum == 0.0f)
    {
        FragColor = texture(WALL_TEXTURE, textureCoordinate);
    }
    else
    {
        FragColor = texture(WALL_TEXTURE_B, textureCoordinate);
    }
}