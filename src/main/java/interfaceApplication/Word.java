package interfaceApplication;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.bson.types.ObjectId;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import common.java.JGrapeSystem.jGrapeFW_Message;
import common.java.JGrapeSystem.rMsg;
import common.java.apps.appsProxy;
import common.java.interfaceModel.GrapeDBDescriptionModel;
import common.java.interfaceModel.GrapePermissionsModel;
import common.java.interfaceModel.GrapeTreeDBModel;
import common.java.nlogger.nlogger;
import common.java.string.StringHelper;
import common.java.time.timeHelper;

public class Word {
	private JSONObject _obj;
	private HashMap<String, Object> map;
	private GrapeTreeDBModel gDbModel;
	private String pkString;

	public Word() {
		map = new HashMap<String, Object>();
		_obj = new JSONObject();
		
		gDbModel = new GrapeTreeDBModel();
		//数据模型
		GrapeDBDescriptionModel  gdbField = new GrapeDBDescriptionModel ();
        gdbField.importDescription(appsProxy.tableConfig("Word"));
        gDbModel.descriptionModel(gdbField);
        
        //权限模型
        GrapePermissionsModel gperm = new GrapePermissionsModel();
		gperm.importDescription(appsProxy.tableConfig("Word"));
		gDbModel.permissionsModel(gperm);
		
		pkString = gDbModel.getPk();
		
        //开启检查模式
        gDbModel.checkMode();
	}

	/**
	 * 新增热词 [若库中已存在该热词，则热词搜索次数加1，否则新增热词]
	 * 
	 * @project GrapeWord
	 * @package interfaceApplication
	 * @file Word.java
	 * 
	 * @param info
	 * @return
	 *
	 */
	/*public String AddWord(String info) {
		int code = 99;
		String _id;
		JSONObject object = model.check(info, def());
		if (object == null) {
			return resultMessage(1);
		}
		// 判断库中是否存在该热搜关键词
		try {
			String content = object.get("content").toString();
			JSONObject obj = findByContent(content);
			if (obj != null) {
				// 获取该热搜词的_id
				obj = (JSONObject) obj.get("_id");
				_id = obj.get("$oid").toString();
				obj = Count(content);
				if (obj != null) {
					code = model.getdb().eq("_id", new ObjectId(_id)).data(obj).update() != null ? 0 : 99;
				}
			} else {
				code = model.getdb().data(object).insertOnce() != null ? 0 : 99;
			}
		} catch (Exception e) {
			code = 99;
		}
		return resultMessage(code, "热词新增成功");
	}*/
	
	public String AddWord(String info) {
		Object infoes = "";
		JSONObject object = JSONObject.toJSON(info);
		if (object == null || object.size() <= 0) {
			return rMsg.netMSG(2, "参数异常");
		}
		infoes = gDbModel.dataEx(object).autoComplete().insertOnce();
		object = gDbModel.eq(pkString, infoes).find();
		return resultJSONInfo(object);
	}

	// 批量新增热词
	/*public String AddWords(String infos) {
		String result = resultMessage(99);
		String info;
		JSONArray array = JSONArray.toJSONArray(infos);
		if (array != null && array.size() != 0) {
			for (int i = 0; i < array.size(); i++) {
				info = array.get(i).toString();
				result = AddWord(info);
			}
		}
		return result;
	}*/
	
	// 批量新增热词
	public String AddWords(String infos) {
		String info = "";
		List<Object> list = new ArrayList<Object>();
		JSONArray Condarray = JSONArray.toJSONArray(infos);
		JSONObject obj;
		if (Condarray == null || Condarray.size() <= 0) {
			return rMsg.netMSG(2, "参数异常");
		}
		if (Condarray != null && Condarray.size() > 0) {
			for (Object object : Condarray) {
				obj = (JSONObject) object;
				info = gDbModel.data(obj).insertOnce().toString();
				list.add(info);
			}
		}
		info = StringHelper.join(list);
		JSONArray array = FindBlock(info);
		return resultArray(array);
	}
	
	/**
	 * 
	 * [批量查询] <br> 
	 *  
	 * @author [南京喜成]<br>
	 * @param bids
	 * @return <br>
	 */
	private JSONArray FindBlock(String bids) {
		JSONArray array = null;
		gDbModel.or();
//		if (bids != null && !bids.equals("")) {//TODO 1
		if (!StringHelper.InvaildString(bids)) {
			String[] value = bids.split(",");
			for (String bid : value) {
				if (!bid.equals("") && !bid.equals("0")) {
					gDbModel.eq(pkString, bid);
				}
			}
			
			//查看有什么字段影藏
			array = gDbModel.mask("itemfatherID,itemSort,deleteable,visable,itemLevel,mMode,uMode,dMode").select();
		}
		return array;
	}

	/**
	 * 按搜索次数显示热词
	 * 
	 * @project GrapeWord
	 * @package interfaceApplication
	 * @file Word.java
	 * 
	 * @param no
	 *            显示的热词数量
	 * @param conds
	 *            条件
	 * @return
	 *
	 */
	public String ShowWord(int no, String conds) {
		JSONArray cond = JSONArray.toJSONArray(conds);
		gDbModel = (cond == null) ? gDbModel : gDbModel.where(cond);
		JSONArray array = gDbModel.desc("time").desc("count").limit(20).select();
		return resultMessage(array);
	}

