# Manual Testing List


## Splash screen

- Enter a name that does *NOT* contain alphanumerical characters.
    * **Expected result**: `Username should be non-empty, and only contain letters and/or numbers.` error message appears on the screen.
    
- Try empty name.
    * **Expected result**: `Username should be non-empty, and only contain letters and/or numbers.` error message appears on the screen.

- Enter an invalid IP address.
    * **Expected result**: `Could not connect` error message appears on the screen.

- Press the close button on the title bar.
    * **Expected result**: `Confirm close` alert pops up.

- Press the close button on the title bar. Now press `Cancel` on the alert.
    * **Expected result**: `Confirm close` alert is closed, and the game is resumed.

- Press the close button on the title bar. Now press `OK` on the alert.
    * **Expected result**: Both the alert and the game are closed.

- Without specifying an IP, try joining a singleplayer game, a multiplayer game, and the leaderboard.
    * **Expected result**: The next screen appears and the client connects to the server hosted on `localhost:8080`.

- Enter a valid name and valid ip (or leave default if the server is on the same machine) and press on `Singleplayer`.
    * **Expected result**: The `Singleplayer gamemodes` screen will appear.

- Enter a valid name and valid ip (or leave default if the server is on the same machine) and press on `Multiplayer`.
    * **Expected result**: The `Room Selection` screen will appear.

- Enter a valid name and valid ip (or leave default if the server is on the same machine) and press on `Leaderboard`.
    * **Expected result**: The `Leaderboard` screen will appear.

- Enter a game with a chosen name, and, on a second client, try entering a game with the same name. This applies to both single and multiplayer.
    * **Expected result**: `Username already exists. Change to continue` error message appears on the screen.

- On another device, specify the IP of the server. Try joining a singleplayer game, a multiplayer game, and the leaderboard.
    * **Expected result**: The next screen appears and the client connects to the server hosted on the machine with the specified IP.

- Enter a single or multi game with a valid name. Close the app and reopen it.
    * **Expected result**: The name is autofilled with the name entered previously.

- Press the `?` button in the bottom right.
    * **Expected result**: The tutorial screen appears.
    
- Press the `Edit activities` button in the bottom right.
    * **Expected result**: The Admin Panel screen appears.

- Hover over the `Singleplayer`, `Leaderboard` and `Multiplayer` buttons.
    * **Expected result**: The animation will start on the button that is being hovered and the plug connects to the battery, meanwhile the batter is filling.

## Leaderboard

- From the Splash screen, enter the leaderboard.
    * **Expected result**: The screen changes and the `Singleplayer` leaderboard appears. The title changes to `Leaderboard - Singleplayer` and only the players from singleplayer are shown.

- Press the `Multiplayer` button.
    * **Expected result**: The title changes to `Leaderboard - Multiplayer` and the leaderboard is updated to show only the players from multiplayer.

- Press the `Survival` button.
    * **Expected result**: The title changes to `Leaderboard - Survival` and the leaderboard is updated to show only the players from survival.

- Press the `Time Attack` button.
    * **Expected result**: The title changes to `Leaderboard - Time Attack` and the leaderboard is updated to show only the players from time attack.

- Press the `Singleplayer` button.
    * **Expected result**: The title changes to `Leaderboard - Singleplayer` and the leaderboard is updated to show only the players from singleplayer.

- Add more than 10 players to a single gamemode and scroll through the leaderboard.
    * **Expected result**: All players are shown, each having a rank represented by a battery that is less *charged* by going down through the list.

- Press `Back` button.
    * **Expected result**: The screen changes to the Splash screen.

## Singleplayer
Start from the `Singleplayer Gamemode` screen...

- Press `Default` and play a full game.
    * **Expected result**: After 20 rounds, the game ends, the player and the score are added to the leaderboard, and the player is returned to the splash screen with an alert that shows his score.

- Press `Survival` and play a full game.
    * **Expected result**: After the 3 lives are gone, the game ends, the player and the score are added to the leaderboard, and the player is returned to the splash screen with an alert that shows his score.

- Press `Time Attack` and play a full game.
    * **Expected result**: After 60 seconds, the game ends, the player and the score are added to the leaderboard, and the player is returned to the splash screen with an alert that shows his score.

- Play another game for each gamemode individually and gain more points than in the last game.
    * **Expected result**: The score is updated on the leaderboard.
    
- Play another game for each gamemode individually and gain fewer points than in the last game.
    * **Expected result**: The score is *NOT* updated on the leaderboard.

