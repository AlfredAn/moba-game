#version 150 core

uniform mat4 u_Matrix;

in vec4 in_Position;

void main()
{
    gl_Position = u_Matrix * in_Position;
}