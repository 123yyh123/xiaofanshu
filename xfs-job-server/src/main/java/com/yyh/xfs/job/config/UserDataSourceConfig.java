package com.yyh.xfs.job.config;

import com.yyh.xfs.job.mapper.user.UserMapper;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.SqlSessionTemplate;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;

import javax.sql.DataSource;

/**
 * @author yyh
 * @date 2024-02-09
 */
@Configuration
@MapperScan(basePackages = "com.yyh.xfs.job.mapper.user", sqlSessionFactoryRef = "userSqlSessionFactory")
public class UserDataSourceConfig {
    //扫描指定前缀的key，使用其value
    @Bean(name = "userDataSource")
    @Primary
    @ConfigurationProperties(prefix="spring.datasource.user")
    public DataSource userDataSource(){
        return DataSourceBuilder.create().build();
    }

    @Bean(name="userSqlSessionFactory")
    @Primary
    public SqlSessionFactory userSqlSessionFactory(@Qualifier("userDataSource") DataSource dataSource) throws Exception{
        SqlSessionFactoryBean ssfb = new SqlSessionFactoryBean();
        ssfb.setDataSource(dataSource);
        // 设置 Mapper XML 文件的位置，可以使用通配符
        ResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
        ssfb.setMapperLocations(resolver.getResources("classpath:mapper/user/*.xml"));
        return ssfb.getObject();
    }

    @Bean(name="userSqlSessionTemplate")
    @Primary
    public SqlSessionTemplate userSqlSessionTemplate(@Qualifier("userSqlSessionFactory") SqlSessionFactory sqlSessionFactory){
        return new SqlSessionTemplate(sqlSessionFactory);
    }
}