- Slide the Questions slider to any other number than the default 20.
    * **Expected result**: Alert message will appear that says `Note: Results from games with changed default settings are not reflected on leaderboards!`.

- Slide the Lives slider to any other number than the default 3.
    * **Expected result**: Alert message will appear that says `Note: Results from games with changed default settings are not reflected on leaderboards!`.

- Slide the Timer slider to any other number than the default 60.
    * **Expected result**: Alert message will appear that says `Note: Results from games with changed default settings are not reflected on leaderboards!`.

-  Slide the Questions slider to any number *N* different than the default 20 and press `Default`.
    * **Expected result**: The game will start and will end after *N* rounds and the result is not stored on the leaderboard.

-  Slide the Lives slider to any number *N* different than the default 3 and press `Survival`.
    * **Expected result**: The game will start with *N* lives and will end after the *N* lives are lost. The result is not stored on the leaderboard.

-  Slide the Timer slider to any number *N* different than the default 60 and press `Time attack`.
    * **Expected result**: The game will start and will end after *N* seconds and the result is not stored on the leaderboard.

-  Press the `Go back` button.
    * **Expected result**: Return to the Splash Screen and allows another player to pick the name that was just freed.

## Room Selection

- Press `Multiplayer` on the Splash screen.
    * **Expected result**: The screen changes to `Room Selection` screen and all the sessions that exist are shown in the table.

- Press `Quick Join`, with *NO* rooms displayed.
    * **Expected result**: The player creates a new room.

- Press `Quick Join`, when there are rooms available.
    * **Expected result**: The player is sent to a random available room.
    * Note: Repeat this multiple times, to ensure the randomization works.

- Press `Host room`.
    * **Expected result**: The player creates a new room.

- Select a room from the list that is available and press `Play`.
    * **Expected result**: The player enters the specified room.

- Enter the ID of a valid available room and press `Join`.
    * **Expected result**: The player enters the specified room.

- Select a room from the list that is *NOT* available and press `Play`.
    * **Expected result**: The player is alerted with the following messages: `The selected game is still going on`, `You can wait for it to get over, or join a new game`.

- Enter the ID of a valid room, but *NOT* available, and press `Join`.
    * **Expected result**: The player is alerted with the following messages: `The selected game is still going on`, `You can wait for it to get over, or join a new game`.

- Enter the ID of a room that is *NOT* valid and press `Join`.
    * **Expected result**: The player is alerted with the following messages: `You have entered an invalid game session ID`, `Please enter a valid session ID to continue`.

- Enter a game that is in `Play Again`.
    * **Expected result**: The timer is reset for everyone in the session and the number of total players gets increased by one.

- Press `Back` button.
    * **Expected result**: The player is sent to the Splash screen.

* Note: Available session means a room that has the status either `Waiting Area`, or `Play Again`.

## Waiting area
Enter a valid available room...

- Enter a valid available room.
    * **Expected result**: The list of all players is seen and on the right, you can see `the number of ready players` / ` number of players in the lobby`.

- Other player enters the same room.
    * **Expected result**: The list and number of players in the lobby get updated, but the ready number stays the same.

- Press `Ready` button.
    * **Expected result**: The number of players ready increases by one.

- Press `Not Ready` button.
    * **Expected result**: The number of players ready decreases by one.

- All players press `Ready` button.
    * **Expected result**: The game starts.

- Press `Back` button.
    * **Expected result**: The player is sent to the Splash screen and the number of ready players (if it is the case)  and the total number of players decreases by one for all other players in the session.

- Press `Close` button and press `Ok` when alerted.
    * **Expected result**: The app closes and the number of ready players (if it is the case) and the total number of players decreases by one for all other players in the session.

## Multiplayer
Start after the waiting room...

- Complete 10 rounds.
    * **Expected result**: The midgame leaderboard is shown.

- Complete 20 rounds.
    * **Expected result**: Fully complete a game, the player and the score are added to the leaderboard, and the player is shown the podium and the end game.

- Play another Multiplayer game and gain more points than in the last game.
    * **Expected result**: The score is updated on the leaderboard.
    
- Play another Multiplayer game and gain fewer points than in the last game.
    * **Expected result**: The score is *NOT* updated on the leaderboard.

- Send all 3 kinds of emojis as fast or as slowly as possible.
    * **Expected result**: For each click, the respective emoji appears on the screen.

- Gain points during a round.
    * **Expected result**: The score is updated on the displayed leaderboard.

