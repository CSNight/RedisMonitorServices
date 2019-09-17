package com.csnight.redis.monitor.utils;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.dom4j.Attribute;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;

import java.util.List;
import java.util.Map;

public class JSONUtil {
    /**
     * 将一个实体类对象转化成JSON数据格式,等效于object2json
     *
     * @param obj 实体类对象
     * @return JSON数据格式字符串
     */
    public static String pojo2json(Object obj) {
        return JSONObject.toJSONString(obj);
    }

    public static Object map2pojo(Map map, Class javaBean) {
        String str = JSONUtil.map2json(map);
        return JSONUtil.json2pojo(str, javaBean);
    }

    /**
     * 将数组集合等对象转换成JSON字符串
     *
     * @param list array
     * @return String json
     */
    public static String object2json(Object list) {
        return JSONObject.toJSONString(list);
    }

    /**
     * 将Map准换为JSON字符串,等效于object2json()
     *
     * @param map map集合
     * @return JSON字符串
     */
    public static String map2json(Map<?, ?> map) {
        return JSONObject.toJSONString(map);
    }

    /**
     * 将xml字符串转换为JSON字符串
     *
     * @param xmlString xml字符串
     * @return JSON对象
     */
    static String xml2json(String xmlString) {
        try {
            Element node = DocumentHelper.parseText(xmlString).getRootElement();
            return elements2Json(node).toJSONString();
        } catch (DocumentException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * org.dom4j.Element 转  com.alibaba.fastjson.JSONObject
     *
     * @param node Element
     * @return JSONObject
     */
    private static JSONObject elements2Json(Element node) {
        JSONObject result = new JSONObject();
        // 当前节点的名称、文本内容和属性
        List<Attribute> listAttr = node.attributes();// 当前节点的所有属性的list
        for (Attribute attr : listAttr) {// 遍历当前节点的所有属性
            result.put(attr.getName(), attr.getValue());
        }
        // 递归遍历当前节点所有的子节点
        List<Element> listElement = node.elements();// 所有一级子节点的list
        if (!listElement.isEmpty()) {
            for (Element e : listElement) {// 遍历所有一级子节点
                if (e.attributes().isEmpty() && e.elements().isEmpty()) // 判断一级节点是否有属性和子节点
                    result.put(e.getName(), e.getTextTrim());// 沒有则将当前节点作为上级节点的属性对待
                else {
                    if (!result.containsKey(e.getName())) // 判断父节点是否存在该一级节点名称的属性
                        result.put(e.getName(), new JSONArray());// 没有则创建
                    ((JSONArray) result.get(e.getName())).add(elements2Json(e));// 将该一级节点放入该节点名称的属性对应的值中
                }
            }
        }
        return result;
    }


    /**
     * 将Json格式的字符串转换成指定的对象返回
     *
     * @param jsonStr  要转化的Json格式的字符串
     * @param javaBean 指定转化对象类型
     * @return 转化后的对象
     */
    public static <T> T json2pojo(String jsonStr,
                                  @SuppressWarnings("rawtypes") Class<T> javaBean) {
        return JSONObject.parseObject(jsonStr, javaBean);
    }

    /**
     * 将Json格式的字符串转换成Map对象
     *
     * @param jsonString JSON数据格式字符串
     * @return map集合
     */
    @SuppressWarnings("unchecked")
    public static Map<String, String> json2Map(String jsonString) {
        return (Map<String, String>) json2pojo(jsonString, Map.class);
    }


    /**
     * 将Json格式的字符串转换成指定对象组成的List返回
     *
     * @param jsonString JSON数据格式字符串
     * @param pojoClass  指定转化对象类型
     * @return list集合
     */
    public static List<?> jsonArray2List(String jsonString,
                                         @SuppressWarnings("rawtypes") Class pojoClass) {
        return JSONArray.parseArray(jsonString, pojoClass);
    }

}
