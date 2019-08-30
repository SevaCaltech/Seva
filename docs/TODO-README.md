# TODO

### Important
- Finish Test Button functionality
    - connect Lambda function to RasPi
- Finish Take Picture function in the Repair guides
    - add a column to the SQLite db which step(s) the picture button should pop up on
- Populate all of the repair guides
- Better support for different sized screens
    - noticeable in repair guides and language settings
- Push Notifications
- Don't need to add alive signals to SevaToilets Dynamo table
    - This happens because the current lambda will add any IoT message on the toilet topic to the table.
- Not sure if Guest Mode is necessary
    - Only allows user to access repair guides without internet
    - User must be receiving texts which hasn't been fully tested 

### Nice-To-Haves
- melissa's logo/color improvements
- drop down text for longer repair steps
- current job option should not stay checked if there is none
- Admin website for managing toilets/users
- Associate specific repairs with specific operators
- Symbol matching game in the Get Started flow
- Support for different android versions (other than 6-8)