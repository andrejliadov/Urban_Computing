import socket

def data_to_shorts(data):
    array=[]
    for i in range(0, len(data)):
        array.append(int(data[i]))

    return array

#Create a socket object
soc = socket.socket()
print("Socket succesfully created")

#Choose an unused, unimport port for the service
port = 10203

#Bind the socket to the socket file descriptor and make it listen
soc.bind(('',port))
print("Socket binded to port %i"%port)

#Put the socket into listening mode
soc.listen(5)
print("Socket is listening")

f = open("data.csv", "a")

c, address = soc.accept()
print ('Got a connection from ', address)

while True:
    data = c.recv(1024)
    
    #Convert the data into shorts
    data = data_to_shorts(data)
    for i in range(0, len(data)):
        f.write(str(data[i]))
        f.write("\n")
    print(data)
