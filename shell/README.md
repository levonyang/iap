对于企业族谱（页面展示为"关联族谱"），拿海致打比方，只用调用cluster_import.sh导入该族谱文件就可以了
eg: ./cluster_import.sh 127.0.0.1:8183 haizhi zupu /somedir/haizhi_zupu

对于其他算法挖出的族谱（页面展示为"关联担保"），比如挖出海致的担保圈，需要调用cluster_import.sh和path_import.sh
算法一般会跑出两份文件，一个叫group，一个叫path（这个path里的担保圈路径一定要在group的族谱中存在，算法保证）
eg: ./cluster_import.sh 127.0.0.1:8183 haizhi danbao group
    ./path_import.sh 127.0.0.1:8183 haizhi danbao path