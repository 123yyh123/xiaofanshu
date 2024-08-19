# 定义 JAR 文件所在目录
jar_directory="/app"

# 循环启动每个 JAR 文件
for jar_file in "$jar_directory"/*.jar
do
    # 获取 JAR 文件名（不带路径）
    project=$(basename "$jar_file" .jar)

    echo "启动 $project..."
    java -Dfile.encoding=utf-8 -jar "$jar_file" --spring.cloud.nacos.config.server-addr="172.17.0.1:8848" > "$jar_directory/logs/$project.log" 2>&1 &
    echo "$project 已启动并输出到 $jar_directory/logs/$project.log"
done

echo "所有项目已启动。"

# 保持容器运行
wait