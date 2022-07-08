#version 330 core
layout (location = 0) in vec2 aPos;
layout (location = 1) in vec2 aTexCoords;

out vec2 uv;

void main()
{
    gl_Position = vec4(aPos.x*2., aPos.y*2., 0., 1.);
    uv = aTexCoords;
}