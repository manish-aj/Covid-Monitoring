import sqlite3
from sqlite3 import Error
import sys
from math import cos, asin, sqrt, pi
import math


def establishConn(db_file):
    conn = None
    try:
        conn = sqlite3.connect(db_file)
    except Error as e:
        print(e)

    return conn

def getLocs(conn, date):
    cur = conn.cursor()
    cur.execute("SELECT _latitude, _longitude, _time_location FROM locationTable")

    rows = cur.fetchall()
    
    maxDateLoc = {}
    myLoc = {}
    totalLoc = []
    for eachDate in range(date-6, date+1):
        myLoc[eachDate] = []
        maxDateLoc[eachDate] = []
    
    for row in rows:
        latitude = row[0]
        longitude = row[1]
        curDate = str(row[2])
        curDateTime = str(row[2])

        if latitude!=0 and longitude!=0 and curDate!='':
            curDateTime = int(curDateTime[:14])
            curDate = int(curDate[:8])
            latitude = float(latitude) / 1000000
            longitude = float(longitude) / 1000000
            if curDate > date-7 and curDate <= date:
                if len(maxDateLoc[curDate]) == 0 :
                    maxDateLoc[curDate] = [curDateTime, latitude, longitude]
                else :
                    if maxDateLoc[curDate][0] < curDateTime:
                        maxDateLoc[curDate] = [curDateTime, latitude, longitude]
                
    for key in maxDateLoc:
        if len(maxDateLoc[key]) != 0:
            myLoc[key] = [maxDateLoc[key][1], maxDateLoc[key][2]]
            
    cur.close()
    
    for key in myLoc:
        if len(myLoc[key]) > 0:
            totalLoc.append(myLoc[key])
    
    return totalLoc

def genMatrix(allSubjectsLocations, subjectID):
    finalResult = [0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0]
    myLoc = allSubjectsLocations[subjectID]
    
    for i in range(0,11):
        if i != subjectID - 1 :
            for j in range(len(myLoc)):
                for k in range(len(allSubjectsLocations[i])):
                    latitude1 = myLoc[j][0]
                    longitude1 = myLoc[j][1]
                    latitude2 = allSubjectsLocations[i][k][0]
                    longitude2 = allSubjectsLocations[i][k][1]
                    if getDistanceFromlatitudelongitudeInKm(latitude1, longitude1, latitude2, longitude2) <= 5.001:
                        finalResult[i] = 1
                        break
                if finalResult[i] == 1:
                    break
    
    return finalResult

def getDistanceFromlatitudelongitudeInKm(latitude1,longitude1,latitude2,longitude2):
    R = 6371 # Radius of the earth in km
    dlatitude = deg2rad(latitude2-latitude1) #deg2rad below
    dlongitude = deg2rad(longitude2-longitude1)
    a = math.sin(dlatitude/2) * math.sin(dlatitude/2) + math.cos(deg2rad(latitude1)) * math.cos(deg2rad(latitude2)) * math.sin(dlongitude/2) * math.sin(dlongitude/2)
    c = 2 * math.atan2(math.sqrt(a), math.sqrt(1-a))
    d = R * c # Distance in km
    return d

def deg2rad(deg):
    return deg * (math.pi/180)

def makeReadableDate(date):
    readableDate = str(date)
    readableDate = readableDate[0:4] + "-" + readableDate[4:6] + "-" + readableDate[6:8]
    return readableDate
    

databasePrefix = r"./dbs/LifeMap_GS"
f = open(sys.argv[3], "w")

subjectID = int(sys.argv[1])
date = sys.argv[2]

date = date.replace('/', '')
date = date.replace('-', '')
date = int(date)

# create a database connection
conns = []
for i in range(1, 12):
    conns.append(establishConn(databasePrefix + str(i) + '.db'))

allSubjectsLocations = []

for i in range(0,11):
    allSubjectsLocations.append(getLocs(conns[i], date))

bfsSt = set()
bfsEnd = set()
bfsSt.add(subjectID - 1)

contactMatrix = []
finalContactMatrix = []
for i in range(0,11):
    contactMatrix.append([0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0])
    finalContactMatrix.append([0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0])

while True:
    newbfsSt = set()
    for key in bfsSt:
        if key not in bfsEnd:
            contactMatrix[key] = genMatrix(allSubjectsLocations, key)
            for i in range(0, 11):
                if contactMatrix[key][i] == 1:
                    if (i != subjectID - 1):
                        finalContactMatrix[subjectID - 1][i] = 1
                    newbfsSt.add(i)
            bfsEnd.add(key)
    if len(newbfsSt) == 0:
        break
    bfsSt = newbfsSt
    
for j in range(0,11):
    bfsSt = set()
    bfsEnd = set()
    bfsSt.add(j)
    if j != subjectID - 1:
        while True:
            newbfsSt = set()
            for key in bfsSt:
                if key not in bfsEnd:
                    for i in range(0, 11):
                        if contactMatrix[key][i] == 1:
                            if (i != j):
                                finalContactMatrix[j][i] = 1
                            newbfsSt.add(i)
                    bfsEnd.add(key)
            if len(newbfsSt) == 0:
                break
            bfsSt = newbfsSt
    

f.write("Subject ID:" + str(subjectID) + " " + "Date: " + makeReadableDate(date) + "\n\n")            

for i in range(0,11):
    f.write(str(finalContactMatrix[i]) + "\n")

for conn in conns:
    conn.close()

