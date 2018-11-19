#version 150 core

uniform sampler2D u_Sampler;
uniform vec4 u_Color;
uniform int u_Filter;

in vec2 pass_TexCoord;

out vec4 out_Color;

void main()
{
    vec4 color = texture(u_Sampler, pass_TexCoord);
    
    out_Color = u_Color * color;
    
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