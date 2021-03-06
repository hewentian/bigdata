package com.hewentian.spark

import java.util.Properties

import com.hewentian.spark.util.SparkUtil

/**
  * 如果是在IDEA下直接运行，则要将mysql的jar包放到spark集群各个节点的 {SPARK_HOME}/jars 目录下
  * mv mysql-connector-java-5.1.25.jar /home/hadoop/spark-2.4.4-bin-hadoop2.7/jars/
  */
object JdbcTest {
  def main(args: Array[String]): Unit = {
    System.setProperty("HADOOP_USER_NAME", SparkUtil.hdfsUser)

    val spark = SparkUtil.getSparkSession()

    val sqlContext = spark.sqlContext

    // Creates a DataFrame based on a table named "student" stored in a MySQL database.

    val df = sqlContext
      .read
      .format("jdbc")
      .option("url", SparkUtil.jdbcUrl)
      .option("driver", SparkUtil.jdbcDriver)
      .option("user", SparkUtil.jdbcUser)
      .option("password", SparkUtil.jdbcPassword)
      .option("dbtable", "student")
      .load()

    // Looks the schema of this DataFrame.
    df.printSchema()

    df.show()

    // Counts student by age
    val countsByAge = df.groupBy("age").count()
    countsByAge.show()

    // Saves countsByAge to hdfs in the JSON format.
    countsByAge.write.format("json").save(SparkUtil.hdfsUrl + "countsByAge")

    val queryStr = """(select * from student where id <= 5) as stud"""

    spark.read
      .format("jdbc")
      .option("url", SparkUtil.jdbcUrl)
      .option("driver", SparkUtil.jdbcDriver)
      .option("user", SparkUtil.jdbcUser)
      .option("password", SparkUtil.jdbcPassword)
      .option("dbtable", queryStr)
      .load()
      .show()

    val connectionProperties = new Properties()
    connectionProperties.put("driver", SparkUtil.jdbcDriver)
    connectionProperties.put("user", SparkUtil.jdbcUser)
    connectionProperties.put("password", SparkUtil.jdbcPassword)

    var predicates = Array("id <= 5 AND sex = 1")

    spark.read.jdbc(SparkUtil.jdbcUrl, "student", predicates, connectionProperties).show()

    spark.stop()
  }
}
