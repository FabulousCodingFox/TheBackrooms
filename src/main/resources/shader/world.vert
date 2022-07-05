#version 330 core
layout (location = 0) in vec3 inputPosition;
layout (location = 1) in vec2 inputTextureCoordinate;
layout (location = 2) in float inputTextureNum;

out vec2 textureCoordinate;
out float textureNum;

uniform mat4 projection;

void main()
{
    gl_Position = projection * vec4(inputPosition, 1.0);
    textureCoordinate = inputTextureCoordinate;
    textureNum = inputTextureNum;
}