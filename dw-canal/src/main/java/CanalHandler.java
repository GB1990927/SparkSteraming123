import com.alibaba.fastjson.JSONObject;
import com.alibaba.otter.canal.protocol.CanalEntry;
import util.GmallConstant;
import util.MykafkaSender;

import java.util.List;

public class CanalHandler {

    String tableName;   //表名
    CanalEntry.EventType eventType; //时间类型  insert update delete
    List<CanalEntry.RowData> rowDataList; //行级

    public CanalHandler(String tableName, CanalEntry.EventType eventType, List<CanalEntry.RowData> rowDataList) {
        this.tableName = tableName;
        this.eventType = eventType;
        this.rowDataList = rowDataList;
    }

    public  void handle(){
        if(tableName.equals("order_info")&&eventType.equals(CanalEntry.EventType.INSERT)){
            for (CanalEntry.RowData rowData : rowDataList) {
                sendKafka(  rowData, GmallConstant.KAFKA_TOPIC_ORDER);
            }
        }


    }

    /**
     * 发送kafka
     * @param rowData
     * @param topic
     */
    private void sendKafka(CanalEntry.RowData rowData,String topic){
        List<CanalEntry.Column> columnsList = rowData.getAfterColumnsList();
        JSONObject jsonObject = new JSONObject();
        for (CanalEntry.Column column : columnsList) {
            System.out.println(column.getName()+"------>"+column.getValue());
            // 发送数据到对应的topic中
            jsonObject.put(column.getName(),column.getValue());
        }
        String rowJson = jsonObject.toJSONString();

        MykafkaSender.send(topic,rowJson);

    }
}
