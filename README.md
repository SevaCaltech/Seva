Seva App
==
### Background
This is the android App for the Caltech Seva Toilet Project.  
The goal is to connect the operators to the repair guides and other information.

Design: [Anastasia Hanan | Case Study:SEVA](https://www.anastasiahanan.com/caltechsevanp)  
Research: [Michael R. Hoffmann | Self-contained Toilet Wastewater Treatment](http://www.hoffmann.caltech.edu/research/seva.html)  

### Screenshots
<img src="docs/screenshots/getstarted1.png" alt="getstarted1" width="250"/>&nbsp;&nbsp;&nbsp;&nbsp;
<img src="docs/screenshots/getstarted2.png" alt="getstarted2" width="250"/>&nbsp;&nbsp;&nbsp;&nbsp;
<img src="docs/screenshots/getstarted3.png" alt="getstarted3" width="250"/>
<br>
<br>
<img src="docs/screenshots/getstarted4.png" alt="getstarted4" width="250"/>&nbsp;&nbsp;&nbsp;&nbsp;
<img src="docs/screenshots/login.png" alt="login" width="250"/>&nbsp;&nbsp;&nbsp;&nbsp;
<img src="docs/screenshots/home.png" alt="home" width="250"/>
<br>
<br>
<img src="docs/screenshots/navdrawer.png" alt="navdrawer" width="250"/>&nbsp;&nbsp;&nbsp;&nbsp;
<img src="docs/screenshots/notification.png" alt="notification" width="250"/>&nbsp;&nbsp;&nbsp;&nbsp;
<img src="docs/screenshots/notificationview.png" alt="notificationview" width="250"/>
<br>
<br>
<img src="docs/screenshots/repairtitle.png" alt="repairtitle" width="250"/>&nbsp;&nbsp;&nbsp;&nbsp;
<img src="docs/screenshots/repairstep.png" alt="repairstep" width="250"/>&nbsp;&nbsp;&nbsp;&nbsp;
<img src="docs/screenshots/settings.png" alt="settings" width="250"/>
<br>
<br>

---
### Creating new users
Here are the steps to create new operators for the app.

#### Cognito
Cognito will allow the users to login to the app.
  
1. Log into [Amazon Cognito](https://console.aws.amazon.com/cognito/home?region=us-east-1#). And click on `Manage User Pools` and then `Seva Operators` to get to the user pool for the app.
2. Click on `Users and Groups`    
<img src="docs/readme/cognito-1.jpg" alt="Users and Groups" width="800"/>

3. Click on `Create user`  
<img src="docs/readme/cognito-2.jpg" alt="create user" width="800"/>

4. Fill in the Username, Temporary-password, Phone Number, and Email sections. Leave all checkmarks, and only check which method of sending the invitation to the user. (SMS and Email)
5. Hit `Create user`
6. Click on the newly created Username highlighted in blue. We will need to comeback to the `sub` key to identify the users in the database.
<img src="docs/readme/cognito-3.jpg" alt="find sub" width="800"/>


#### DynamoDB
DynamoDB will store more detailed information about each user, essentially their profile. The table this is information is all stored in is called ToiletOperators
  
1. Log into [DynamoDB](https://console.aws.amazon.com/dynamodb/home?region=us-east-1#).
2. Click on `Tables` and then from the list of tables choose `SevaOperators`.
<img src="docs/readme/dynamo-1.jpg" alt="SevaOperators" width="800"/>

3. Select `Items` from the tabs and then hit `Create item`
<img src="docs/readme/dynamo-2.jpg" alt="Create item" width="800"/>

4. From the dropdown menu switch it from `Tree` to `Text` and enter the new user's information in the following format. Where the `uid` field is the `sub` string from Step 6 in the Cognito instructions.
```
{
  "displayName": "test",
  "email": "test@gmail.com",
  "phone": "+15555555555"
  "toilets": [
    "toilet_ip_address_1",
    "toilet_ip_address_2"
  ],
  "uid": "dfbfd6d8-85aa-4da0-9d56-b730a7202fd2"
}
```
5. Click `Save` and make sure the new user shows up in the table.
#### SNS
AWS Simple Notification Service (SNS) handles sending error notification messages to the operators. Where users are subscribed to each toilet as a topic.  
*There might be a Lambda function to do this automatically in the future.*  

6. Log into [Amazon SNS](https://console.aws.amazon.com/sns/v3/home?region=us-east-1#/dashboard). Click `Topics`, select the toilet that the operator is in charge of, and click `Create subscription`. 
 <img src="docs/readme/sns-1.jpg" alt="Create item" width="800"/>

7. Select `SMS` from the dropdown `Protocol` menu, fill in the `Endpoint` field with the user's phone number, and hit `Create subscription`.

### Adding repair guides
This is all done in DBBrowser. The database can either be manually edited or a csv can be imported.  

1. [Download](https://sqlitebrowser.org/dl/) and open DBBrowser.
2. Open the database: `File->Open Database...` Location: `app/src/main/assets/databases/sevaDb.db`

#### repairInfo table
This table contains the extra information for each repair guide. 

3. Create the csv with the following structure and name it `repairInfo.csv`.
<img src="docs/readme/excel-2.png" alt="repair Info" width="800"/>
<br>
<br>

*NOTE: When importing a csv into DBBrowser, the csv will be appended to the selected table if it already exists. So we first want to clear the existing table*.   
4. Click on the `Execute SQL` tab. Type in the following SQL command to clear the table. Then press the play button.
```
DELETE FROM repairInfo
```
<img src="docs/readme/db-1.png" alt="clear SQL table" width="800"/>

5. Import the repairInfo table: `File->Import->Table from CSV file...`. Select your `repairInfo.csv` file.
6. Verify that the format is correct. Then press `OK` and `Yes`
<img src="docs/readme/db-2.png" alt="import csv" width="800"/>

7. To view the repairInfo table select the `Browse Data` tab and `repairInfo` from the dropdown menu.
<img src="docs/readme/db-3.png" alt="view table" width="800"/>

#### repairStep table
The repairStep table contains each step for a given repair code.

8. Create the csv with the following structure and name it `repairStep-X.csv`. **Where `X` is the number of the repair code.**
 <img src="docs/readme/excel-1.png" alt="repair step" width="800"/>

9. If the repairStep-X table exists already, clear it just like Step 4 above, but change the table name to the repairStep table:
```
DELETE FROM repairStep-X
```

10. Import the repairStep-X csv and verify that the table is correct, following steps 5-7 above.

### Adding new toilets
The information for the toilets is stored locally on the App so this will also be done in DBBrowser.  

1. Open the sevaDb just as Steps 1-2 above.
2. Create the csv with the following structure and name it `toiletInfo.csv`.
 <img src="docs/readme/excel-3.png" alt="toilet Info" width="800"/>

3. Clear the current toiletInfo table by following step 4 from the repair guide instructions above.
4. Import the toiletInfo csv and verify that the table is correct, following steps 5-7 above.

### Deploying App
TBD still working on this.  
1. 

---
### Resources
- [[Android] Creating Custom Login Screen for AWS Mobile Hub](https://wtmimura.com/post/aws-mobile-hub-android-custom-login/)
- [AWS Documentation Guides & Documentation](https://docs.aws.amazon.com/aws-mobile/latest/developerguide/reference-mobile-hub.html)
- [Android Developers Documentation](https://developer.android.com/docs) 
