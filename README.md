## Description of project
For this project the client has asked to deliver a quiz game with the goal of raising awareness around energy consumption. The client also put forward requirements that the application had to abide by.

### Requirements
- The application must be client/server based and predominantly follow a restful approach.
- Data must be stored in a relational database.


## Group members

| Profile Picture | Name | Email |
|---|---|---|
| ![](https://secure.gravatar.com/avatar/f5f23c5dd0d5796f6c65be150eb2a9f1?s=800&d=identicon&size=50) | Alperen Guncan | A.Guncan@student.tudelft.nl |
| ![](https://secure.gravatar.com/avatar/3e621380f108ec846485d3d2410fbb0b?s=80&d=identicon&size=50) | Beni Selyem | B.Selyem@student.tudelft.nl |
| ![](https://secure.gravatar.com/avatar/b1deaa5bdd4de16ca5f88bc54c89e9bc?s=800&d=identicon&size=50) | Razvan Nistor | R.I.Nistor@student.tudelft.nl |
| ![](https://secure.gravatar.com/avatar/0408829ba64f29bb1f00e5934d5b6968?s=800&d=identicon&size=50) | Yongcheng Huang | Y.Huang-51@student.tudelft.nl |
| ![](https://secure.gravatar.com/avatar/d05ffb7f3e511625b5c8bbf90f857b00?s=800&d=identicon&size=50) | Faizel Mangroe | F.Mangroe@student.tudelft.nl |
| ![](https://secure.gravatar.com/avatar/325ee4730af88cf34054be3ab5aec337?s=192&d=identicon&size=50) | Rithik Appachi Senthilkumar | R.AppachiSenthilkumar@student.tudelft.nl |

## How to run it

### Requirements
Before running the application the user must make sure that their devices have the following requirements:
- The device must have Java SDK 17 installed.
- The device must have Javafx-sdk-17.0.2 or a later version installed.

### Starting the server & client
1. Run the server through terminal.
    - Terminal: Change directory to the folder in which you saved the application and execute `gradlew bootrun`.
2. Run the client by executing `gradlew run` in a separate terminal that also points to the folder in which you saved the application.

### Accessing the admin panel
There are two approaches for accessing the admin panel. Accessing the admin panel through a browser or through the client's *edit activities* button located on the splash screen.

#### Through browser
1. Open a browser of your choosing.
2. Navigate to `http://localhost:8080/` for the devices running on the same network as the server and for players playing on different network they must replace the "localhost" part with the IP of the server they are trying to connect to. 

#### Through the client
1. On the splash screen one must insert the IP of the server they are trying to connect to into to the text box under their username.
2. Then the user can click on the *edit activities* button located on the bottom right.
3. Exiting this screen can be done by clicking on the *back* button.

### Adding activities
Before a user can play the game the activity database needs to be filled. If the database is empty and the user starts a game no questions will be loaded. There are several ways to add activities to the activity database namely, Adding an activity manually, Adding one activity through inserting a JSON format and Bulk adding activities through a JSON file with the corresponding format. An addition has to abide by several conditions.

#### Conditions
- A title must have at least three words and must be unique.
- The supported file types for images are JPG, JPEG and PNG.
- If the user provides a relative link for the image path they must use the add images button to add the images through a zip file with the hierarchy described in the relative path (i.e. relative path: 38/clock.png -> Zip hierarchy: zipfile.zip > 38 > clock.png). If the zip does not abide by this format the image will not be found.
- If the user chooses to add an image through the URL of the link the user must provide a valid URL ending in "xxx.filetype".

#### Manually adding one activity
1. Click on the *add activity* button.
2. Click on the *add one activity* button.
3. Fill in the corresponding text fields that show up while abiding to the conditions mentioned above.
4. Click on *send*.

#### Adding one activity through JSON format
1. Click on the *add activity* button.
2. Click on the *add activities* (JSON) button.
3. Fill in the corresponding text fields with the JSON format for an activity.
4. Click on *send*
    
#### Adding multiple activities through JSON format
1. Click on the *add activity* button.
2. Click on the *add activities (JSON-formatted file)* button.
3. Click on the *choose file* button.
4. Select a JSON formatted file from your hard drive.
5. Click on *send*.

#### Adding images through zip file
1. Click on the *add activity* button.
2. Click on the *add images* button.
3. Click on the *choose file* button.
4. Select a zip file that abides by the conditions mentioned above from your harddrive.
5. Click on *send*.

### Removing an activity
1. Click on the *remove activity* button.
2. Insert the corresponding activity id in the text field
3. Click on *send*.

### Edit an activity
1. Click on the *edit activity* button.
2. Enter the id of the activity you want to edit.
3. Fill in the corresponding text fields while abiding by the conditions mentioned in adding activities.
4. Click on *send*.

### Refresh the database
If your changes do not occur immediately try refreshing the database by clicking on the *refresh database* button.

### Delete the database
If you want to remove all activities and images that correspond with the image paths of each activity, click on the *delete database* button.

### Accessing the tutorial screen
To receive a game explanation click on the *question mark icon* on the bottom right. You can navigate through the different panels by clicking on the arrow keys on the sides. If you want to leave the tutorial click on the *back* button.

### Playing a game
If a user wants to play any game at all they first have to enter a valid username. A username is valid if it only contains letters and numbers and is not a duplicate of a username that is currently used in a session. The second thing the user needs to do is enter a valid server IP. By default it is "http://localhost:8080/". However if the user is playing from a different device than the device on which the server runs on they must specify the IP of the server's device.

#### Starting a singleplayer game
After initializing the username and the server connection the user can click on the *singleplayer* button to be sent to the gamemode screen. Where they can pick one of the modes by clicking on the corresponding buttons of the modes. For the default singleplayer game someone can adjust the amount of questions they get. For the survival gamemode the user can adjust the amount of lives they have and for the time attack mode the initial time can be changed.

#### Starting a multiplayer game
After initializing the username and the server connection the user can click on the *multiplayer* button to be sent to the selection screen. On the selection screen a player will see all live session in a list and they can pick a session with status: waiting area or play again to join. For each session the id, status and amount of players is displayed. If a user has no preference they can click on *quick join* to be sent to a random available session. If the user wants to join a specific session they can do that by either inserting the id in the text field and clicking on *join* or by clicking on the session in the list and clicking on *play*. If a player wants to host a fresh session they can click on *host room*. In the waiting area when everyone has clicked on the *ready* button a game will start.

#### End of multiplayer game
When the multiplayer game ends the players are sent to the end of game screen where they can either choose to leave by clicking on the *leave* button or play again by toggling the *play again* button. At the end of the timer a new game starts for the players that have toggled play again if there are 2 or more people who toggled the button. The others are sent to the splash screen.

### Viewing the leaderboard
The user can view the scores of all players in the leaderboard by clicking on the *leaderboard* button. By default the default singleplayer leaderboard will be loaded, unless the player has already entered the leaderboard once and left the board on a different panel. Then that panel will be loaded. By clicking on the *back* button the player gets reverted back to the splash screen.

## How to contribute to it
If you want to contribute, you can open an issue relating to a feature, refactor or bug, and it can be worked on. You can clone the repository and work on a branch you created, then open a merge request. We point you to the required template documents (issue/merge request) and the code of conduct located in the docs folder.

## Copyright / License (opt.)
