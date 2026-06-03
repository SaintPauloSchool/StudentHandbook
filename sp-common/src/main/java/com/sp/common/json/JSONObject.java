package com.sp.common.json;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sp.common.utils.StringUtils;

/**
 * 通用消息對象，基於Map實現的可嵌套數據結構。 支持JSON數據結構。
 *
 */
public class JSONObject extends LinkedHashMap<String, Object>
{
    private static final long serialVersionUID = 1L;
    private static final Pattern arrayNamePattern = Pattern.compile("(\\w+)((\\[\\d+\\])+)");
    private static final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 數組結構。
     */
    public static class JSONArray extends ArrayList<Object>
    {
        private static final long serialVersionUID = 1L;

        public JSONArray()
        {
            super();
        }

        public JSONArray(int size)
        {
            super(size);
        }

        @Override
        public String toString()
        {
            try
            {
                return JSON.marshal(this);
            }
            catch (Exception e)
            {
                throw new RuntimeException(e);
            }
        }

        @Override
        public Object set(int index, Object element)
        {
            return super.set(index, transfer(element));
        }

        @Override
        public boolean add(Object element)
        {
            return super.add(transfer(element));
        }

        @Override
        public void add(int index, Object element)
        {
            super.add(index, transfer(element));
        }
    }

    public JSONObject()
    {
        super();
    }

    public JSONObject(final JSONObject other)
    {
        super(other);
    }

    @Override
    public String toString()
    {
        try
        {
            return JSON.marshal(this);
        }
        catch (Exception e)
        {
            throw new RuntimeException(e);
        }
    }

    /**
     * 轉換爲緊湊格式的字符串。
     * 
     * @return 返回本對象緊湊格式字符串。
     */
    public String toCompactString()
    {
        try
        {
            return objectMapper.writeValueAsString(this);
        }
        catch (Exception e)
        {
            throw new RuntimeException(e);
        }
    }

    /**
     * 獲取指定字段的整數值。如果字段不存在，或者無法轉換爲整數，返回null。
     * 
     * @param name 字段名，支持多級。
     * @return 返回指定的整數值，或者null。
     */
    public Integer intValue(final String name)
    {
        return valueAsInt(value(name));
    }

    /**
     * 獲取指定字段的整數值。如果字段不存在，或者無法轉換爲整數，返回defaultValue。
     * 
     * @param name 字段名，支持多級。
     * @param defaultValue 查詢失敗時，返回的值。
     * @return 返回指定的整數值，或者defaultValue。
     */
    public Integer intValue(final String name, final Integer defaultValue)
    {
        return StringUtils.nvl(intValue(name), defaultValue);
    }

    /**
     * 獲取指定字段的長整數值。如果字段不存在，或者無法轉換爲長整數，返回null。
     * 
     * @param name 字段名，支持多級。
     * @return 返回指定的長整數值，或者null。
     */
    public Long longValue(final String name)
    {
        return valueAsLong(value(name));
    }

    /**
     * 獲取指定字段的長整數值。如果字段不存在，或者無法轉換爲長整數，返回defaultValue。
     * 
     * @param name 字段名，支持多級。
     * @param defaultValue 查詢失敗時，返回的值。
     * @return 返回指定的長整數值，或者defaultValue。
     */
    public Long longValue(final String name, final Long defaultValue)
    {
        return StringUtils.nvl(longValue(name), defaultValue);
    }

    /**
     * 獲取指定字段的布爾值。如果字段不存在，或者無法轉換爲布爾型，返回null。
     * 
     * @param name 字段名，支持多級。
     * @return 返回指定的布爾值，或者null。
     */
    public Boolean boolValue(final String name)
    {
        return valueAsBool(value(name));
    }

    /**
     * 獲取指定字段的布爾值。如果字段不存在，或者無法轉換爲布爾型，返回defaultValue。
     * 
     * @param name 字段名，支持多級。
     * @param defaultValue 查詢失敗時，返回的值。
     * @return 返回指定的布爾值，或者defaultValue。
     */
    public Boolean boolValue(final String name, final Boolean defaultValue)
    {
        return StringUtils.nvl(boolValue(name), defaultValue);
    }

    /**
     * 獲取指定字段的字符串值。如果字段不存在，返回null。
     * 
     * @param name 字段名，支持多級。
     * @return 返回指定的字符串值，或者null。
     */
    public String strValue(final String name)
    {
        return valueAsStr(value(name));
    }

