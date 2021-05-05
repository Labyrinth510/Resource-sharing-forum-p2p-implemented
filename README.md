# Resource-sharing-forum-p2p-implemented
This is a resource sharing forum which uses p2p to share resources

User Manual
## 0.Introduction
### 0.0 Contributers
COMP208 Group 18

You Wu, Junze Zhu, Zhiyuan He, Asim Shah

### 0.1 Background

This application is intended to be an online resource-sharing forum which implements peer-to- peer file-sharing technology as the core of file-sharing function.

### 0.2 Users of the system

There is only one user of the system, that is the user of the resource-sharing forum.

There are 2 important values associated with each user:
+ Prestige value, a user gains 1 prestige value by publishing one file. There may be minimum prestige value required for downloading a file. 

+ Point, a user needs some points to download every 1 MB of data. User can obtain same points from uploading every 1 MB of data to other users.
+ 
### 0.3 Main functions

First of all, the user is required to log in the system before they can perform any further operations. If they do not have an account, the system has provided a register page for them.

After logged in to the system, the user can use the file-sharing functions of the system:

+ Publish: The user selects the the file they wants to publish, sets constraints of the file (including minimum prestige value required, points required to download per 1 MB of data, and tags of the file, which eases other users search), and then publishes the file.

+ Search & Download: The user could search published files either by a string or by several tags. When searched on string, the server would return files with the input string as a part of the file name. When searched on tags, the server would return results, ordered by the number of tags matched. Then user can select one resource in the search result and download that resource to a chosen location.

+ Monitor tasks: The user could monitor current local fire-transmission tasks. They can monitor the status of the task, including progress, download/upload amount. They can remove/stop/start the task.

NOTE: due to Network Address Translation(NAT), all users of the application must be under the same local network.

### 0.4 Requirement and Installation

Software requirement: The system must have Java SDK to compile and run the application. The system is given in the form of source code. It uses a standard project structure: all java source code are in /src folder, and dependencies are under /lib folder. To execute the program, compile the code and enter the main() at gui.Main.

## 1. Instructions on Operation

### 1.1 Log in

![](https://cdn.jsdelivr.net/gh/Labyrinth510/PictureBed@master/COMP/COM20820210505221806.png)

Once opened the application, the frame above shows up. If the user already holds an account, they can enter their username and password, and then click “Log in” to log in the system.

If the user is not registered yet, they could click on “Sign up”, and will be then guided to the sign up page:

![](https://cdn.jsdelivr.net/gh/Labyrinth510/PictureBed@master/COMP/COM20820210505221822.png)

The user could then enter the username and password they are willing to register following the instructions. The username must have length between 6 to 20, and containing only letters and digits; The password’s length must be between 8-20. The user then confirms the password they has entered before, and press the “Register” button to register their account.

If the user wishes to return to the previous page, he can click on “Return” button to return to the login page.

On registration success, a notification window will pop out to notify the user, and they will be guided back to the login page to log in to the system.

### 1.2 Main Frame
On successful login, the user will be guided to the main frame of the application.

![](https://cdn.jsdelivr.net/gh/Labyrinth510/PictureBed@master/COMP/COM20820210505221853.png)

As shown from the picture above, there are 4 tabs for the main frame: Publish, Search, Tasks, Profile, the first three corresponds to the three main functions of the application, and user could view their account information, including prestige value and points at the Profile page. The system will navigate between tabs by clicking on the tab buttons at the bottom of the window.

### 1.2.1 Publish

![](https://cdn.jsdelivr.net/gh/Labyrinth510/PictureBed@master/COMP/COM20820210505221950.png)

The picture above shows the publish tab of the application. On this tab, the user could select a local file and publish it.

**Select file**

To select a file, press the “Select file” button, and a window would pop out:

Select the file and then click on “Open”, the system will load the selected file and its properties.

**Enter tags**

The user could set tags of the file by entering them into the “Tags:” text field.

NOTE: THE FORMAT OF THE TAGS IS IMPORTANT HERE. Each tag should be only comprise of letters or digitals, The user must split each tag by a comma. Space between tags is acceptable.

example: movie, romance,1990s

![](https://cdn.jsdelivr.net/gh/Labyrinth510/PictureBed@master/COMP/COM20820210505222038.png)

**Set prestige value required**

Lowest prestige required means the least prestige value a user needs to download the file. It must be a nonnegative integer. Illegal inputs will not be accepted.

**Set points required per unit**

Points required per unit indicates for this file, how much points is required to download 1 MB of data. It can only be nonnegative integer. Illegal inputs will not be accepted.

After entering all the parameters and options required for the file, the user could click on the “submit” button to publish the file.

### 1.2.2 Search

Click on “Search” tab at the bottom of the page to switch to the search tab.

NOTE: THE FORMAT OF THE SEARCH STRING IS IMPORTANT!

+ To search by the name of the file:Enter the filename or a consecutive part of filename, and **add a comma at the end of the** string. Note that, DO NOT add unnecessary spaces in the string. Then click the “Search”
button.
![](https://cdn.jsdelivr.net/gh/Labyrinth510/PictureBed@master/COMP/COM20820210505222207.png)

  The result will be demonstrated in the form of table, the parameters of each file would also be shown.

+ To search by tags of the file:

  Enter tags by splitting each tag by a comma (!!DO NOT ADD UNNECESSARY SPACES).

  Example: COMP208,Group18,file-sharingThe result will be demonstrated in the form of table, and the files will be ordered by relevance (i.e. the number of tags match with the search)

+ Download:
  To download a file from the result list, click on the row to select the file, and then click “Download” button. The system will then add the corresponding task to current tasks.
  **Hint: the “number of group member” here indicates the number of users that is currently active in the file transmission. The more active group members, the faster the file transmission would be.**

### 1.2.3 Tasks

Click the “Tasks” profile to view current local file-transmission tasks.
![](https://cdn.jsdelivr.net/gh/Labyrinth510/PictureBed@master/COMP/COM20820210505222343.png)

As shown in the picture above, the current local file-transmission tasks are listed in a table, each with their parameters, including status, size, download/upload amount etc.

To start/stop/remove a task, a user shall first select the task from the task list, then click on the corresponding button below to perform the operation. All tasks are initially in wait status.

Start: if the task is currently stopped or wait, it will start.

Stop: if the task is currently started, it will stop.

Remove: A window will pop out to confirm the remove operation.

![](https://cdn.jsdelivr.net/gh/Labyrinth510/PictureBed@master/COMP/COM20820210505222621.png)

Now, click “remove” to remove the task in the list, but not the file on the disk; or click “remove with the file” to remove both the task and the file on the disk. Click “cancel” to cancel removing and return to the tasks panel.

### 1.2.4 Profile
Click the “Profile” tab to view the user profile. Here the information, including username, prestige value and points of the current logged in user could be found. The user information will be refreshed every time the user switch to the Profile tab.

## 2. Limitation

This system does not handle the problem caused by NAT. The communication between two client is through their IP address. If they are in different private network, they cannot connect to each other. At least one of them has a public IP address or they are in the same private network. Thus, this system is recommended to be used in private network file transmission. For example, students in the same campus network can use this application to share resources.

# Demonstration Video Address

https://stream.liv.ac.uk/fv2ek5w6

# File Address

https://github.com/Labyrinth510/Resource-sharing-forum-p2p-implemented
