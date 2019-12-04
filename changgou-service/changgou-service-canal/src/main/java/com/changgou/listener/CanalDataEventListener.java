package com.changgou.listener;

import com.alibaba.fastjson.JSON;
import com.alibaba.otter.canal.protocol.CanalEntry;
import com.changgou.canal.mq.queue.TopicQueue;
import com.changgou.canal.mq.send.TopicMessageSender;
import com.changgou.content.feign.ContentFeign;
import com.changgou.content.pojo.Content;
import com.xpand.starter.canal.annotation.CanalEventListener;
import com.xpand.starter.canal.annotation.DeleteListenPoint;
import com.xpand.starter.canal.annotation.ListenPoint;
import entity.Message;
import entity.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.util.List;

@CanalEventListener
public class CanalDataEventListener {
    @Autowired(required = false)
    private ContentFeign contentFeign;
    @Autowired
    private StringRedisTemplate redisTemplate;
    @Autowired
    private TopicMessageSender topicMessageSender;


    //自定义监听器：                               监听的数据库，             监听的表，可以配多个
    @ListenPoint(destination = "example",schema = "changgou_content",table = {"tb_content"},
            // 监听的类型                 新增                       更新
            eventType ={CanalEntry.EventType.INSERT, CanalEntry.EventType.UPDATE} )

    public void onEventContent(CanalEntry.EntryType entryType, CanalEntry.RowData rowData) {
        //获取分类id
        String categoryId = getColumnValue(rowData, "category_id");
        //通过分类id查询广告列表
        Result<List<Content>> result = contentFeign.findContentByCategoryId(Long.parseLong(categoryId));
        redisTemplate.boundValueOps("content"+categoryId).set(JSON.toJSONString(result.getData()));
    }

    /***
     * 规格、分类数据修改监听
     * 同步SKU生成页面
     * @param eventType
     * @param rowData
     */
    @ListenPoint(destination = "example", schema = "changgou_goods", table = {"tb_spu"}, eventType = {CanalEntry.EventType.UPDATE,CanalEntry.EventType.DELETE})
    public void onEventCustomSpu(CanalEntry.EventType eventType, CanalEntry.RowData rowData) {
        //操作类型
        int number = eventType.getNumber();
        //操作的数据
        String id = getColumnValue(rowData,"id");
        //封装Message
        Message message=new Message(number,id,TopicQueue.TOPIC_QUEUE_SPU,TopicQueue.TOPIC_EXCHANGE_SPU);
        //发送消息
        topicMessageSender.sendMessage(message);
    }
    /*@InsertListenPoint
    public void onEventInsert(CanalEntry.EntryType entryType, CanalEntry.RowData rowData) {
        List<CanalEntry.Column> list = rowData.getAfterColumnsList();
        for (CanalEntry.Column column : list) {
            System.out.println("列名：" + column.getName() + "<---->列值：" + column.getValue());
        }
    }

    @UpdateListenPoint
    public void onEventUpdate(CanalEntry.EntryType entryType, CanalEntry.RowData rowData) {
        List<CanalEntry.Column> beforeColumnsList = rowData.getBeforeColumnsList();
        for (CanalEntry.Column column : beforeColumnsList) {
            System.out.println("列名：" + column.getName() + "<---->列值：" + column.getValue());
        }
        System.out.println("---------------------更新后-----------------------");
        List<CanalEntry.Column> afterColumnsList = rowData.getAfterColumnsList();
        for (CanalEntry.Column column : afterColumnsList) {
            System.out.println("列名：" + column.getName() + "<---->列值：" + column.getValue());
        }
    }*/

    @DeleteListenPoint
    public void onEventDelete(CanalEntry.EntryType entryType, CanalEntry.RowData rowData) {
        List<CanalEntry.Column> beforeList = rowData.getBeforeColumnsList();
        for (CanalEntry.Column column : beforeList) {
            System.out.println("列名：" + column.getName() + "<--->列值：" + column.getValue());
        }
    }

    //获取字段对应的值
    private String getColumnValue(CanalEntry.RowData rowData, String columnName) {
        List<CanalEntry.Column> list = rowData.getAfterColumnsList();
        for (CanalEntry.Column column : list) {
            if (columnName.equals(column.getName())) {
                return column.getValue();
            }
        }
        return null;
    }
    /***
     * 获取某个列对应的值
     * @param rowData
     * @param name
     * @return
     */
    public String getColumn(CanalEntry.RowData rowData , String name){
        //操作后的数据
        for (CanalEntry.Column column : rowData.getAfterColumnsList()) {
            String columnName = column.getName();
            if(columnName.equalsIgnoreCase(name)){
                return column.getValue();
            }
        }
        //操作前的数据
        for (CanalEntry.Column column : rowData.getBeforeColumnsList()) {
            String columnName = column.getName();
            if(columnName.equalsIgnoreCase(name)){
                return column.getValue();
            }
        }
        return null;
    }
}