- All players answer a question before the timer runs out.
    * **Expected result**: The timer stops, the correct answer is shown and the game shows the next round (or mid/end game leaderboard if this is the case).

- *NOT* all players answer a question before the timer runs out.
    * **Expected result**: Correct answer is shown and the game continues.

## Podium screen

- Get to the podium screen.
    * **Expected result**: Top 3 players are shown with their scores.
    * Note if not enough people are in the lobby, the podium has empty spaces or no podium is shown if there is only one person in the game.

- Wait 10 seconds.
    * **Expected result**: The end game appears.

## End game
Note: This refers to the screen that appears after a multiplayer game and after the podium.

- Reach this screen.
    * **Expected result**: A leaderboard appears with all players and their scores for this game.

- Multiple clients press `Play again`.
    * **Expected result**: The number of players that want to play again is displayed and after the timer runs out, a new game starts with these players.

- All players in the game press `Play again`.
    * **Expected result**: Same as above, plus the timer runs faster.

- All players in the game press `Play again`. And one person clicks `Play again`.
    * **Expected result**: The number of players ready decreases by one. The timer is slowed back down.

- All players in the game press `Play again`. And one person closes the app.
    * **Expected result**: Both the number of players ready and the number of total players decreases by one and the timer still runs fast.

- One player presses `Leave`.
    * **Expected result**: That player is sent to the splash screen and the number of total players gets decreased by one. Also, the number of ready players gets decreased by one if he was marked as ready.

- Let the timer run out, without pressing `Play again`.
    * **Expected result**: The player is returned to the splash screen.

- Only one player presses `Play again`.
    * **Expected result**: The player is returned to the splash screen and the following alert appears: `There are too few people to play again:`, `Please join a fresh game to play with more people!`.

- Send all 3 kinds of emojis as fast or as slowly as possible.
    * **Expected result**: For each click, the respective emoji appears on the screen.

## Questions & Points

- Submit an answer.
    * **Expected result**: No more answers can be selected.

- Timer runs out, or all players in the session submit an answer.
    * **Expected result**: The correct answer is shown to all players.

### Multiple choice, Comparison, and Equivalence type questions

- One of the previous types of questions appears.
    * **Expected result**: A list of 3 or 4 (depends on the type of question) appears, that all can be selected by the player.

- Select the correct answer.
    * **Expected result**: Points are awarded based on how fast the player submitted his answer, with a maximum of 100 points awarded for the perfect time and a minimum of 20 when the timer runs out before submitting the answer.

- Select an incorrect answer.
    * **Expected result**: The player gets no points for this round.


### Estimation type question

- The question appears.
    * **Expected result**: A designated place appears in which the players can enter their guess.

- Enter an invalid response in the text field.
    * **Expected result**: An alert appears that says: `Invalid answer`, `You should only enter an integer number`.

- Enter a valid response.
    * **Expected result**: The player is awarded points based on how close he is to the answer and how fast he answered. If the player gets the perfect answer the score is between 40 and 100. Otherwise, they can get any score between 0 and 100.


## Jokers

- Press `Remove one Answer` Joker for a question that allows it.
    * **Expected result**: One incorrect answer is grayed out and made nonclickable.

- Select all answers and press `Remove one Answer`.
    * **Expected result**: One incorrect answer is marked as not selected, then grayed out and made nonclickable.

- Try using `Remove one Answer` for `Estimation` type questions.
    * **Expected result**:  The joker is not available to use.

- Press `Double Points` joker and get the correct answer.
    * **Expected result**: The points are doubled and added to the existing score.

- Press `Double Points` joker and get an incorrect answer.
    * **Expected result**: No points awarded.

- Press `Decrease time` joker.
    * **Expected result**: The timer speeds up. After the round is over the timer is at normal speed.

- Multiple people press `Decrease time` joker in the same round.
    * **Expected result**: The timers speed up according to how many jokers have been used. After the round is over the timer is at normal speed.

- Press `Increase time` joker in a singleplayer session.
    * **Expected result**: The timer slows down. After the round is over the timer is at normal speed.

- Try using jokers in a Singleplayer session.
    * **Expected result**: All jokers can be used before the timer is up and the player submits his final answer.

- Try using jokers in a Multiplayer session.
    * **Expected result**: The `Remove one` joker can be used before giving the final answer (for the questions it is available), but the `Decrease Time` and the `Double Points` can only be used after the player submits his answer and before the timer runs out (for the last player that has to answer the question these 2 jokers are not available).   

