FROM eclipse-temurin:17-jre-alpine

WORKDIR /app
# 复制 Maven 编译的 jar 包到镜像中，这里采用通配符匹配
COPY target/*.jar app.jar

# 暴露项目的 8888 端口
EXPOSE 8888

# 挂载日志与配置挂载点（可选）
VOLUME /tmp

# 防止时区乱码，配置上海时间
ENV TZ=Asia/Shanghai
RUN ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone

ENTRYPOINT ["java", "-jar", "app.jar"]
