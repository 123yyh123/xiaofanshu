package com.yyh.xfs.job.config;

import com.yyh.xfs.job.mapper.notes.NotesMapper;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.SqlSessionTemplate;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;

import javax.sql.DataSource;

/**
 * @author yyh
 * @date 2024-02-09
 */
@Configuration
@MapperScan(basePackages = "com.yyh.xfs.job.mapper.notes", sqlSessionFactoryRef = "notesSqlSessionFactory")
public class NotesDataSourceConfig {
    //扫描指定前缀的key，使用其value
    @Bean(name = "notesDataSource")
    @ConfigurationProperties(prefix="spring.datasource.notes")
    public DataSource notesDataSource(){
        return DataSourceBuilder.create().build();
    }

    @Bean(name="notesSqlSessionFactory")
    public SqlSessionFactory notesSqlSessionFactory(@Qualifier("notesDataSource") DataSource dataSource) throws Exception{
        SqlSessionFactoryBean ssfb = new SqlSessionFactoryBean();
        ssfb.setDataSource(dataSource);
        // 设置 Mapper XML 文件的位置，可以使用通配符
        ResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
        ssfb.setMapperLocations(resolver.getResources("classpath:mapper/notes/*.xml"));
        return ssfb.getObject();
    }

    @Bean(name="notesSqlSessionTemplate")
    public SqlSessionTemplate notesSqlSessionTemplate(@Qualifier("notesSqlSessionFactory") SqlSessionFactory sqlSessionFactory){
        return new SqlSessionTemplate(sqlSessionFactory);
    }
}
