#version 460 core

out vec4 FragColor;

in vec2 uv;

uniform vec2 iResolution;
uniform float iTime;
uniform sampler2D screenTexture;

const float warp = .75; // simulate curvature of CRT monitor
const float scan = 0.75; // simulate darkness between scanlines

void main(){
    // squared distance from center
    vec2 uv = gl_FragCoord.xy/iResolution.xy;
    vec2 dc = abs(0.5-uv);
    dc *= dc;

    // warp the fragment coordinates
    uv.x -= 0.5; uv.x *= 1.0+(dc.y*(.3*warp)); uv.x += 0.5;
    uv.y -= 0.5; uv.y *= 1.0+(dc.x*(.4*warp)); uv.y += 0.5;

    // sample inside boundaries, otherwise set to black
    if (uv.y > 1.0 || uv.x < 0.0 || uv.x > 1.0 || uv.y < 0.0){
        FragColor = vec4(0.0, 0.0, 0.0, 1.0);
    }else{
        // determine if we are drawing in a scanline
        float apply = abs(sin(gl_FragCoord.y+sin(iTime))*0.5*scan);

        // Chromatic aberration
        vec2 chrom = vec2(mod(abs(sin(iTime)*2.0), 0.01), 0.0); // Strength of the color shift
        vec3 col;
        col.r       = texture(screenTexture, vec2(uv.x+chrom.x, uv.y)).r;
        col.g       = texture(screenTexture, vec2(uv.x, uv.y)).g;
        col.b       = texture(screenTexture, vec2(uv.x-chrom.x, uv.y)).b;


        // sample the texture
        FragColor = vec4(mix(col,vec3(0.0),apply),1.0);
    }
}