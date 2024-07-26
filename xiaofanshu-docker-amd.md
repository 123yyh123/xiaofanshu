# the file for xiaofanshu amd

## nacos
``` sh
docker run -d \
-e MODE=standalone \
-e PREFER_HOST_MODE=hostname \
-e SPRING_DATASOURCE_PLATFORM=mysql \
-e MYSQL_SERVICE_HOST=127.0.0.1 \
-e MYSQL_SERVICE_PORT=3306 \
-e MYSQL_SERVICE_USER=root \
-e MYSQL_SERVICE_PASSWORD='@YangYaHao5036' \
-e MYSQL_SERVICE_DB_NAME=nacos_config \
-e NACOS_AUTH_TOKEN=SecretKey012345678901234567890123456789012345678901234567890123456789 \
-p 8848:8848 \
--name nacos \
--restart=always \
nacos/nacos-server:1.4.2
```

## xxl-job
``` sh
docker run  -d \
-p 11000:8088 \
-v /docker/xxl-job/logs:/data/applogs \
-v /docker/xxl-job/application.properties:/xxl-job/xxl-job-admin/src/main/resources/application.properties \
-e PARAMS="--server.port=8088 \
--spring.datasource.url=jdbc:mysql://127.0.0.1:3306/xxl_job?useUnicode=true&characterEncoding=UTF-8&autoReconnect=true&serverTimezone=Asia/Shanghai \
--spring.datasource.username=root \
--spring.datasource.password=@YangYaHao5036 \
--spring.mail.username=yahaoyang929@vip.qq.com \
--spring.mail.from=yahaoyang929@vip.qq.com \
--spring.mail.password=mcbsnkodpjkobdbd" \
--name xxl-job-admin \
--restart=always \
xuxueli/xxl-job-admin:2.4.0
```


## mongodb
``` sh
docker run -itd --name mongo -v /docker/mongodb:/data/db -p 27017:27017 --restart=always mongo:4.4.0 --auth
```
### 进入容器，初始化用户（例子）
``` sh
docker exec -it mongo mongo admin
db.createUser({ user:'admin',pwd:'admin',roles:[ { role:'userAdminAnyDatabase', db: 'admin'}]});
db.auth('admin', 'admin')
use test
db.createUser({user:'test',pwd:'123456',roles:['readWrite']})
db.auth("test","123456")
db.users.insert( { name:"test1", age:22} )
```

## elasticsearch
``` sh
docker run -p 9200:9200 -p 9300:9300 --name elasticsearch \
--restart=always \
-e "cluster.name=elasticsearch" \
-e "ES_JAVA_OPTS=-Xms512m -Xmx512m" \
-v /docker/es/conf/elasticsearch.yml:/usr/share/elasticsearch/config/elasticsearch.yml \
-v /docker/es/data:/usr/share/elasticsearch/data \
-v /docker/es/plugins:/usr/share/elasticsearch/plugins \
-d "docker.elastic.co/elasticsearch/elasticsearch:7.15.2"
```
elasticsearch.yml
``` yml 
#集群名称 
cluster.name: my-application 
#节点名称
node.name: es-node-1 
#数据和日志的存储目录
path.data: /usr/share/elasticsearch/data 
path.logs: /usr/share/elasticsearch/logs
##设置绑定的ip，设置为0.0.0.0以后就可以让任何计算机节点访问到了 
network.host: 0.0.0.0 
#端口
http.port: 9200 
##设置在集群中的所有节点名称，这个节点名称就是之前所修改的，当然你也可以采用默认的也行，目前 是单机，放入一个节点即可 
cluster.initial_master_nodes: ["es-node-1"]
xpack.security.enabled: true
xpack.license.self_generated.type: basic
xpack.security.transport.ssl.enabled: true
```

### 修改配置
``` sh
sudo vim /etc/sysctl.conf
	vm.max_map_count=262144
sudo sysctl -p
#设置密码
docker exec -it elasticsearch bash
./bin/elasticsearch-setup-passwords interactive
```

## rocketmqNamesrv
``` sh
docker run -d \
--restart=always \
--name rmqnamesrv \
-p 9876:9876 \
-v /docker/rocketmq/data/namesrv/logs:/root/logs \
-v /docker/rocketmq/data/namesrv/store:/root/store \
-e "MAX_POSSIBLE_HEAP=100000000" \
apache/rocketmq:4.9.2 \
sh mqnamesrv 
```

## rocketmqBroker
``` sh
 docker run -d --restart=always --name rmqbroker --link rmqnamesrv:namesrv -p 10911:10911 -p 10909:10909 --privileged=true -v /docker/rocketmq/data/broker/logs:/root/logs -v /docker/rocketmq/data/broker/store:/root/store -v /docker/rocketmq/conf/broker.conf:/opt/docker/rocketmq/broker.conf -e "NAMESRV_ADDR=172.17.0.1:9876" -e "MAX_POSSIBLE_HEAP=200000000" apache/rocketmq:4.9.2 sh mqbroker -c /opt/docker/rocketmq/broker.conf
```
broker.conf
``` conf
terName = DefaultCluster
#broker名称，master和slave使用相同的名称，表明他们的主从关系
autoCreateTopicEnable = true
brokerName = broker-a
#0表示Master，大于0表示不同的slave
brokerId = 0
#表示几点做消息删除动作，默认是凌晨4点
deleteWhen = 04
#在磁盘上保留消息的时长，单位是小时
fileReservedTime = 48
#有三个值：SYNC_MASTER，ASYNC_MASTER，SLAVE；同步和异步表示Master和Slave之间同步数据的机制；
brokerRole = ASYNC_MASTER
#刷盘策略，取值为：ASYNC_FLUSH，SYNC_FLUSH表示同步刷盘和异步刷盘；SYNC_FLUSH消息写入磁盘后才返回成功状态，ASYNC_FLUSH不需要；
flushDiskType = ASYNC_FLUSH
# 设置broker节点所在服务器的内网ip地址即可
namesrvAddr = 127.0.0.1:9876
brokerIP1 = 127.0.0.1
# 磁盘使用达到95%之后,生产者再写入消息会报错 CODE: 14 DESC: service not available now, maybe disk full
diskMaxUsedSpaceRatio=95
```

## rocketmq-console
``` sh
docker run -d \
--restart=always \
--name rmqadmin \
-e "JAVA_OPTS=-Drocketmq.namesrv.addr=172.17.0.1:9876 -Dcom.rocketmq.sendMessageWithVIPChannel=false -Duser.timezone='Asia/Shanghai'" \
-v /etc/localtime:/etc/localtime -p 8180:8080 \
--ulimit nofile=1024 \
styletang/rocketmq-console-ng:latest
```

# xiaofanshu
``` sh
docker build -f xiaofanshu.dockerfile -t xiaofanshu:latest .
docker run -d --name xiaofanshu-container -p 9000:9000 -v /docker/xiaofanshu/logs:/app/logs xiaofanshu:latest
```