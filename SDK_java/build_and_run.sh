#!/bin/bash
cp  ../src/com/huawei/* ./code/CodeCraft-2019/src/main/java/com/huawei/
sh build.sh
cd bin
sh startup.sh config/car.txt config/road.txt config/cross.txt config/answer.txt