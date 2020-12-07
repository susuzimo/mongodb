package com.wtm.mondbdemo;


import com.mongodb.client.result.UpdateResult;
import com.wtm.mondbdemo.pojo.Comment;
import com.wtm.mondbdemo.pojo.Log;
import com.wtm.mondbdemo.pojo.Order;
import com.wtm.mondbdemo.pojo.User;
import org.bson.conversions.Bson;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.FindAndModifyOptions;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.query.*;

import javax.print.Doc;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Pattern;


import static org.springframework.data.mongodb.core.query.Query.query;


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


	/*
	生成数据
	 */
	@Test
	public void batchInsertOrder() {
		String[] userCodes = new String[] { "james", "AV", "allen", "six",
				"peter", "mark", "king", "zero", "lance", "deer", "lison" };
		String[] auditors = new String[] { "auditor1","auditor2","auditor3","auditor4","auditor5"};
		List<Order> list = new ArrayList<Order>();
		Random rand = new Random();
		for (int i = 0; i < 100000; i++) {
			Order order = new Order();
			int num = rand.nextInt(11);
			order.setUseCode(userCodes[num]);
			order.setOrderCode(UUID.randomUUID().toString());
			order.setOrderTime(new Date());
			order.setPrice(rand.nextLong());
			int length = rand.nextInt(5)+1;
			String[] temp = new String[length];
			for (int j = 0; j < temp.length; j++) {
				temp[j] = getFromArrays(temp,auditors,rand);
			}
			order.setAuditors(temp);
			list.add(order);
		}
		mongoTemplate.insertAll(list);
	}


	private String getFromArrays(String[] temp, String[] auditors, Random rand) {
		String ret = null;
		boolean test = true;
		while (test) {
			ret = auditors[rand.nextInt(5)];
			int i =0;
			for (String _temp : temp) {
				i++;
				if(ret.equals(_temp)){
					break;
				}
			}
			if(i==temp.length){
				test=false;
			}
		}
		return ret;
	}

	/*
	聚合查询
	 */
	@Test
	void findGroup() {
		Aggregation aggregation = Aggregation.newAggregation(
				//日期条件
				Order.class,
				//第一步先根据查询条件查出想要的数据
				//Aggregation.match(Criteria.where("devnum").is("devnum").and("time").gte(start).lte(last)),
                        /*第二步从查询出的数据中取出需要处理的数据，我这里是因为库中存的时间格式是“2018-03-08”的格式，
                        而我要的是根据一年当中12个月来分组，所以这里我要先把数据截取一下，截取到月，也就是substr(time,0,7)
                        截取time这个字段的数据截取后的时间格式为“2018-03”留作分组按月用,其中用到了三个方法
                        .project方法.andExpression方法和.as方法.project是要取那几个字段来用.andExpression是把处理后的数据添
                        添加到处理后的结果中，as方法是为处理后的数据字段加一个别名，注意project方法中的参数，后面
                        andExpression要处理哪个字段的数据。project中是必须要有的*/
				//Aggregation.project("hours","time").andExpression("substr(time,0,7)").as("month"),
                        /*数据处理完以后就是分组操作，group方法中的参数就是上面as取别名的字段的数据参数，这里的意思就是
                        根据month进行分组，也就是别名之前的那个time字段，不过是已经处理过的数据从time字段的“2018-03-08”
                        变成了现在month字段的“2018-03”，后面的sum方法就是分组后要计算的数据hours（工作时长）然后as别名
                        hours,这里的操作室求和，group方法后面我记得网上说还有很多强大的方法，后期我再找找刚大家参考*/
				Aggregation.group("useCode").sum("price").as("sum"),
                        /*然后排序，这里有个问题，就是分组获得数据以后个字段有些变化，原先的time变成了id，所有这里排序是
                        根据id排序的，id就是时间（后期弄明白在补）*/
				Aggregation.sort(Sort.Direction.ASC, "_id")
		);

		AggregationResults<Order> orders = mongoTemplate.aggregate(aggregation, "orders", Order.class);
		List<Order> mappedResults = orders.getMappedResults();
		System.out.println(mappedResults.size()+"打印输出");
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


	//--------------------------------------upsert demo--------------------------------------------------------------
	//db.users.update({"username":"cang"},{"$set":{"age":18}},{"upsert":true})
	@Test
	public void upsertTest(){
		Query query = query(Criteria.where("username").is("cang"));
		Update set = new Update().set("age", 18);
		UpdateResult users = mongoTemplate.upsert(query, set, User.class, "users");
	}



	//测试unset,删除字段示例
	//db.users.updateMany({"username":"lison"},{"$unset":{"country":"","age":""}})
	@Test
	public void unsetTest(){
		Query query = query(Criteria.where("username").is("lison"));
		Update unset = new Update().unset("country").unset("age");
		UpdateResult upsert = mongoTemplate.updateMulti(query, unset, User.class);
		System.out.println(upsert.getModifiedCount());
	}

	//测试rename,更新字段名称示例
	//db.users.updateMany({"username":"lison"},{"$rename":{"lenght":"height", "username":"name"}})
	@Test
	public void renameTest(){
		Query query = query(Criteria.where("username").is("lison"));
		Update rename = new Update().rename("lenght", "height").rename("username", "name");
		UpdateResult upsert = mongoTemplate.updateMulti(query, rename, User.class);
	}


	//测试pull pullAll,删除字符串数组中元素示例
//    db.users.updateMany({ "username" : "james"}, { "$pull" : { "favorites.movies" : [ "小电影2 " , "小电影3"]}})
//    db.users.updateMany({ "username" : "james"}, { "$pullAll" : { "favorites.movies" : [ "小电影2 " , "小电影3"]}})

	@Test
	public void pullAllTest(){

		Query query = query(Criteria.where("username").is("james"));
		Update pull = new Update().pull("favorites.movies", Arrays.asList("小电影2 " , "小电影3"));
		UpdateResult upsert = mongoTemplate.updateMulti(query, pull, User.class);
		System.out.println(upsert);



		query = query(Criteria.where("username").is("james"));
		Update pullAll = new Update().pullAll("favorites.movies", new String[]{"小电影2 " , "小电影3"});
		upsert = mongoTemplate.updateMulti(query, pullAll, User.class);
		System.out.println(upsert);
	}





	//--------------------------------------insert demo--------------------------------------------------------------


	//增加一条评论（$push）
	//db.users.updateOne({"username":"james"},
//                         {"$push":{"comments":{"author":"lison23",
//                                     "content":"ydddyyytttt",
//                                     "commentTime":ISODate("2019-01-06T00:00:00")}}})
	@Test
	public void addOneComment(){
		Query query = query(Criteria.where("username").is("james"));
		Comment comment = new Comment();
		comment.setAuthor("lison23");
		comment.setContent("ydddyyytttt");
		comment.setCommentTime(getDate("2019-01-06"));
		Update push = new Update().push("comments", comment);
		UpdateResult updateFirst = mongoTemplate.updateFirst(query, push, User.class);
		System.out.println(updateFirst);
	}


	//批量新增两条评论（$push,$each）
//  db.users.updateOne({"username":"james"},
//  	       {"$push":{"comments":
//  	                  {"$each":[{"author":"lison22","content":"yyyytttt","commentTime":ISODate("2019-02-06T00:00:00")},
//  	                            {"author":"lison23","content":"ydddyyytttt","commentTime":ISODate("2019-03-06T00:00:00")}]}}})
	@Test
	public void addManyComment(){
		Query query = query(Criteria.where("username").is("james"));
		Comment comment1 = new Comment();
		comment1.setAuthor("lison55");
		comment1.setContent("lison55lison55");
		comment1.setCommentTime(getDate("2019-02-06"));
		Comment comment2 = new Comment();
		comment2.setAuthor("lison66");
		comment2.setContent("lison66lison66");
		comment2.setCommentTime(getDate("2019-03-06"));
		Update push = new Update().pushAll("comments", new Comment[]{comment1,comment2});
		UpdateResult updateFirst = mongoTemplate.updateFirst(query, push, User.class);
		System.out.println(updateFirst);
	}

	// 批量新增两条评论并对数组进行排序（$push,$eachm,$sort）
//  db.users.updateOne({"username":"james"},
//  	      {"$push": {"comments":
//  	                {"$each":[ {"author":"lison22","content":"yyyytttt","commentTime":ISODate("2019-04-06T00:00:00")},
//  	                           {"author":"lison23","content":"ydddyyytttt","commentTime":ISODate("2019-05-06T00:00:00")} ],
//  	                  $sort: {"commentTime":-1} } } })
	@Test
	public void addManySortComment(){
		Query query = query(Criteria.where("username").is("james"));
		Comment comment1 = new Comment();
		comment1.setAuthor("lison77");
		comment1.setContent("lison55lison55");
		comment1.setCommentTime(getDate("2019-04-06"));
		Comment comment2 = new Comment();
		comment2.setAuthor("lison88");
		comment2.setContent("lison66lison66");
		comment2.setCommentTime(getDate("2019-05-06"));
		Update update = new Update();
		Update.PushOperatorBuilder pob = update.push("comments");
		pob.each(comment1,comment2);
		Sort sort = Sort.by(Sort.Order.desc("commentTime"));
		pob.sort(sort);
		System.out.println("---------------");
		UpdateResult updateFirst = mongoTemplate.updateFirst(query, update,User.class);
		System.out.println(updateFirst);
	}

	//--------------------------------------delete demo--------------------------------------------------------------

//  删除lison22对james的所有评论   （批量删除）
//    db.users.update({"username":“james"},
//                               {"$pull":{"comments":{"author":"lison23"}}})

	@Test
	public void deleteByAuthorComment(){
		Query query = query(Criteria.where("username").is("james"));
		Comment comment1 = new Comment();
		comment1.setAuthor("lison55");
		Update pull = new Update().pull("comments",comment1);
		UpdateResult updateFirst = mongoTemplate.updateFirst(query, pull, User.class);
		System.out.println(updateFirst);
	}


	//    删除lison5对lison评语为“lison是苍老师的小迷弟”的评论（精确删除）
//    db.users.update({"username":"lison"},
//            {"$pull":{"comments":{"author":"lison5",
//                                  "content":"lison是苍老师的小迷弟"}}})
	@Test
	public void deleteByAuthorContentComment(){
		Query query = query(Criteria.where("username").is("lison"));
		Comment comment1 = new Comment();
		comment1.setAuthor("lison5");
		comment1.setContent("lison是苍老师的小迷弟");
		Update pull = new Update().pull("comments",comment1);
		UpdateResult updateFirst = mongoTemplate.updateFirst(query, pull, User.class);
		System.out.println(updateFirst);
	}

	//--------------------------------------update demo--------------------------------------------------------------
//    db.users.updateMany({"username":"james","comments.author":"lison1"},
//            {"$set":{"comments.$.content":"xxoo",
//                        "comments.$.author":"lison10" }})
//    	含义：精确修改某人某一条精确的评论，如果有多个符合条件的数据，则修改最后一条数据。无法批量修改数组元素
	@Test
	public void updateOneComment(){
		Query query = query(Criteria.where("username").is("james").and("comments.author").is("lison11"));
		//Update update = update("comments.$.content","xxoo").set("comments.$.author","lison11");
		Update update=new Update();
		update.set("comments.$.author","lison12").set("comments.$.content","xxoo");
		UpdateResult updateFirst = mongoTemplate.updateFirst(query, update, User.class);
		System.out.println(updateFirst);
	}

//--------------------------------------findandModify demo--------------------------------------------------------------



	//使用findandModify方法在修改数据同时返回更新前的数据或更新后的数据
//db.fam.findAndModify({query:{name:'morris1'},
//    update:{$inc:{age:1}},
//    'new':true});

	@Test
	public void findAndModifyTest(){
		Query query = query(Criteria.where("name").is("morris1"));
		Update update = new Update().inc("age", 1);
		FindAndModifyOptions famo = FindAndModifyOptions.options().returnNew(true);
		Doc doc = mongoTemplate.findAndModify(query, update,famo, Doc.class);
		System.out.println(doc.toString());
	}


	private Date getDate(String string) {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		Date parse=null;
		try {
			parse = sdf.parse(string);
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return parse;
	}

}
