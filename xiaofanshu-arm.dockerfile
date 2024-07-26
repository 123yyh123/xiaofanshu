FROM fmww/alpine-jdk8
MAINTAINER yyh <yahaoyang929@gmail.com>
EXPOSE 9000
# 创建应用目录
WORKDIR /app
# 复制 JAR 包和启动脚本到容器中
COPY *.jar /app/
COPY run.sh /app/run.sh

#创建启动脚本
RUN chmod 777 /app/run.sh

# 创建日志目录
RUN mkdir /app/logs

#执行命令
ENTRYPOINT ["sh","/app/run.sh"]