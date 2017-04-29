# EE422C-ChatRoom
## Contributors
Samuel Zhang (shz96)
Grace Zhuang (gpz68)

## Files
### Java Files

#### ClientMain.java
This class implements the client interface using JavaFX and also handles direct communication with the server. ClientMain is wrapped up into a jar file called Client which can run independently

#### ServerMain.java
This class creates a server that handles all user input from Clients. Server handles all chat rooms, username/password implementations, and sending messages.

#### ClientObserver.java
This class extends PrintWriter and is used to enforce the Observer/Observable framework required for this assignment.

### Executable Files
#### Client.jar
#### Server.jar

## Testing
We exclusively tested these applications on Windows. The applications should work on Linux and Windows, however we are unsure about MAC because the application creates a directory to store username/password information.

## GIT URL
https://github.com/grace-zhuang/EE422C-ChatRoom