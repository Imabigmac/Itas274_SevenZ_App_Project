SevenZ App project ReadMe 

Overview: 

The SevenZ dating app project is incomplete relative to the original goal, however I was able to implement core functionality of the App originally 
specified. Currently a user can be crated with account details all added to a firebase database. This data is then used to match 
two users together. The app has GPS location functionality working, and uses this data in the match making process. 
Also implemented is the chat roulette. If a user has 7 matches a random other user is selected and a chat is created. 
There is a simple implementation of a chat system, however this chat system is now "live" and can't be demo'd. more this below.
The UI is not complete, ideally page labels would be better and profile pictures would be added. 

App instructions:
1.	Launch App - if using an emulator set a location for the emulator to use. This location data will be entered. 
2.	Enter information into the account creation fields, enter a compatibility trait (1 entry, no spaces). 
the trait "movies" will get a match for sure as the database is full of that trait. 
3.	Click “Login”.
4.	Click on the “profile” dashboard icon to go to the next app page. 
5.	See the new account data load onto the page. 
6.	Click “Match” – this will match behind the scenes. 
7.	Click “SevenZ” – this will now create a new “chat match” behind the scenes. 
8.	Open the chat by clicking on the chat icon. 
9.	Enter a message, click submit and messages will load into the text field below. 


Bugs: 
There are quite a few bugs :( 

1. When a user is matched they are only matched using 1 trait. This also happens to be the first the trait in string of traits.
A trait has to be a single word as all traits entered are a single string, separated by a space. so "dog lover" trait would just be called
"dog" and then would be matched with other "dog". 

2. A matched user could be matched with themselves. 

3. if a user has less than 7 matches they may not be randomly matched. 

4. A user can't match until the profile information is populated on the "account / dashboard" page of the app. Pressing the match button
before the user data is populated will cause the app to crash. As the user account creation data has not been posted to the database. 

5. Cant press the SevenZ button until user has been matched. Otherwise nothing will happen. 

6. Duplicated users will cause app to crash when matching process is initiated. 

7. It is possible to accidently create more than account. IF the fields have data and the login button is clicked twice multiple user will be made. 

8.Chat messages for a user are stored in the same location. Im not really sure why this occurs. So when a new chat is opened between users, 
the entire chat history is displayed for both users... 

Limitations: 

Chat can only been seen by one user. But logically two users would be able to see it. Without proper account authentication 
or user creation only the last created user account is "active" so if two users are matched together and a new chat is made for those 
two users only the current users can see the chat. But the App will load all chat message history for each user so if the matched users have 
message history all their mesasges will load into display message box. 

Only 1 chat token can be assigned to a user. This was by accident and needs some more database logic to sort out. Though I believe my original
app intention meant to just a timed conversation and at the end the chat would close with the option for the two users to add each other 
as friends for another chat system.

There is no way to re-sign into an existing account. Entering the same username and "logging in" will create a duplicate username and 
incomplete account. 

Code: 

I used your firebase database connection code and chat code, but modified it and expanded on it quite a bit in several locations. 
So thank you for that :) 

lines 264-269 the “geodecoder” code was pulled from a taken overflow, but is otherwise similarly documented by google. 


Overview: 

I think having the ability to sign in as a user would have been a good addition and I should have attempted to implement that and just the matching system. Leaving the messaging system out due to limited time available. Instead I attempted to implement everything but the functionality of all components is limited.
