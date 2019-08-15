import java.text.SimpleDateFormat
import java.util
import java.util.Date

import com.alibaba.fastjson.JSON
import common.GmallConstants
import org.apache.hadoop.conf.Configuration
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.apache.spark.broadcast.Broadcast
import org.apache.spark.rdd.RDD
import org.apache.hadoop.hbase.conf.ConfigurationManager
import org.apache.phoenix.spark._
import org.apache.spark.streaming.dstream.{DStream, InputDStream}
import org.apache.spark.streaming.{Seconds, StreamingContext}
import org.apache.spark.{SparkConf, SparkContext}
import redis.clients.jedis.Jedis
import utill.{MykafkaUtil, RedisUtil, StartUpLog}

object RealtimeStartupApp {
  def main(args: Array[String]): Unit = {
    val sparkConf: SparkConf = new SparkConf().setMaster("local[*]").setAppName("gmall2019")
    val sc = new SparkContext(sparkConf)
    val ssc = new StreamingContext(sc, Seconds(5))

    val startupStream: InputDStream[ConsumerRecord[String, String]] = MykafkaUtil.getKafkaStream(GmallConstants.KAFKA_TOPIC_STARTUP, ssc)


    val startupLogDstream: DStream[StartUpLog] = startupStream.map(_.value()).map { log =>
      val startUpLog: StartUpLog = JSON.parseObject(log, classOf[StartUpLog])
      val dateTimeStr: String = new SimpleDateFormat("yyyy-MM-dd HH").format(new Date(startUpLog.ts))
      val dateArr: Array[String] = dateTimeStr.split(" ")
      startUpLog.logDate = dateArr(0)
      startUpLog.logHour = dateArr(1)
      startUpLog
    }
    val value: DStream[(String, StartUpLog)] = startupLogDstream.map(i => {
      (i.mid, i)
    })
    //过滤掉窗口期间重复的KEY
    val value1: DStream[StartUpLog] = value.groupByKey().map(i => {
      (i._1, i._2.take(1))
    }).flatMap(olp => olp._2)

    //过滤掉redis里面重复的key
    val value3: DStream[StartUpLog] = value1.transform(rdd => {
      val jedis = new Jedis("hadoop100", 6379)
      val dateStr: String = new SimpleDateFormat("yyyy-MM-dd").format(new Date())
      val key = "dau:" + dateStr
      println("过滤前：" + rdd.count())
      val strings: util.Set[String] = jedis.smembers(key)
      val value4: Broadcast[util.Set[String]] = ssc.sparkContext.broadcast(strings)
      jedis.close()
      val value2: RDD[StartUpLog] = rdd.filter(i => {
        !value4.value.contains(i.mid)
      })
      println("过滤后：" + value2.count())
      value2
    })

    //往redis里面写入新数据
    value3.foreachRDD { rdd => {
      rdd.foreachPartition { i => {
        val client: Jedis = RedisUtil.getJedisClient
        for (i1 <- i) {
          val key = "dau:" + i1.logDate
          client.sadd(key, i1.mid)
//          println(i1)
        }
        client.close()
      }
      }
    }
    }
    //往hbase里面写数据

    value3.foreachRDD(rdd=>{
      if(!rdd.isEmpty()){
        rdd.saveToPhoenix("GMALL2019_DAU",Seq("MID", "UID", "APPID", "AREA", "OS", "CH", "TYPE", "VS", "LOGDATE", "LOGHOUR", "TS") ,new Configuration,Some("hadoop100,hadoop101,hadoop102:2181"))
      }
    })


      //    startupLogDstream.foreachRDD(rdd=>{
      //      rdd.foreach(println)
      //    })
      ssc.start()
      print("程序开始")
      ssc.awaitTermination()
    }
  }
