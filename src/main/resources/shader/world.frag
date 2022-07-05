#version 330 core
out vec4 FragColor;

in vec2 inputTextureCoordinate;

uniform sampler2D WALL_TEXTURE;

void main()
{
    FragColor = texture(WALL_TEXTURE, inputTextureCoordinate);
}