    /**
     * 獲取指定字段的字符串值。如果字段不存在，返回defaultValue。
     * 
     * @param name 字段名，支持多級。
     * @param defaultValue 查詢失敗時，返回的值。
     * @return 返回指定的字符串值，或者defaultValue。
     */
    public String strValue(final String name, final String defaultValue)
    {
        return StringUtils.nvl(strValue(name), defaultValue);
    }

    /**
     * 獲取指定字段的值。
     * 
     * @param name 字段名，支持多級，支持數組下標。
     * @return 返回指定字段的值。
     */
    public Object value(final String name)
    {
        final int indexDot = name.indexOf('.');
        if (indexDot >= 0)
        {
            return obj(name.substring(0, indexDot)).value(name.substring(indexDot + 1));
        }
        else
        {
            final Matcher matcher = arrayNamePattern.matcher(name);
            if (matcher.find())
            {
                return endArray(matcher.group(1), matcher.group(2), new EndArrayCallback<Object>()
                {
                    @Override
                    public Object callback(JSONArray arr, int index)
                    {
                        return elementAt(arr, index);
                    }
                });
            }
            else
            {
                return get(name);
            }
        }
    }

    /**
     * 設置指定字段的值。
     * 
     * @param name 字段名，支持多級，支持數組下標。
     * @param value 字段值。
     * @return 返回本對象。
     */
    public JSONObject value(final String name, final Object value)
    {
        final int indexDot = name.indexOf('.');
        if (indexDot >= 0)
        {
            obj(name.substring(0, indexDot)).value(name.substring(indexDot + 1), value);
        }
        else
        {
            final Matcher matcher = arrayNamePattern.matcher(name);
            if (matcher.find())
            {
                endArray(matcher.group(1), matcher.group(2), new EndArrayCallback<Void>()
                {
                    @Override
                    public Void callback(JSONArray arr, int index)
                    {
                        elementAt(arr, index, value);
                        return null;
                    }
                });
            }
            else
            {
                set(name, value);
            }
        }
        return this;
    }

    /**
     * 獲取對象（非標量類型）字段。返回的數據是一個結構體。當不存在指定對象時，則爲指定的名字創建一個空的MessageObject對象。
     * 
     * @param name 字段名。不支持多級名字，支持數組下標。
     * @return 返回指定的對象。如果對象不存在，則爲指定的名字創建一個空的MessageObject對象。
     */
    public JSONObject obj(final String name)
    {
        final Matcher matcher = arrayNamePattern.matcher(name);
        if (matcher.find())
        {
            return endArray(matcher.group(1), matcher.group(2), new EndArrayCallback<JSONObject>()
            {
                @Override
                public JSONObject callback(JSONArray arr, int index)
                {
                    return objAt(arr, index);
                }
            });
        }
        else
        {
            JSONObject obj = getObj(name);
            if (obj == null)
            {
                obj = new JSONObject();
                put(name, obj);
            }
            return obj;
        }
    }

    /**
     * 獲取數組字段。將名字對應的對象以數組對象返回，當指定的字段不存在時，創建一個空的數組。
     * 
     * @param name 字段名。不支持多級名字，不支持下標。
     * @return 返回一個數組（List）。
     */
    public JSONArray arr(final String name)
    {
        JSONArray arr = getArr(name);
        if (arr == null)
        {
            arr = new JSONArray();
            put(name, arr);
        }
        return arr;
    }

    /**
     * 獲取對象（非標量類型）字段。返回的數據是一個結構體。
     * 
     * @param name 字段名。
     * @return 返回指定的對象字段。
     */
    public JSONObject getObj(final String name)
    {
        return (JSONObject) get(name);
    }

    /**
     * 獲取數組類型字段。
     * 
     * @param name 字段名。
     * @return 返回數組類型字段。
     */
    public JSONArray getArr(final String name)
    {
        return (JSONArray) get(name);
    }

    /**
     * 返回字段整數值。如果不存在，返回null。
     * 
     * @param name 字段名。
     * @return 返回指定字段整數值。
     */
    public Integer getInt(final String name)
    {
        return valueAsInt(get(name));
    }

    /**
     * 返回字段整數值。如果不存在，返回defaultValue。
     * 
     * @param name 字段名。
     * @param defaultValue 字段不存在時，返回的值。
     * @return 返回指定字段整數值。
     */
    public Integer getInt(final String name, Integer defaultValue)
    {
        return StringUtils.nvl(getInt(name), defaultValue);
    }

    /**
     * 返回字段長整數值。如果不存在，返回null。
     * 
     * @param name 字段名。
     * @return 返回指定字段長整數值。
     */
    public Long getLong(final String name)
    {
        return valueAsLong(get(name));
    }

