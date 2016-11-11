package net.tuxun.core.mybatis.db;

/**
 * @author liuqiang
 */
public enum Dialect {
  oracle, mysql, sqlserver;

  public static Dialect of(String dialect) {
    try {
      Dialect d = Dialect.valueOf(dialect);
      return d;
    } catch (IllegalArgumentException e) {
      String dialects = null;
      for (Dialect d : Dialect.values()) {
        if (dialects == null) {
          dialects = d.toString();
        } else {
          dialects += "," + d;
        }
      }
      throw new IllegalArgumentException("Mybatis分页插件dialect参数值错误，可选值为[" + dialects + "]");
    }
  }
}
