#version 150 core

uniform sampler2D u_Sampler;
uniform vec4 u_Color;

in vec2 pass_TexCoord;

out vec4 out_Color;

void main()
{
    float opacity = texture(u_Sampler, pass_TexCoord).r;
    
    out_Color.rgb = u_Color.rgb;
    out_Color.a = u_Color.a * opacity;
}