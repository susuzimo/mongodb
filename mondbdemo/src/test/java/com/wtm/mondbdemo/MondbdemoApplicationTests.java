package com.wtm.mondbdemo;


import com.wtm.mondbdemo.pojo.Log;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.*;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;


@SpringBootTest
class MondbdemoApplicationTests {


	@Autowired
	MongoTemplate mongoTemplate;

	@Test
	void contextLoads() {
		Log log = new Log();
		log.setUsername("wtm");
		log.setAge("13");
		//mongoTemplate.save(log,"users");
		mongoTemplate.insertAll(Arrays.asList(log,log));
	}

	@Test
	void find() {
		//Query query = new Query(new Criteria("username").is("wtm").and("age").is("13"));
		//Query query = new Query(new Criteria("username").is("wtm"));
		//Query query = new Query(new Criteria("age").lte("11"));
		//分页
		Sort sort = Sort.by(Sort.Order.desc("age"));

		//完全匹配
		Pattern pattern = Pattern.compile("^lison", Pattern.CASE_INSENSITIVE);
		//左匹配
		Pattern patternLeft = Pattern.compile("^.*pe", Pattern.CASE_INSENSITIVE);
		//右匹配
		Pattern patternRight = Pattern.compile("^p.*$", Pattern.CASE_INSENSITIVE);
		//模糊匹配
		Pattern patternLike = Pattern.compile("^.*w.*$", Pattern.CASE_INSENSITIVE);

		Criteria criteria = Criteria.where("username").regex(pattern);//查询条件
		Query query = new Query(criteria);
		query.fields().include("username");
		//List<Log> logs = mongoTemplate.find(query.with(sort).limit(2).skip(1), Log.class, "users");
		List<Log> logs = mongoTemplate.find(query, Log.class, "users");
		for (Log log:logs) {
			System.out.println(log.getUsername()+"打印输出"+log.getAge());
		}
	}

	@Test
	void delete() {
		Query query = new Query(new Criteria("username").is("test123"));
		mongoTemplate.remove(query,Log.class,"users");
	}



	@Test
	void upate() {
		Query query = new Query(new Criteria("username").is("wtm"));
		Log log = new Log();
		log.setAge("14");
		Update update = new Update();
		update.set("age", log.getAge());
		mongoTemplate.updateFirst(query, update, Log.class, "users");
	}

}
