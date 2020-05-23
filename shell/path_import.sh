#!/bin/bash
HOST=$1
DOMAIN_NAME=$2
TYPE=$3
FILE=$4

if [ $# != 4 ];then
    echo "Usage: $0 host domain_name type file_path"
    echo "eg: $0 127.0.0.1:8183 lz_bank danbao /home/work/cluster_result/lz_danbao_path"
    exit 1
fi

curl -H 'Authorization: Bearer eyJhbGciOiJIUzI1NiJ9.eyJqdGkiOiI2ZmVaVjNTckxYViIsImV4cCI6MTgxOTMzMjc0NywidWlkIjoic3VwZXIifQ.0Vz10oG0oE3UyXv_7B_pcsTYXfustsJjiLHPlTXTCWM' -F 'file=@'$FILE';filename="somepath"' http://$HOST/api/graph/import/path\?domain_name\=$DOMAIN_NAME\&type\=$TYPE
