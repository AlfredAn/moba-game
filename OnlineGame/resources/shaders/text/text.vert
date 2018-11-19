#version 150 core

uniform mat4 u_Matrix;
uniform vec2 u_Scale;

in vec4 in_Position;
in vec2 in_TexCoord;

out vec2 pass_TexCoord;

void main()
{
    gl_Position = u_Matrix * in_Position;
    pass_TexCoord = in_TexCoord * u_Scale;
}