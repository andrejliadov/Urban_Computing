import geopandas as gpd
import pandas as pd
import folium
from folium.plugins import HeatMap
import boto3
import os
import csv

session = boto3.Session(
    aws_access_key_id='xx',
    aws_secret_access_key='xxxx',
    region_name='eu-west-1'
)

s3 = session.resource('s3')
bucket = s3.Bucket('andrejkinesis')
obj = bucket.Object(key='2020/12/18/08/NoiseStream-1-2020-12-18-08-42-55-e8431500-13c2-4d76-b838-ad5e2e4ef296')

response = obj.get()
lines = response['Body'].read()
data = lines.decode('utf-8')
reader = csv.reader(data.split('\n'), delimiter=',')
amp=[]; lat=[]; lon=[]
for row in reader:
    if(len(row) > 0):
        amp.append(float(row[0]))
        lat.append(float(row[1]))
        lon.append(float(row[2]))

list_of_tuples = list(zip(amp, lat, lon))

df = pd.DataFrame(list_of_tuples, columns = ['amplitude', 'latitude', 'longitude']) 
max_amount = df['amplitude'].max()


hmap = folium.Map(location=[df['latitude'][0], df['longitude'][0]], zoom_start=16)
arg = list(zip(df['latitude'].values, df['longitude'].values, df['amplitude'].values))
hm_wide = HeatMap(arg, 
                   min_opacity=0.0,
                   max_val=max_amount,
                   radius=17, blur=15, 
                   max_zoom=1, 
                 )

hmap.save(os.path.join('results', 'heatmap.html'))

