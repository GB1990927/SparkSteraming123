package com.example.demo.service.imp;

import com.example.demo.dao.DauMapper;
import com.example.demo.service.PublisherService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
@Service
public class PublisherServiceImpl implements PublisherService {

    @Autowired
    DauMapper dauMapper;


    @Override
    public int getDauTotal(String date) {
        return  dauMapper.selectDauTotal(date);
    }

    @Override
    public Map getDauHours(String date) {
        HashMap dauHourMap=new HashMap();
        List<Map> dauHourList = dauMapper.selectDauTotalHourMap(date);
        for (Map map : dauHourList) {
            dauHourMap.put(map.get("LH"),map.get("CT"));
        }

        return dauHourMap;
    }

}
