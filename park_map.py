import geopandas as gpd
import pandas as pd
import folium
from folium.plugins import HeatMap
import boto3
import os
import json
import csv

session = boto3.Session(
    aws_access_key_id='xx',
    aws_secret_access_key='xxxx',
    region_name='eu-west-1'
)

s3 = session.resource('s3')
bucket = s3.Bucket('andrejkinesis')
obj = bucket.Object(key='ParkingData/2020/11/22/19:30:16.json')

response = obj.get()
lines = response['Body'].read()
json_data = lines.decode('utf-8')
f = open("data.json", "w")
f.write(json_data)
f.close()

file = open('data.json', 'r')
data = json.load(file)

lat=[];lon=[];cap=[]
for i in data['features']:
    lat.append(float(i['geometry']['coordinates'][1]))
    lon.append(float(i['geometry']['coordinates'][0]))
    if i['properties']['FreeSpaceLong'] == "":
        freelong = 0

    if i['properties']['FreeSpaceShort'] == "":
        freeshort = 0

    if i['properties']['FreeSpaceLong'] != "":
        freelong = float(i['properties']['FreeSpaceLong'])

    if i['properties']['FreeSpaceShort'] != "":
        freeshort = float(i['properties']['FreeSpaceShort'])

    cap.append(freelong + freeshort)



list_of_tuples = list(zip(cap, lat, lon))

df = pd.DataFrame(list_of_tuples, columns = ['capacity', 'latitude', 'longitude']) 
max_amount = df['capacity'].max()


hmap = folium.Map(location=[df['latitude'][0], df['longitude'][0]], zoom_start=12)
arg = list(zip(df['latitude'].values, df['longitude'].values, df['capacity'].values))
hm_wide = HeatMap(arg, 
                   min_opacity=0.3,
                   max_val=max_amount,
                   radius=17, blur=15, 
                   max_zoom=1, 
                 )

#folium.GeoJson().add_to(hmap)
hmap.add_child(hm_wide)

hmap.save(os.path.join('results', 'heatmap.html'))

