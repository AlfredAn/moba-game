#version 150 core

uniform mat4 u_Matrix;

in vec4 in_Position;
in vec2 in_TexCoord;
in vec3 in_Normal;

out vec2 pass_TexCoord;
out vec3 pass_Normal;

void main()
{
    gl_Position = u_Matrix * in_Position;
    pass_TexCoord = in_TexCoord;
    pass_Normal = in_Normal;
}