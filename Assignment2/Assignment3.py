import urllib.request, json
import boto3
from botocore.exceptions import NoCredentialsError
import datetime; 
import time

ACCESS_KEY = 'xx'
SECRET_KEY = 'xxx'

url = 'http://opd.it-t.nl/data/amsterdam/ParkingLocation.json'

def upload_to_aws(local_file, bucket, s3_file):
    s3 = boto3.client('s3', aws_access_key_id=ACCESS_KEY,
                      aws_secret_access_key=SECRET_KEY)

    try:
        s3.upload_file(local_file, bucket, s3_file)
        return True
    except FileNotFoundError:
        print("The file was not found")
        return False
    except NoCredentialsError:
        print("Credentials not available")
        return False

data_hash = hash(0)

while True:
    with urllib.request.urlopen(url) as response:
        #Read in the the data
        s = response.read()
        data = json.loads(s)
        
        #Check that it has not changed
        if (data_hash != hash(s)):
            file_object = open('data.json', 'w+')
            file_object.write(s.decode('utf-8'))
            ct = datetime.datetime.now() 
            uploaded = upload_to_aws('data.json', 'andrejkinesis', 'ParkingData/'+ct.strftime("%Y/%m/%d/%H:%M:%S.json"))
            if uploaded:
                print('Upload Successful')
                data_hash = hash(s)
            else:
                print('Upload Failed')
            
            file_object.close()
    
    time.sleep(10)