    /**
     * 返回字段長整數值。如果不存在，返回defaultValue。
     * 
     * @param name 字段名。
     * @param defaultValue 字段不存在時，返回的值。
     * @return 返回指定字段長整數值。
     */
    public Long getLong(final String name, Long defaultValue)
    {
        return StringUtils.nvl(getLong(name), defaultValue);
    }

    /**
     * 返回字段字符串值。如果不存在，返回null。
     * 
     * @param name 字段名。
     * @return 返回指定字段字符串值。
     */
    public String getStr(final String name)
    {
        return valueAsStr(get(name));
    }

    /**
     * 返回字段字符串值。如果不存在，返回defaultValue。
     * 
     * @param name 字段名。
     * @param defaultValue 字段不存在時，返回的值。
     * @return 返回指定字段字符串值。
     */
    public String getStr(final String name, final String defaultValue)
    {
        return StringUtils.nvl(getStr(name), defaultValue);
    }

    /**
     * 字段值按照布爾類型返回。如果不存在，返回null。
     * 
     * @param name 字段名。
     * @return 字段值。
     */
    public Boolean getBool(final String name)
    {
        return valueAsBool(get(name));
    }

    /**
     * 字段值按照布爾類型返回。如果不存在，返回defaultValue。
     * 
     * @param name 字段名。
     * @param defaultValue 字段不存在時，返回的值。
     * @return 字段值。
     */
    public Boolean getBool(final String name, final Boolean defaultValue)
    {
        return StringUtils.nvl(getBool(name), defaultValue);
    }

    /**
     * 設置字段值
     * 
     * @param name 字段名
     * @param value 字段值（標量：數字、字符串、布爾型；結構體：MessageObject）。 如果是Map類型同時非MessageObject類型，則自動轉換爲MessageObject類型再存入
     *            （此時，再修改Map中的數據，將不會體現到本對象中）。
     * @return 返回本對象
     */
    public JSONObject set(final String name, final Object value)
    {
        put(name, value);
        return this;
    }

    /**
     * 將本對象轉換爲Java Bean。
     * 
     * @param beanClass Java Bean的類對象。
     * @return 返迴轉換後的Java Bean。
     */
    public <T> T asBean(Class<T> beanClass)
    {
        try
        {
            return JSON.unmarshal(JSON.marshal(this), beanClass);
        }
        catch (Exception e)
        {
            throw new RuntimeException(e);
        }
    }

    /**
     * 重載基類的方法。如果 value 是 Map 類型，但不是 MessageObject 類型，則創建一個包含內容等同於原 Map 的 MessageObject 作爲 value（注意：此後再更改 Map 的內容，將不會反映到
     * MessageObject 中）。 重載此方法的目的是爲了使JSON能夠正確地解析爲MessageObject對象。不建議直接調用此方法，請使用 set(name, value)方法設置字段值。
     */
    @Override
    public Object put(String key, Object value)
    {
        return super.put(key, transfer(value));
    }

    public static Integer valueAsInt(Object value)
    {
        if (value instanceof Integer)
        {
            return (Integer) value;
        }
        else if (value instanceof Number)
        {
            return ((Number) value).intValue();
        }
        else if (value instanceof String)
        {
            return Integer.valueOf((String) value);
        }
        else if (value instanceof Boolean)
        {
            return ((Boolean) value) ? 1 : 0;
        }
        else
        {
            return null;
        }
    }

    public static Long valueAsLong(Object value)
    {
        if (value instanceof Long)
        {
            return (Long) value;
        }
        else if (value instanceof Number)
        {
            return ((Number) value).longValue();
        }
        else if (value instanceof String)
        {
            return Long.valueOf((String) value);
        }
        else if (value instanceof Boolean)
        {
            return ((Boolean) value) ? 1L : 0L;
        }
        else
        {
            return null;
        }
    }

    public static String valueAsStr(Object value)
    {
        if (value instanceof String)
        {
            return (String) value;
        }
        else if (value != null)
        {
            return value.toString();
        }
        else
        {
            return null;
        }
    }

    public static Boolean valueAsBool(Object value)
    {
        if (value instanceof Boolean)
        {
            return (Boolean) value;
        }
        else if (value instanceof Number)
        {
            return ((Number) value).doubleValue() != 0.0;
        }
        else if (value instanceof String)
        {
            return Boolean.valueOf((String) value);
        }
        else
        {
            return null;
        }
    }

    /**
     * 將所有層次中凡是Map類型同時又不是MessageObject的類型，轉換爲MessageObject類型。
     * 
     * @param value 值。
     * @return 返迴轉換後的值。
     */
    @SuppressWarnings("unchecked")
    private static Object transfer(final Object value)
    {
        if (!(value instanceof JSONObject) && value instanceof Map)
        {
            return toObj((Map<String, Object>) value);
        }
        else if (!(value instanceof JSONArray) && value instanceof Collection)
        {
            return toArr((Collection<Object>) value);
        }
        else
        {
            return value;
        }
    }

