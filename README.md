# MOBA Game / League of DOTA Fortress 2

The foundation for a League of Legends-style game. In order to run this, you need to run both the server (in OnlineGameServer/dist) and the client (in OnlineGameClient/dist) at the same time.

### Features

- Basic 3D graphics, made using OpenGL (through LWJGL).
- Netcode using UDP – designed to work under poor network conditions, although I haven't actually tested this.
- Fast any-angle pathfinding, i.e. the game characters are moved by clicking on the point, and the server finding the shortest possible path to that point. Optimal paths are always found, and they are not constrained to a grid.
- Basic account system, with in-client registration and login.
- Lobby system – players can create matches or join others’ matches through a list. Also contains a chat system.
- Custom menu system with buttons, text boxes, lists etc.
- Custom text rendering system.
