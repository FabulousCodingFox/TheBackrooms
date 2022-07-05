#version 330 core
layout (location = 0) in vec3 inputPosition;
layout (location = 1) in vec2 inputTextureCoordinate;

out vec2 textureCoordinate;

void main()
{
    gl_Position = vec4(inputPosition, 1.0);
    textureCoordinate = inputTextureCoordinate;
}