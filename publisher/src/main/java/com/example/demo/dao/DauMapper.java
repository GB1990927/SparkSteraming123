package com.example.demo.dao;

import org.apache.ibatis.annotations.Mapper;

import java.util.List;
import java.util.Map;

@Mapper
public interface  DauMapper {
    public Integer selectDauTotal(String date);
    public List<Map> selectDauTotalHourMap(String date);
}
