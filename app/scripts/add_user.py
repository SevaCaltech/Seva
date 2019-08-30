import boto3
import configparser

# read config
config = configparser.ConfigParser()
config.read("config.ini")
userpoolID = config.get("default", "aws-userpool-id")
username = config.get("new-user", "username")
email = config.get("new-user", "email")
phone_number = config.get("new-user", "phone_number")
password = config.get("new-user", "password")
toilets = config.get("new-user", "toilets").split("\n")

# create the user in cognito
cognito_client = boto3.client('cognito-idp')
try:
    cognito_response = cognito_client.admin_create_user(
        UserPoolId=userpoolID,
        Username=username,
        UserAttributes=[
            {
                'Name': 'email',
                'Value': email
            },
            {
                'Name': 'phone_number',
                'Value': phone_number
            }
        ],
        TemporaryPassword=password,
        DesiredDeliveryMediums=[
            'EMAIL'
        ]
    )
    sub = cognito_response['User']['Attributes'][0]['Value']
except Exception as e:
    print(e)
    quit()


# add the user to the dynamodb table
dynamodb = boto3.resource('dynamodb')
table = dynamodb.Table('SevaOperators')

dynamo_response = table.put_item(
    Item={
        'displayName': username,
        'email': email,
        'phone': phone_number,
        'toilets': toilets,
        'uid': sub
    }
)

# subcribe the user to each toilet SNS topic
sns_client = boto3.client('sns')
for toilet in toilets:
    topic_name = toilet[11:]
    t = sns_client.create_topic(Name=topic_name)
    topicArn = t['TopicArn']
    sns_response = sns_client.subscribe(
        TopicArn=topicArn,
        Protocol='sms',
        Endpoint=phone_number
    )

print("Done!")



