#version 150 core

uniform vec4 u_Color;

in vec3 pass_Normal;

out vec4 out_Color;

const vec3 sunAngle = normalize(vec3(2, 1, 3));
const float ambientFactor = 0.375;
const float directFactor = 1 - ambientFactor;

void main()
{
    out_Color = u_Color;
    out_Color.rgb *= max(dot(sunAngle, pass_Normal), 0) * directFactor + ambientFactor;
}