#version 150 core

uniform sampler2D u_Sampler;
uniform vec4 u_Color;
uniform int u_Filter;

in vec2 pass_TexCoord;
in vec3 pass_Normal;

out vec4 out_Color;

const vec3 sunAngle = normalize(vec3(2, 1, 3));
const float ambientFactor = 0.375;
const float directFactor = 1 - ambientFactor;

void main()
{
    vec4 color = texture(u_Sampler, pass_TexCoord);
    
    out_Color = u_Color * color;
    out_Color.rgb *= max(dot(sunAngle, pass_Normal), 0) * directFactor + ambientFactor;
    
    switch (u_Filter)
    {
        case 0:
            //no filter
            break;
        case 1:
            //grayscale filter
            float intensity = dot(out_Color.rgb, vec3(0.299, 0.587, 0.114));
            out_Color.rgb = vec3(intensity, intensity, intensity);
            break;
    }
}