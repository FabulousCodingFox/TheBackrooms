#version 330 core
layout (location = 0) in vec3 inputPosition;
layout (location = 1) in vec3 inputTextureData;

out vec3 textureData;

uniform mat4 model;
uniform mat4 view;
uniform mat4 projection;

void main()
{
    gl_Position = projection * view * model * vec4(inputPosition, 1.0);
    textureData = inputTextureData;
}