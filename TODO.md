###  * Fix:

- [x] transcription language resets (internally) when navigating back from flipcards
    - start matching with non-default transcription
    - go back
    - AR:  transcription visually remains but logically resets to default/fallback
    - ER: both remain and match
- [X] detect locale hot changes on Languages screen
- [ ] detect locale hot changes on Alphabets screen
- [ ] sometimes cards are not clickable (latest clicked the previous time or what?)
- [ ] sometimes cards are weakly or not at all shuffled

###  * User experience

- [X] publish debug apk
- [ ] describe database update procedure
- [ ] invite into «Discussions» section

###  * Database

- [ ] store database in a user-writable location

###  * Features

- [ ] add Armenian language
    - [x] Entities
    - [x] Capital letters
    - [x] Lowercase letters
    - [ ] transcriptions for letter names
- [ ] add dictionary mode
    - [ ] learning
    - [ ] training with flipcards
    - [ ] training with manual input
    - [ ] periodic «Time to train» notifications

###  * Back to Database

- [ ] create database editor
- [ ] revert database dir back to internal
