#version 150 core

uniform mat4 u_Matrix;

in vec4 in_Position;
in vec3 in_Normal;

out vec3 pass_Normal;

void main()
{
    gl_Position = u_Matrix * in_Position;
    pass_Normal = in_Normal;
}