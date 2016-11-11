package net.tuxun.core.mybatis.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import net.tuxun.core.mybatis.SystemMetaObject;
import net.tuxun.core.mybatis.db.Parser;
import net.tuxun.core.mybatis.page.PageAttr;
import net.tuxun.core.mybatis.sqlsource.PageDynamicSqlSource;
import net.tuxun.core.mybatis.sqlsource.PageProviderSqlSource;

import org.apache.ibatis.builder.StaticSqlSource;
import org.apache.ibatis.builder.annotation.ProviderSqlSource;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.ResultMap;
import org.apache.ibatis.mapping.ResultMapping;
import org.apache.ibatis.mapping.SqlSource;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.scripting.xmltags.DynamicSqlSource;
import org.apache.ibatis.scripting.xmltags.MixedSqlNode;
import org.apache.ibatis.scripting.xmltags.SqlNode;
import org.apache.ibatis.session.Configuration;

/**
 * 对Mybatis MappedStatement 对象的处理
 * 
 * @author liuqiang
 */
@SuppressWarnings("rawtypes")
public class MSUtils implements Constant {

  private static final List<ResultMapping> EMPTY_RESULTMAPPING = new ArrayList<ResultMapping>(0);

  private Parser parser;

  public MSUtils(Parser parser) {
    this.parser = parser;
  }

  /**
   * 处理count查询的MappedStatement
   * 
   * @param ms
   * @param sqlSource
   * @param args
   */
  public void processCountMappedStatement(MappedStatement ms, SqlSource sqlSource, Object[] args) {
    args[0] = getMappedStatement(ms, sqlSource, args[1], SUFFIX_COUNT);
  }

  /**
   * 处理分页查询的MappedStatement
   * 
   * @param ms
   * @param sqlSource
   * @param page
   * @param args
   */
  public void processPageMappedStatement(MappedStatement ms, SqlSource sqlSource, PageAttr attr,
      Object[] args) {
    args[0] = getMappedStatement(ms, sqlSource, args[1], SUFFIX_PAGE);
    // 处理入参
    args[1] = setPageParameter((MappedStatement) args[0], args[1], attr);
  }


  /**
   * 设置分页参数
   * 
   * @param parameterObject
   * @param page
   * @return
   */
  public Map setPageParameter(MappedStatement ms, Object parameterObject, PageAttr attr) {
    BoundSql boundSql = ms.getBoundSql(parameterObject);
    Map map = parser.setPageParameter(ms, parameterObject, boundSql, attr);
    return map;
  }

  /**
   * 获取ms - 在这里对新建的ms做了缓存，第一次新增，后面都会使用缓存值
   * 
   * @param ms
   * @param sqlSource
   * @param suffix
   * @return
   */
  public MappedStatement getMappedStatement(MappedStatement ms, SqlSource sqlSource,
      Object parameterObject, String suffix) {
    MappedStatement qs = null;
    if (ms.getId().endsWith(SUFFIX_PAGE) || ms.getId().endsWith(SUFFIX_COUNT)) {
      throw new RuntimeException(
          "分页插件配置错误:请不要在系统中配置多个分页插件(使用Spring时,mybatis-config.xml和Spring<bean>配置方式，请选择其中一种，不要同时配置多个分页插件)！");
    }
    if (parser.isSupportedMappedStatementCache()) {
      try {
        qs = ms.getConfiguration().getMappedStatement(ms.getId() + suffix);
      } catch (Exception e) {
        // ignore
      }
    }
    if (qs == null) {
      // 创建一个新的MappedStatement
      qs =
          newMappedStatement(ms,
              getsqlSource(ms, sqlSource, parameterObject, suffix.equals(SUFFIX_COUNT)), suffix);
      if (parser.isSupportedMappedStatementCache()) {
        try {
          ms.getConfiguration().addMappedStatement(qs);
        } catch (Exception e) {
          // ignore
        }
      }
    }
    return qs;
  }

