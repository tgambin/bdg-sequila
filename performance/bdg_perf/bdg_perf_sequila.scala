import org.apache.spark.sql.SequilaSession
import org.biodatageeks.utils.{SequilaRegister, UDFRegister}
import org.biodatageeks.BDGPerf
import org.biodatageeks.BDGPerf.{BDGPerfRunner, BDGQuery, BDGTestParams}

/*set params*/

val ss = SequilaSession(spark)

/*register UDFs*/

UDFRegister.register(ss)

/*inject strategies and data sources*/
SequilaRegister.register(ss)



val BAM_DIR = s"${sys.env("BGD_PERF_SEQ_BAM_DIR")}/*.bam"

//preparation
ss.sql(s"""
CREATE TABLE IF NOT EXISTS reads
USING org.biodatageeks.datasources.BAM.BAMDataSource
OPTIONS(path '${BAM_DIR}')""")



val queries = Array(
  BDGQuery("bdg_seq_count_NA12878","SELECT COUNT(*) FROM reads WHERE sampleId='NA12878.ga2.exome'")
)

BDGPerfRunner.run(ss,queries)


//cleanup
ss.sql(s"""DROP TABLE IF EXISTS reads""")

System.exit(0)