- Try using any of the 3 jokers after they have already been used.
    * **Expected result**: The jokers can not be used again after usage (except if they get refreshed).

- Use jokers and get a refreshment.
    * **Expected result**: The player gets notified that they get a refreshment and the refreshed joker(s) can be used again.

- Use any joker in a Multiplayer session.
    * **Expected result**: All players in the lobby can see the used joker and who used it.

## Tutorial screen
Start on the tutorial screen...

- Be on the first page.
    * **Expected result**: Left arrow is faded and cannot be clicked, right arrow is lit up and available. The first dot is lit up and the rest are faded.

- Click on the right arrow a certain number of times.
    * **Expected result**: Left arrow is lit up and available, right arrow is lit up and available. The next page and corresponding dot are shown.

- Click on the left arrow a certain number of times.
    * **Expected result**: Left arrow is lit up and available, right arrow is lit up and available. The previous page and corresponding dot are shown.

- Be on the last page.
    * **Expected result**: Left arrow is lit up and available, right arrow is faded and cannot be clicked. The last dot is lit up and the rest are faded.

- Press `Back` button.
    * **Expected result**:  The player is sent to the Splash screen.

## Admin Panel Screen
Accessible either from a web browser on `localhost:8080` or from splash screen (localhost can be switched to the ip of the server where the activities are).

### Main page
- Enter the Admin Panel.
    * **Expected result**: The list of all activities is displayed with the total number of entries.

- Press `Add an activity`.
    * **Expected result**: 4 more buttons appear: `Add one activity`, `Add activities (JSON)`, `Add activities (JSON-formatted file` and `Add images`.

### Add activity

#### Add one activity
- Press `Add one activity`.
    * **Expected result**: Fields for `Title`, `Consumption`, `Image Source`, `Info source`, and a `Send` button appear.

- Complete fields with valid information (this is explained in the README file).
    * **Expected result**: Activity is added to the DB and the` Addition was successful!` message is displayed.

- Enter invalid information.
    * **Expected result**: Activity gets rejected and the `Addition was unsuccessful!` message is displayed.


#### Add activities (JSON)
- Press `Add activities (JSON)`.
    * **Expected result**: A field for JSON text and a `Send` button appear.

- Input valid information (this is explained in the README file).
    * **Expected result**: Activity is added to the DB and the` Addition was successful!` message is displayed.

- Input invalid information.
    * **Expected result**: Activity gets rejected and the `Addition was unsuccessful!` message is displayed.

#### Add activities (JSON-formatted file)
- Press `Add activities (JSON-formatted file)`.
    * **Expected result**: A `Browse` button appears and a `Send` button appears.

- Select a valid file that contains activities (valid and invalid).
    * **Expected result**: All valid activities are added to the DB and `Bulk addition was successful! Added N entries.` message is displayed, where N is the number of valid activities.

- Select an invalid file.
    * **Expected result**: File gets rejected and a `Could not parse file!` message appears.

#### Add Images
- Press `Add images`.
    * **Expected result**: A `Browse` button appears and a `Send` button appears.

- Select a *.zip* file that contains images.
    * **Expected result**: Images are added to the server and the `Uploaded zip of images successfully!` message appears.


### Remove an activity

- Press `Remove an activity` button.
    * **Expected result**: A text field with the label `Enter an id` and a `Send` button appears.

- Enter an id of a valid activity.
    * **Expected result**: The activity is deleted from the DB and the` Removal was successful!` message appears.

- Enter invalid id.
    * **Expected result**: `Removal was unsuccessful! Make sure you enter a valid id.` message appears.

- Enter a valid id of an invalid activity.
    * **Expected result**: `Removal was unsuccessful! An entry with the provided id was not found.` message appears.

### Edit an activity

- Press `Edit an activity`.
    * **Expected result**: Fields for `activity'id`, `Title`, `Consumption`, `Image Source`, `Info source` and a `Send` button appear.

- Complete fields with valid information (no duplicate titles and valid activity).
    * **Expected result**: Activity is updated to contain the new information and the `Edit was successful!` message is displayed.

- Enter invalid information (either id or invalid activity).
    * **Expected result**: Activity gets rejected and the `Edit was unsuccessful!` message is displayed.

### Refresh database

- Press `Refresh database`.
    * **Expected result**: The activity list gets refreshed.

### Reset database

- Press `Reset database`.
    * **Expected result**: The whole database gets deleted and the `Reset successful!` message appears.

### In-game Admin Panel

- Press `Go back` button.
    * **Expected result**: The user is sent to the splash screen.