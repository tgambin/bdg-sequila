package pl.edu.pw.ii.biodatageeks.tests

import java.io.{OutputStreamWriter, PrintWriter}

import com.holdenkarau.spark.testing.{DataFrameSuiteBase, SharedSparkContext}
import org.apache.spark.sql.{MySparkSession, SparkSession}
import org.bdgenomics.utils.instrumentation.{Metrics, MetricsListener, RecordedMetrics}
import org.biodatageeks.datasources.BAM.BAMRecord
import org.biodatageeks.preprocessing.coverage.CoverageStrategy
import org.scalatest.{BeforeAndAfter, FunSuite}

class CoverageTestSuite extends FunSuite with DataFrameSuiteBase with BeforeAndAfter with SharedSparkContext{

    val bamPath = getClass.getResource("/NA12878.slice.bam").getPath
    val adamPath = getClass.getResource("/NA12878.slice.adam").getPath
    val metricsListener = new MetricsListener(new RecordedMetrics())
    val writer = new PrintWriter(new OutputStreamWriter(System.out))
    val tableNameBAM = "reads"
    val tableNameADAM = "readsADAM"
    before{

      Metrics.initialize(sc)
      sc.addSparkListener(metricsListener)
      System.setSecurityManager(null)
      spark.sql(s"DROP TABLE IF EXISTS ${tableNameBAM}")
      spark.sql(
        s"""
           |CREATE TABLE ${tableNameBAM}
           |USING org.biodatageeks.datasources.BAM.BAMDataSource
           |OPTIONS(path "${bamPath}")
           |
      """.stripMargin)

      spark.sql(s"DROP TABLE IF EXISTS ${tableNameADAM}")
      spark.sql(
        s"""
           |CREATE TABLE ${tableNameADAM}
           |USING org.biodatageeks.datasources.ADAM.ADAMDataSource
           |OPTIONS(path "${adamPath}")
           |
      """.stripMargin)

    }
  test("BAM - coverage table-valued function"){
    val session: SparkSession = MySparkSession(spark)

    session.experimental.extraStrategies = new CoverageStrategy(session) :: Nil

    assert(session.sql(s"select * from coverage('${tableNameBAM}') where position=20204").first().getInt(3)===1019)

  }

}