	/**
	 * 删除热词，支持批量删除操作
	 * 
	 * @project GrapeWord
	 * @package interfaceApplication
	 * @file Word.java
	 * 
	 * @param id
	 * @return
	 *
	 */
	public String DeleteWord(String id) {
		int code = 99;
		try {
			String[] value = id.split(",");
			if (value.length == 1) {
				code = gDbModel.eq(pkString, new ObjectId(id)).delete() != null ? 0 : 99;
			} else {
				for (String _id : value) {
					gDbModel.eq(pkString, new ObjectId(_id));
				}
				code = gDbModel.deleteAll() == value.length ? 0 : 99;
			}
		} catch (Exception e) {
			nlogger.logout(e);
			code = 99;
		}
		return resultMessage(code, "热词删除成功");
	}

	@SuppressWarnings("unchecked")
	public String Page(int ids, int pageSize) {
		JSONArray array = new JSONArray();
		JSONObject object = new JSONObject();
		try {
			array = gDbModel.page(ids, pageSize);
			object.put("totalSize", (int) Math.ceil((double)gDbModel.count() / pageSize));
		} catch (Exception e) {
			nlogger.logout(e);
			object.put("totalSize", 0);
		}
		object.put("pageSize", pageSize);
		object.put("currentPage", ids);
		object.put("data", array);
		return resultMessage(object);
	}

	/**
	 * 根据时间查询热词
	 * 
	 * @project GrapeWord
	 * @package interfaceApplication
	 * @file Word.java
	 * 
	 *
	 */
	public String Search(String time, int no) {
		JSONArray array = null;
		try {
			array = gDbModel.like("time", time).desc("count").limit(no).select();
		} catch (Exception e) {
			array = null;
		}
		return resultMessage(array);
	}

	/**
	 * 根据角色plv，获取角色级别
	 * 
	 * @project GrapeSuggest
	 * @package interfaceApplication
	 * @file Suggest.java
	 * 
	 * @return
	 *
	 */
	/*private int getRoleSign() {
		int roleSign = 0; // 游客
		String sid = (String) execRequest.getChannelValue("sid");
		if (sid != null) {
			try {
				privilige privil = new privilige(sid);
				int roleplv = privil.getRolePV(appsProxy.appidString());
				if (roleplv >= 1000 && roleplv < 3000) {
					roleSign = 1; // 普通用户即企业员工
				}
				if (roleplv >= 3000 && roleplv < 5000) {
					roleSign = 2; // 栏目管理员
				}
				if (roleplv >= 5000 && roleplv < 8000) {
					roleSign = 3; // 企业管理员
				}
				if (roleplv >= 8000 && roleplv < 10000) {
					roleSign = 4; // 监督管理员
				}
				if (roleplv >= 10000) {
					roleSign = 5; // 总管理员
				}
			} catch (Exception e) {
				nlogger.logout(e);
				roleSign = 0;
			}
		}
		return roleSign;
	}*/

	/**
	 * 根据内容查询热搜关键词
	 * 
	 * @project GrapeWord
	 * @package interfaceApplication
	 * @file Word.java
	 * 
	 * @param content
	 * @return
	 *
	 */
	@SuppressWarnings("unused")
	private JSONObject findByContent(String content) {
		JSONObject object = gDbModel.eq("content", content).find();
		return object;
	}

	@SuppressWarnings({ "unchecked", "unused" })
	private JSONObject Count(String content) {
		JSONObject object = null;
		try {
			object = new JSONObject();
			object = gDbModel.eq("content", content).field("count").find();
			int count = Integer.parseInt(object.get("count").toString()) + 1;
			object.put("count", count);
		} catch (Exception e) {
			nlogger.logout(e);
			object = null;
		}
		return object;
	}

	@SuppressWarnings("unused")
	private HashMap<String, Object> def() {
		map.put("content", "");
		map.put("count", 1);
		map.put("state", 1); // 待审核热词
		map.put("time", timeHelper.nowMillis());
		return map;
	}

	@SuppressWarnings("unused")
	private String resultMessage(int num) {
		return resultMessage(num, "");
	}

	@SuppressWarnings("unchecked")
	private String resultMessage(JSONObject object) {
		if (object == null) {
			object = new JSONObject();
		}
		_obj.put("records", object);
		return resultMessage(0, _obj.toString());
	}

	@SuppressWarnings("unchecked")
	private String resultMessage(JSONArray array) {
		if (array == null) {
			array = new JSONArray();
		}
		_obj.put("records", array);
		return resultMessage(0, _obj.toString());
	}

	private String resultMessage(int num, String message) {
		String msg = "";
		switch (num) {
		case 0:
			msg = message;
			break;
		case 1:
			msg = "必填字段没有填";
			break;
		case 2:
			msg = "没有操作权限";
			break;
		default:
			msg = "其他操作异常";
			break;
		}
		return jGrapeFW_Message.netMSG(num, msg);
	}
	
	@SuppressWarnings("unchecked")
	public String resultJSONInfo(JSONObject object) {
		if (object == null) {
			object = new JSONObject();
		}
		_obj.put("records", object);
		return resultMessage(0, _obj.toString());
	}
	
	@SuppressWarnings("unchecked")
	public String resultArray(JSONArray array) {
		if (array == null) {
			array = new JSONArray();
		}
		_obj.put("records", array);
		return resultMessage(0, _obj.toString());
	}
}
