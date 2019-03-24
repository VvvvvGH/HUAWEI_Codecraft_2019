#!/bin/bash
cp  ../src/com/huawei/* ./code/CodeCraft-2019/src/main/java/com/huawei/
rm ./bin/CodeCraft-2019-1.0.jar
sh build.sh
cd bin
sh startup.sh config/car.txt config/road.txt config/cross.txt config/answer.txt