#version 150 core

uniform sampler2D u_Sampler;
uniform sampler2D u_MaskSampler;
uniform vec4 u_Color;
uniform vec4 u_FilterColor;
uniform int u_Filter;

in vec2 pass_TexCoord;

out vec4 out_Color;

void main()
{
    vec4 color = texture(u_Sampler, pass_TexCoord);
    vec4 mask = texture(u_MaskSampler, pass_TexCoord);
    
    if (u_Filter == 2)
    {
        //team color filter
        out_Color.rgb = u_Color.rgb * mix(u_FilterColor.rgb, color.rgb, mask.r);
        out_Color.a = u_Color.a * mix(u_FilterColor.a, color.a, mask.r) * mask.a;
    }
    else
    {
        out_Color = u_Color * mask * color;
    }
    
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