  /**
   * 新建count查询和分页查询的MappedStatement
   * 
   * @param ms
   * @param sqlSource
   * @param suffix
   * @return
   */
  public MappedStatement newMappedStatement(MappedStatement ms, SqlSource sqlSource, String suffix) {
    String id = ms.getId() + suffix;
    MappedStatement.Builder builder =
        new MappedStatement.Builder(ms.getConfiguration(), id, sqlSource, ms.getSqlCommandType());
    builder.resource(ms.getResource());
    builder.fetchSize(ms.getFetchSize());
    builder.statementType(ms.getStatementType());
    builder.keyGenerator(ms.getKeyGenerator());
    if (ms.getKeyProperties() != null && ms.getKeyProperties().length != 0) {
      StringBuilder keyProperties = new StringBuilder();
      for (String keyProperty : ms.getKeyProperties()) {
        keyProperties.append(keyProperty).append(",");
      }
      keyProperties.delete(keyProperties.length() - 1, keyProperties.length());
      builder.keyProperty(keyProperties.toString());
    }
    builder.timeout(ms.getTimeout());
    builder.parameterMap(ms.getParameterMap());
    if (suffix.equals(SUFFIX_PAGE)) {
      builder.resultMaps(ms.getResultMaps());
    } else {
      // count查询返回值int
      List<ResultMap> resultMaps = new ArrayList<ResultMap>();
      ResultMap resultMap =
          new ResultMap.Builder(ms.getConfiguration(), id, int.class, EMPTY_RESULTMAPPING).build();
      resultMaps.add(resultMap);
      builder.resultMaps(resultMaps);
    }
    builder.resultSetType(ms.getResultSetType());
    builder.cache(ms.getCache());
    builder.flushCacheRequired(ms.isFlushCacheRequired());
    builder.useCache(ms.isUseCache());

    return builder.build();
  }

  /**
   * 获取新的sqlSource
   * 
   * @param ms
   * @param sqlSource
   * @param parameterObject
   * @param count
   * @return
   */
  public SqlSource getsqlSource(MappedStatement ms, SqlSource sqlSource, Object parameterObject,
      boolean count) {
    if (sqlSource instanceof DynamicSqlSource) {// 动态sql
      MetaObject msObject = SystemMetaObject.forObject(ms);
      SqlNode sqlNode = (SqlNode) msObject.getValue("sqlSource.rootSqlNode");
      MixedSqlNode mixedSqlNode;
      if (sqlNode instanceof MixedSqlNode) {
        mixedSqlNode = (MixedSqlNode) sqlNode;
      } else {
        List<SqlNode> contents = new ArrayList<SqlNode>(1);
        contents.add(sqlNode);
        mixedSqlNode = new MixedSqlNode(contents);
      }
      return new PageDynamicSqlSource(this, ms.getConfiguration(), mixedSqlNode, count);
    } else if (sqlSource instanceof ProviderSqlSource) {// 注解式sql
      return new PageProviderSqlSource(parser, ms.getConfiguration(),
          (ProviderSqlSource) sqlSource, count);
    } else if (count) {// RawSqlSource和StaticSqlSource
      return getStaticCountSqlSource(ms.getConfiguration(), sqlSource, parameterObject);
    } else {
      return getStaticPageSqlSource(ms.getConfiguration(), sqlSource, parameterObject);
    }
  }

  /**
   * 获取静态的分页SqlSource <br/>
   * ParameterMappings需要增加分页参数的映射
   * 
   * @param configuration
   * @param sqlSource
   * @param parameterObject
   * @return
   */
  public SqlSource getStaticPageSqlSource(Configuration configuration, SqlSource sqlSource,
      Object parameterObject) {
    BoundSql boundSql = sqlSource.getBoundSql(parameterObject);
    return new StaticSqlSource(configuration, parser.getPageSql(boundSql.getSql()),
        parser.getPageParameterMapping(configuration, boundSql));
  }

  /**
   * 获取静态的count的SqlSource
   * 
   * @param configuration
   * @param sqlSource
   * @param parameterObject
   * @return
   */
  public SqlSource getStaticCountSqlSource(Configuration configuration, SqlSource sqlSource,
      Object parameterObject) {
    BoundSql boundSql = sqlSource.getBoundSql(parameterObject);
    return new StaticSqlSource(configuration, parser.getCountSql(boundSql.getSql()),
        boundSql.getParameterMappings());
  }
}
