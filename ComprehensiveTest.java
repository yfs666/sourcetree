package com.graze.service.count;

import com.graze.enums.ComprehensiveTypeEnum;
import com.graze.enums.KeyEnum;
import com.graze.enums.TagEnum;
import com.graze.service.GrazeServiceApplication;
import com.graze.service.dao.mapper.StockPriceMapper;
import com.graze.service.dao.pojo.StockPriceDO;
import com.graze.service.mongodb.param.DayPriceTagParam;
import com.graze.service.mongodb.pojo.Comprehensive;
import com.graze.service.mongodb.pojo.DayPriceTag;
import com.graze.service.mongodb.service.ComprehensiveRepository;
import com.graze.service.mongodb.service.DayPriceTagRepository;
import com.graze.service.util.CalendarUtil;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(GrazeServiceApplication.class)
public class ComprehensiveTest {

    @Autowired
    MongoTemplate mongoTemplate;
    @Autowired
    StockPriceMapper stockPriceMapper;
    @Autowired
    ComprehensiveRepository comprehensiveRepository;
    @Autowired
    DayPriceTagRepository dayPriceTagRepository;

    @Test
    public void tagCount() {
        String key = TagEnum.UP.key;
        Query query = new Query(Criteria.where("noValTags").in(Arrays.asList(key)));
        long count = mongoTemplate.count(query, DayPriceTag.class);
        System.out.println(count);
    }

    @Test
    public void comprehensivetagTest() {
        for (ComprehensiveTypeEnum type : ComprehensiveTypeEnum.values()) {
            String symbol = type.symbol;
            Date date = CalendarUtil.stringToDate("2017-09-28", CalendarUtil.DEFAULT_DATE_PATTERN);
            StockPriceDO stockPriceDO = stockPriceMapper.getBySymbolAndTime(symbol, date);
            if (stockPriceDO == null) {
                return;
            }
            Comprehensive comprehensive = new Comprehensive();
            comprehensive.setId(stockPriceDO.getId());
            comprehensive.setSymbol(stockPriceDO.getSymbol());
            comprehensive.setCode(stockPriceDO.getCode());
            comprehensive.setDate(stockPriceDO.getTime());
            //收涨
            BigDecimal percent = stockPriceMapper.countIncrease(type.codePre + "%", comprehensive.getDate());
            comprehensive.setCloseIncrease(percent);
            //TODO 最高点和最低点的比例平均数
            //        comprehensive.setHighIncrease();
            //        comprehensive.setLowIncrease();
            //  NoValueTag
            for (TagEnum tagEnum : TagEnum.values()) {
                String key = tagEnum.key;
                Query query = new Query(Criteria.where("noValTags").in(Arrays.asList(tagEnum.key)).and("date").is(stockPriceDO.getTime()).and("code").regex(type.codePre + ".*"));
                long count = mongoTemplate.count(query, DayPriceTag.class);
                Map<String, Object> noValueTag = comprehensive.getNoValueTag();
                noValueTag = noValueTag == null ? new HashMap<>() : noValueTag;
                noValueTag.put(tagEnum.key, count);
                comprehensive.setNoValueTag(noValueTag);
            }
//            for (KeyEnum keyEnum : KeyEnum.values()) {
//                //暂时只处理区间
//                if (!KeyEnum.BETWEEN.equals(keyEnum)) {
//                    return;
//                }
//                //TODO  如何统计
//                List<Map<String, Object>> valueTag = comprehensive.getValueTag();
//                valueTag = valueTag == null ? new ArrayList<>() : valueTag;
//            }
            comprehensiveRepository.save(comprehensive);
        }
//TODO   带值的标记
//        comprehensive.setValueTag();
    }
	//fasdfsdfsdfdsf
	@Test
	pubulic void test1() {
		//这是aaa文件夹的test文件
	}
  //fasdfsadfsdfsdfds
	@Test
	pubulic void tesfsdfdstfasdfsd() {
		run();
		//fasdjifsdlfjsdfjsdifjds
	}
//fasdfsdfsadfas
	@Test
	pubulic void testfasdfsd() {
		//打开附近的空间发的卡死了发多少发送到发送到发斯蒂芬手动发水立方奥斯卡了发
		//fajsdl;fjasdlfdsklfjsdlfjsldjfsldajfksadljfksadljfklsd
	}
//fasdfsdfsdfds
	
//发大水发斯蒂芬
}
