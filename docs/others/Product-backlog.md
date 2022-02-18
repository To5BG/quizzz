# Requirements Draft (based on Q&A lecture 2022 February 09 & Discord Meeting 2022 February 10)

## Functional requirements

### Bare minimum requirements/Must have

#### General
- The player shall be able to play in a singleplayer session where he compares his own ability overtime
- The game shall also incorporate a multiplayer gamemode
- The questions must have a basic **text formatting**, at the very least,  but preferably they should be accompanied by some images/graphics
- The player shall **pick a name** when joining a game, regardless of the game mode
- The player can see his best all-time score on the splash screen
- The player must be able to see his score at all times

#### Single Player
- The player can see an all-time singleplayer **leaderboard** that shows the best scores of all players

#### Multiplayer
- The player shall be able to also visit an all-time multiplayer leaderboard to show best scores in multiplayer sessions
- The game shall open a **waiting area** once a player joins a multiplayer session
    - The waiting area shall require at least 2 players to start a game
    - A game can start once all players in the waiting area press a start button, to ensure that all players are ready when the game starts
    - The waiting area must be emptied once all players press start, to ensure that only a single waiting area is present at any time
    - The player shall be able to see how many other players are in the waiting room
- The players will answer the **same question at the same time**
- The players share the same time to answer a question, unless in-game modifiers are used by other players (see jokers below)
- The players shall see a ranking at the end of a game session and be able to rejoin the waiting area for another game

#### Questions
- A multiplayer game session must have 20 questions
- There must be a small **time window** between questions
- The player shall receive points based on how quickly he answered the question correctly
- Every question has some kind of **image**
- The player shall receive no points if a question is left unanswered or the player was incorret
- The player must be able to see the **leaderboard** for the current session at the middle and the end of a game
- The game must have **multiple choice questions** that can have up to N-1 out of N correct options
    - guess how much energy something takes
    - guess which activity takes the most energy
- The questions shall increase in difficulty as the game progresses

### Should have

- The server should be able to detect when a player has disconnected from a game session

#### Player interaction
- The player should be able to interact with the other players in some way, preferably through **emojis**
- The player should have a variety in picking an emoji, preferably at least 3
- The player should be able to spam these emojis as a way of expressing acute or spontanenous reactions

#### Joker cards/Power ups
- The player should be able to use in-game modifiers that affect the game of himself or for other players, so-called jokers.
- The player may use at most one such joker per game session
- The game should allow for more than one joker to be used in the same round
- Jokers should be used **after** the player answers the question, but before the timer runs out
- The game should have a diverse **joker variants**, some being:
    - The game should be able to provide the players with a joker that **removes one incorrect answer** from the board
    - The game should be able to provide the player with a joker that **decreases the time for other players** to answer the current question, which will be percentage based for balancing purposes
        - The effects of this joker should stack in case several players use it in the same round
    - The game should be able to provide the player with a joker that **grants additional points** for the current question, which can be either multiplicative or additive in effect
- The game should disable jokers based on their applicability to the questions
- The player should either be able to pick jockers for usage pre-game, or be given the jokers in-game

### Could have
- The player can use power-ups in singleplayer sessions to affect his own questions positively
- The website can have a tutorial screen for new players to flatten the learning curve
- The player can adjust the question count for singleplayer sessions
- The first player that joins the waiting room can pick a question count for the next game session he's in
- The player can pick different singleplayer gamemodes, for instance to choose between a fixed question count or a survival gamemode
- The player can see a leaderboard for every gamemode individually
- During a game session the player can see a leaderboard not just at the end, but all the time

#### Sound
- The game could have some sound design to further improve the player gaming experience

### Won't have
- The end-product will have no authentication for its users
- The end-product will be devoid any security features, since faithful users are being presumed
- The game lobbies won't have a player ceiling
- The game will not provide communicational means through chatting due to in-game time constraints
- The game won't allow reconnections into game sessions -> If connection drops you are out of the game

## Non-functional requirements

- The game should not store data in a persistent manner about games (it can stay in RAM)
- The game must have a database to store the **leaderboards** for all gamemodes
- The game must have a separate **database** to store all the different questions

## Notes

### Possible future question types
- Estimation questions
    - Guess a number, the closer you are the more points you get
- Sorting questions
    - Sort provided activities based on power consumption
    - The questions can be formulated to require both ascending or descending order

