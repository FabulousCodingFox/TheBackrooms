#version 460 core

out vec4 FragColor;

in vec2 uv;

uniform vec2 iResolution;
uniform float iTime;
uniform sampler2D screenTexture;

void main(){
    vec2 uv = gl_FragCoord.xy/iResolution.xy;
    FragColor = texture(screenTexture, vec2(uv.x, uv.y));
}