    private static JSONArray toArr(final Collection<Object> list)
    {
        final JSONArray arr = new JSONArray(list.size());
        for (final Object element : list)
        {
            arr.add(element);
        }
        return arr;
    }

    private static JSONObject toObj(final Map<String, Object> map)
    {
        final JSONObject obj = new JSONObject();
        for (final Map.Entry<String, Object> ent : map.entrySet())
        {
            obj.put(ent.getKey(), transfer(ent.getValue()));
        }
        return obj;
    }

    /**
     * 將指定下標元素作爲數組返回，如果不存在，則在該位置創建一個空的數組。
     * 
     * @param arr 當前數組。
     * @param index 下標。
     * @return 返回當前數組指定下標的元素，該元素應該是一個數組。
     */
    private static JSONArray arrayAt(JSONArray arr, int index)
    {
        expand(arr, index);
        if (arr.get(index) == null)
        {
            arr.set(index, new JSONArray());
        }
        return (JSONArray) arr.get(index);
    }

    /**
     * 將指定下標元素作爲結構體返回，如果不存在，則在該位置創建一個空的結構體。
     * 
     * @param arr 當前數組。
     * @param index 下標。
     * @return 返回當前數組指定下標元素，該元素是一個結構體。
     */
    private static JSONObject objAt(final JSONArray arr, int index)
    {
        expand(arr, index);
        if (arr.get(index) == null)
        {
            arr.set(index, new JSONObject());
        }
        return (JSONObject) arr.get(index);
    }

    /**
     * 設置數組指定下標位置的值。
     * 
     * @param arr 數組。
     * @param index 下標。
     * @param value 值。
     */
    private static void elementAt(final JSONArray arr, final int index, final Object value)
    {
        expand(arr, index).set(index, value);
    }

    /**
     * 獲取數組指定下標元素的值。
     * 
     * @param arr 數組。
     * @param index 下標。
     * @return 值。
     */
    private static Object elementAt(final JSONArray arr, final int index)
    {
        return expand(arr, index).get(index);
    }

    /**
     * 擴展數組到指定下標，以防止訪問時下標越界。
     * 
     * @param arr 數組
     * @param index 下標
     * @return 返回傳入的數組
     */
    private static JSONArray expand(final JSONArray arr, final int index)
    {
        while (arr.size() <= index)
        {
            arr.add(null);
        }
        return arr;
    }

    /**
     * 最後數組回調。
     * 
     * @author Mike
     *
     * @param <T> 回調返回數據類型。
     */
    private interface EndArrayCallback<T>
    {
        /**
         * 當定位到最後一級數組，將調用本方法。
         * 
         * @param arr 最後一級數組對象。
         * @param index 最後一級索引。
         * @return 返回回調的返回值。
         */
        T callback(JSONArray arr, int index);
    }

    /**
     * 處理多維數組的工具函數（包括一維數組）。多維數組的名字如：arrary[1][2][3]， 則name=array，indexStr=[1][2][3]，在callback中，endArr將是
     * array[1][2]指定的對象，indexe=3。
     * 
     * @param name 不帶下標的名字，不支持多級名字。
     * @param indexesStr 索引部分的字符串，如：[1][2][3]
     * @param callback 回調函數。
     * @return 返回回調函數的返回值。
     */
    private <T> T endArray(final String name, final String indexesStr, final EndArrayCallback<T> callback)
    {
        JSONArray endArr = arr(name);
        final int[] indexes = parseIndexes(indexesStr);
        int i = 0;
        while (i < indexes.length - 1)
        {
            endArr = arrayAt(endArr, indexes[i++]);
        }
        return callback.callback(endArr, indexes[i]);
    }

    private static int[] parseIndexes(final String s)
    {
        int[] indexes = null;
        List<Integer> list = new ArrayList<Integer>();

        final StringTokenizer st = new StringTokenizer(s, "[]");
        while (st.hasMoreTokens())
        {
            final int index = Integer.valueOf(st.nextToken());
            if (index < 0)
            {
                throw new RuntimeException(String.format("Illegal index %1$d in \"%2$s\"", index, s));
            }

            list.add(index);
        }

        indexes = new int[list.size()];
        int i = 0;
        for (Integer tmp : list.toArray(new Integer[list.size()]))
        {
            indexes[i++] = tmp;
        }

        return indexes;
    }
}
