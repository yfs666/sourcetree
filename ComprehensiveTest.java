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

	@Override
    public Boolean validateRule(StockPriceDO priceInfo) {
        return priceInfo.getMa5() >= priceInfo.getClose() && priceInfo.getClose() >= priceInfo.getMa10();
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

	/**
     * 统计某时间段内该code对应的数量
     */
    @Select("select count(*) from stock_today_price where code =#{code} and time > #{start}  and time < #{end}  ")
    Integer countByDate(@Param("code") String code, @Param("start") Date start, @Param("end") Date end);

    @Select("select * from stock_today_price where code =#{code} and time >= #{date} order by time limit #{limit}  ")
    List<StockPriceDO> getEarliestByDate(@Param("code") String code, @Param("date") Date date, @Param("limit") Integer limit);

    @Select("select avg(percent) from stock_today_price where time=#{time} and code like #{code} ")
    BigDecimal countIncrease( @Param("code") String code, @Param("time") Date time);
	
//这是A文件新添加内容的注释
	@Test
	public Integer methodForA() {
		//为方法a添加一些注释和实现
		return 0;
	}
	//AAAAAAAAAAAAAA注释
	@Test
	public String methodForA2() {
		//为方法A2添加一些注释和实现
		return null;
	}
	//这是B文件新添加内容的注释
	@Test
	public void methodForB() {
		//为方法b添加一些注释和实现
	}
	//BB注释
	@Test
	public void methodForB2() {
		//为方法b2添加一些注释和实现
	}
//aaaaa
	@Override
    public Object getTagValue(StockPriceDO priceInfo) {
        //按照时间倒序查询最近一个大约当天最高点的数据
        StockPriceDO preHigh = stockPriceMapper.getLatestNewHigh(priceInfo.getCode(), priceInfo.getHigh(), priceInfo.getTime());
        if (preHigh == null) {
            return 0;
        }
        //量时间段的数量
        Integer count = stockPriceMapper.countByDate(priceInfo.getCode(), preHigh.getTime(), priceInfo.getTime());
        return count;
    }
	//aaaaa
	 @Override
    public Object getTagValue(StockPriceDO priceInfo) {
        Date startDate = this.getStartDate(priceInfo);
        List<StockPriceDO> priceDOS = stockPriceMapper.getEarliestByDate(priceInfo.getCode(), startDate, 1);
        if (CollectionUtils.isEmpty(priceDOS)) {
            return 0;
        }
        StockPriceDO startPriceInfo = priceDOS.get(0);
        //根据开始时间，计算这段时间的增幅，下降用负数表示
        BigDecimal todayClose = new BigDecimal(priceInfo.getClose());
        BigDecimal startClose = new BigDecimal(startPriceInfo.getClose());
        // 格式化，并且四舍五入一下,由于展示的是百分比，先乘以100再算
        BigDecimal upAmount = todayClose.subtract(startClose).multiply(new BigDecimal("100"));
        BigDecimal upRatio = upAmount.divide(startClose,2, BigDecimal.ROUND_HALF_UP);
        return upRatio.doubleValue() + "%";
    }

    public static void main(String[] args) {
        BigDecimal todayClose = new BigDecimal(1);
        BigDecimal startClose = new BigDecimal(3);
        BigDecimal upRatio = todayClose.divide(startClose);
        System.out.println(upRatio.toString());
    }
	//getTagValue commend     bbbbb
	@Override
    public Object getTagValue(StockPriceDO priceInfo) {
        Double percent = priceInfo.getPercent();
        //0直接表示0，如果不是0，
        if (percent == 0) {
            return 0;
        }
        if (percent >= 10) {
            return 10;
        }
        if (percent <= -10) {
            return -10;
        }
        String section = this.getSection(percent);
        return section;
    }

    /**bbbb
     * 整数绝对值大的是闭区间，比如2表示在 2~3之间   -5表示  ~5~-6之间
     * @param number
     * @return
     */
    private String getSection(double number) {
        double floor = Math.floor(number);
        double ceil = Math.ceil(number);
        String section = "";
        if (number > 0) {
            if (floor == ceil) {
                BigDecimal ceilDecimal = new BigDecimal(ceil);
                ceil = ceilDecimal.add(new BigDecimal(1)).doubleValue();
            }
            section = floor + "~" + ceil;
        } else {
            if (floor == ceil) {
                BigDecimal floorDecimal = new BigDecimal(floor);
                floor = floorDecimal.subtract(new BigDecimal(1)).doubleValue();
            }
            section = ceil + "~" + floor;
        }
        return section;
    }



}
