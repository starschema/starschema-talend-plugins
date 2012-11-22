// ============================================================================
//
// Copyright (C) 2006-2012 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.core.token;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import us.monoid.json.JSONArray;
import us.monoid.json.JSONException;
import us.monoid.json.JSONObject;

/**
 * ggu class global comment. Detailled comment
 */
public final class TokenInforUtil {

    /**
     * 
     * calc the average to x.x. for example, 5/3,should be 1.8
     */
    public static String calcAverageToStr(int total, int per) {
        return String.valueOf(calcAverage(total, per));
    }

    public static float calcAverage(int total, int per) {
        if (per > 0 && total > 0) {
            BigDecimal bd = new BigDecimal(1.0f * total / per);
            bd = bd.setScale(1, BigDecimal.ROUND_HALF_UP);
            return bd.floatValue();
        }
        return 0;
    }

    /**
     * 
     * ggu Comment method "integrateJSONObject".
     * 
     * if have same key and is not json value, will be error
     */
    public static void integrateJSONObject(JSONObject target, JSONObject source) throws Exception {
        if (target != null && source != null) {
            Iterator keys = source.keys();
            while (keys.hasNext()) {
                String key = (String) keys.next();
                Object sourceValue = source.get(key);
                if (target.has(key)) {
                    Object targetValue = target.get(key);
                    boolean valid = true;
                    if (sourceValue instanceof JSONObject && targetValue instanceof JSONObject) {
                        integrateJSONObject((JSONObject) targetValue, (JSONObject) sourceValue);
                        valid = true;
                    } else if (sourceValue instanceof JSONArray && targetValue instanceof JSONArray) {
                        JSONArray sourceArray = (JSONArray) sourceValue;
                        JSONArray targetArray = (JSONArray) targetValue;

                        for (int i = 0; i < sourceArray.length(); i++) {
                            targetArray.put(sourceArray.get(i));
                        }
                        valid = true;
                    } else {
                        throw new IllegalArgumentException("Have same value existed in target: " + key + "," + targetValue);
                    }
                    if (!valid) {
                        throw new IllegalArgumentException("the type is not match, target is "
                                + targetValue.getClass().getSimpleName());
                    }
                } else { // if not contain, add directly.
                    target.put(key, sourceValue);
                }
            }
        }
    }

    /**
     * 
     * ggu Comment method "getDateAfter".
     * 
     * add the days after date
     */
    public static Date getDateAfter(Date date, int days) {
        Calendar now = Calendar.getInstance();
        now.setTime(date);
        now.set(Calendar.DATE, now.get(Calendar.DATE) + days);
        return now.getTime();
    }

    /**
     * 
     * ggu Comment method "convertTopComponents".
     * 
     * convert the map to json, and only with the max values.
     */
    public static JSONObject convertTopComponents(Map<String, Integer> numComponentMap, final int max) throws JSONException {
        JSONObject topComponentsObject = new JSONObject();
        if (numComponentMap != null && !numComponentMap.isEmpty()) {
            List<Integer> numList = new ArrayList(new HashSet(numComponentMap.values()));
            Collections.sort(numList);
            if (numList.size() > max) {
                List<Integer> tmpList = new ArrayList<Integer>();
                for (int i = 0; i < max; i++) {
                    tmpList.add(numList.get(i));
                }
                numList = tmpList;
            }
            for (String name : numComponentMap.keySet()) {
                Integer num = numComponentMap.get(name);
                if (num != null && numList.contains(num)) { // is top20
                    topComponentsObject.put(name, num);
                    if (topComponentsObject.length() > max) {
                        break;
                    }
                }
            }
        }
        return topComponentsObject;
    }

    public static JSONArray convertTopComponentsArray(Map<String, Integer> numComponentMap, final int max) throws JSONException {
        JSONArray topComponentsArray = new JSONArray();
        JSONObject topComponentsObject = convertTopComponents(numComponentMap, max);
        Iterator<String> keys = topComponentsObject.keys();
        while (keys.hasNext()) {
            topComponentsArray.put(keys.next());
        }
        return topComponentsArray;
    }
}
