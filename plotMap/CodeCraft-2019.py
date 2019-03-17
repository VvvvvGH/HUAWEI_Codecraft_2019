import logging
import sys
import plotMap as pm


def main():

    car_path = "/home/cheng/IdeaProjects/HWSC/SDK_java/bin/config/car.txt"
    road_path = "/home/cheng/IdeaProjects/HWSC/SDK_java/bin/config/road.txt"
    cross_path = "/home/cheng/IdeaProjects/HWSC/SDK_java/bin/config/cross.txt"
    answer_path = "/home/cheng/IdeaProjects/HWSC/SDK_java/bin/config/answer.txt"

    carData,roadData,crossData=read_files(car_path,road_path,cross_path)
    pm.plotMap(carData,roadData,crossData)
    result=process(carData,roadData,crossData)
    write_files(result)

# to read input file
def read_files(car_path,road_path,cross_path):
    print('reading files')
    carData={'id':[],
             'from':[],
             'to':[],
             'speed':[],
             'planTime':[]}
    roadData={'id':[],
              'length':[],
              'speed':[],
              'channel':[],
              'from':[],
              'to':[],
              'isDuplex':[]}
    crossData={'id':[],
             'roadUp':[],
             'roadRight':[],
             'roadDown':[],
             'roadLeft':[]}
    with open(car_path, 'r') as f:
        for line in f.readlines():
            if line[0]=='#':
                continue
            line=line.strip().replace('\n','').replace('(','').replace(')','').split(',')
            carData['id'].append(line[0].strip())
            carData['from'].append(line[1].strip())
            carData['to'] .append(line[2].strip())
            carData['speed'].append(line[3].strip())
            carData['planTime'].append(line[4].strip())

    with open(road_path, 'r') as f:
        for line in f.readlines():
            if line[0]=='#':
                continue
            line=line.strip().replace('\n','').replace('(','').replace(')','').split(',')
            roadData['id'].append(line[0].strip())
            roadData['length'] .append(line[1].strip())
            roadData['speed'] .append(line[2].strip())
            roadData['channel'] .append(line[3].strip())
            roadData['from'] .append(line[4].strip())
            roadData['to'] .append( line[5].strip())
            roadData['isDuplex'] .append( line[6].strip())

    with open(cross_path, 'r') as f:
        for line in f.readlines():
            if line[0]=='#':
                continue
            line=line.strip().replace('\n','').replace('(','').replace(')','').split(',')
            crossData['id'].append(line[0].strip())
            crossData['roadUp'] .append(line[1].strip())
            crossData['roadRight'] .append(line[2].strip())
            crossData['roadDown'] .append(line[3].strip())
            crossData['roadLeft'] .append(line[4].strip())

    return carData, roadData, crossData

# process
def process(carData,roadData,crossData):
    print('processing')
    result=None
    return result

# to write output file
def write_files(data):
    print('writing files')
    pass

if __name__ == "__main__":
    main()