package com.openlap.AnalyticsMethods.Prototypes.Counter;

import com.openlap.dataset.OpenLAPColumnDataType;
import com.openlap.dataset.OpenLAPDataColumnFactory;
import com.openlap.dataset.OpenLAPDataSet;
import com.openlap.dynamicparam.OpenLAPDynamicParamDataType;
import com.openlap.dynamicparam.OpenLAPDynamicParamFactory;
import com.openlap.dynamicparam.OpenLAPDynamicParamType;
import com.openlap.dynamicparam.OpenLAPDynamicParams;
import com.openlap.exceptions.OpenLAPDataColumnException;
import com.openlap.exceptions.OpenLAPDynamicParamException;
import com.openlap.template.AnalyticsMethod;

import java.io.InputStream;
import java.util.*;
import java.util.regex.Pattern;

/**
 * Created by Arham Muslim on 11-Mar-16.
 * This Analytics Method counts the occurrences of each item in the incoming OpenLAP-DataSet, sort them in descending order and select the top 10 items
 */
public class Keywords extends AnalyticsMethod {
    public Keywords() {
        this.setInput(new OpenLAPDataSet());
        this.setOutput(new OpenLAPDataSet());
        this.setParams(new OpenLAPDynamicParams());

        try {
            this.getInput().addOpenLAPDataColumn(
                    OpenLAPDataColumnFactory.createOpenLAPDataColumnOfType("item_name", OpenLAPColumnDataType.Text, true, "Items", "List of items to count.")
            );
            this.getOutput().addOpenLAPDataColumn(
                    OpenLAPDataColumnFactory.createOpenLAPDataColumnOfType("keywords", OpenLAPColumnDataType.Text, true, "Keywords", "List of keywords.")
            );
            this.getOutput().addOpenLAPDataColumn(
                    OpenLAPDataColumnFactory.createOpenLAPDataColumnOfType("keyword_count", OpenLAPColumnDataType.Numeric, true, "Keyword Count", "Number of time each keyword occurred in the list.")
            );
        } catch (OpenLAPDataColumnException e) {
            e.printStackTrace();
        }

        try {
            this.getParams().addOpenLAPDynamicParam(
                    OpenLAPDynamicParamFactory.createOpenLAPDataColumnOfType("keywords", OpenLAPDynamicParamType.Textbox, OpenLAPDynamicParamDataType.STRING, "Keywords list", "Case insensitive comma separated keywords list. Combine multiple keywords using |(OR) or &(AND) operators. Brackets () are currently not supported. Only use one type of operator for combining. E.g. learn&analyze&teach,good|great|best|nice", "", "", true)
            );
            this.getParams().addOpenLAPDynamicParam(
                    OpenLAPDynamicParamFactory.createOpenLAPDataColumnOfType("useUniqueData", OpenLAPDynamicParamType.Choice, OpenLAPDynamicParamDataType.STRING, "Search Type", "Search for keywords from a unique set of items in the list or search in the whole list", "Complete List", "Complete List,Unique Items", true)
            );
            this.getParams().addOpenLAPDynamicParam(
                    OpenLAPDynamicParamFactory.createOpenLAPDataColumnOfType("include_others", OpenLAPDynamicParamType.Checkbox, OpenLAPDynamicParamDataType.STRING, "Show misc. item count.", "Include an additional column called 'Misc.' that shows the number of items that does not contain any of the provided keywords.", "false", "", true)
            );
        } catch (OpenLAPDynamicParamException e) {
            e.printStackTrace();
        }
    }

    @Override
    public String getAnalyticsMethodName() {
        return "Count keywords in items list";
    }

    @Override
    public String getAnalyticsMethodDescription() {
        return "Count the occurrences of provided keywords in the list.";
    }

    @Override
    public String getAnalyticsMethodCreator() {
        return "Arham Muslim";
    }

    @Override
    protected void implementationExecution() {

        try {
            LinkedHashMap<Pattern, Integer> patternCount = new LinkedHashMap<>();

            String keywordsString = (String)this.getParams().getParams().get("keywords").getValue();
            String isUniqueString = (String)this.getParams().getParams().get("useUniqueData").getValue();
            boolean useUniqueData = !isUniqueString.equals("Complete List");

            String includeOtherString = (String)this.getParams().getParams().get("include_others").getValue();
            boolean includeOthers = includeOtherString.equals("true");

            if(keywordsString != null && !keywordsString.isEmpty()){

                String[] keywords = keywordsString.split(",", -1);

                for(String keyword:keywords){
                    Pattern pattern;

                    if(keyword.contains("&")){
                        String[] subKeywords = keyword.split("&");

                        String patternString = "";
                        for(String subKeyword: subKeywords)
                            if(subKeyword != null && !subKeyword.isEmpty()){
                                patternString += "(?=.*"+ subKeyword.replace("*","\\*").replace("?","\\?").replace("\\","\\\\") +")";
                        }

                        pattern = Pattern.compile(patternString, Pattern.CASE_INSENSITIVE|Pattern.DOTALL);
                    }
                    else{
                        pattern = Pattern.compile(keyword, Pattern.CASE_INSENSITIVE|Pattern.DOTALL);
                    }

                    patternCount.put(pattern, 0);
                }

            }

            Iterator items = this.getInput().getColumns().get("item_name").getData().iterator();

            Set<Map.Entry<Pattern, Integer>> patternCountSet = patternCount.entrySet();

            List<String> uniqueItems = null;
            int otherCount = 0;

            if(useUniqueData)
                uniqueItems = new ArrayList<>();

            while (items.hasNext()){
                String item = (String) items.next();

                if(useUniqueData){
                    if(uniqueItems.contains(item))
                        continue;
                    else
                        uniqueItems.add(item);
                }

                boolean containsKeyword = false;

                for(Map.Entry<Pattern, Integer> pattern: patternCountSet){
                    if(pattern.getKey().matcher(item).find()) {
                        pattern.setValue(pattern.getValue() + 1);
                        containsKeyword = true;
                    }
                }

                if(!containsKeyword)
                    otherCount++;
            }

            for(Map.Entry<Pattern, Integer> patternSet: patternCountSet){

                String patterString = patternSet.getKey().pattern();

                patterString = patterString.replace("|", " or ")
                        .replace("?=.*","")
                        .replace(")("," and ")
                        .replace("(","")
                        .replace(")","")
                        .replace("\\*","*")
                        .replace("\\?","?")
                        .replace("\\\\","\\");

                getOutput().getColumns().get("keywords").getData().add(patterString);
                getOutput().getColumns().get("keyword_count").getData().add(patternSet.getValue());
            }

            if(includeOthers){
                getOutput().getColumns().get("keywords").getData().add("Misc.");
                getOutput().getColumns().get("keyword_count").getData().add(otherCount);
            }
        }
        catch (Exception exc)
        {
            System.out.println(exc.getMessage());
        }
    }
    
    @Override
    public InputStream getPMMLInputStream() {
        return null;
    }

    @Override
    public Boolean hasPMML() {
        return false;
    }
}
