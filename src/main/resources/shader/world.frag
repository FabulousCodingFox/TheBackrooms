#version 330 core
out vec4 FragColor;

in vec2 textureCoordinate;

uniform sampler2D WALL_TEXTURE;
uniform sampler2D WALL_TEXTURE_B;

void main()
{
    FragColor = texture(WALL_TEXTURE, textureCoordinate);
}