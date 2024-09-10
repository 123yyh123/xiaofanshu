FROM openjdk:8-jdk-alpine
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

# 设置时区
RUN ln -sf /usr/share/zoneinfo/Asia/Shanghai /etc/localtime
RUN echo "Asia/Shanghai" > /etc/timezone

#执行命令
ENTRYPOINT ["sh","/app/run.sh"]